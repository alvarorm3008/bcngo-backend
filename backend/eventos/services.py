"""
Servicios para gestionar eventos culturales, incluyendo sincronización desde APIs y notificaciones push.
"""

import json
from datetime import datetime  # pylint: disable=unused-import
import requests
import google.auth
from google.auth.transport.requests import Request
from django.utils.dateparse import parse_datetime
from django.utils.timezone import now, make_aware, is_aware, utc  # pylint: disable=no-name-in-module
from .models import CulturalEvent


def is_valid_date(date):
    """
    Valida que una fecha esté en un rango lógico.
    """
    try:
        if not date:
            return False
        year = date.year
        return 1900 <= year <= 2100  # Ajusta el rango según tus necesidades
    except AttributeError:
        return False

def convert_to_aware(datetime_obj):
    """
    Convierte una fecha naive a aware, si es necesario.
    """
    if datetime_obj and not is_aware(datetime_obj):
        return make_aware(datetime_obj, utc)
    return datetime_obj

def sync_events_from_api():  # pylint: disable=too-many-branches, too-many-statements
    """
    Sincroniza los eventos culturales desde la API de Open Data Barcelona.
    Elimina eventos finalizados y actualiza o crea nuevos eventos en la base de datos.
    """
    # URL de la API
    url = "https://opendata-ajuntament.barcelona.cat/data/api/action/datastore_search"
    params = {
        "resource_id": "3abb2414-1ee0-446e-9c25-380e938adb73",
        "limit": 5000,
        "fields": "name,geo_epgs_4326_lon,start_date,end_date,geo_epgs_4326_lat,"
                  "addresses_road_name,addresses_district_name,addresses_start_street_number,register_id,modified",
    }

    try:  # pylint: disable=too-many-nested-blocks
        # Hacer la petición a la API
        response = requests.get(url, params=params)  # pylint: disable=missing-timeout
        response.raise_for_status()  # Lanza una excepción si hay un error HTTP

        # Procesar los datos de la respuesta
        data = response.json()
        if data.get('success'):
            events = data['result']['records']

            # Paso 1: Eliminar eventos finalizados
            CulturalEvent.objects.filter(end_date__lt=now()).delete()

            # Paso 2: Procesar eventos de la API
            for event in events:
                try:
                    # Validar los datos antes de guardar
                    start_date = (
                        convert_to_aware(parse_datetime(event.get('start_date')))
                        if event.get('start_date') else None
                    )
                    end_date = (
                        convert_to_aware(parse_datetime(event.get('end_date')))
                        if event.get('end_date') else None
                    )
                    modified_date = (
                        convert_to_aware(parse_datetime(event.get('modified')))
                        if event.get('modified') else None
                    )
                    # Omitir eventos con datos esenciales faltantes
                    if not all([event.get('register_id'), event.get('name'), end_date]):
                        print(f"Evento omitido por datos incompletos: {event.get('name')}")
                        continue

                    if not is_valid_date(end_date):
                        print(f"Fecha de fin inválida: {event.get('end_date')} para {event.get('name')}")
                        continue

                    # Verificar si el evento está finalizado
                    if end_date and end_date < now():
                        print(f"Evento finalizado, omitido: {event.get('name')}")
                        continue  # Si el evento ya está finalizado, se omite

                    # Si el evento no tiene start_date, asignar el valor de end_date como start_date
                    if not start_date:
                        start_date = end_date

                    # Validar fechas solo si start_date no es None (no es null)
                    if not is_valid_date(start_date):
                        print(f"Fecha de inicio inválida: {event.get('start_date')} para {event.get('name')}")
                        continue

                    if not is_valid_date(modified_date):
                        print(f"Fecha modificada inválida: {event.get('modified')} para {event.get('name')}")
                        continue

                    # Verificar si ya existe y si necesita actualización
                    existing_event = CulturalEvent.objects.filter(register_id=event['register_id']).first()

                    if existing_event:
                        # Si existe, comprobar si necesita actualización
                        if existing_event.modified >= modified_date:
                            print(f"Evento sin cambios: {existing_event.name}")
                            continue

                        print(f"Actualizando evento: {existing_event.name}")
                        existing_event.name = event.get('name')
                        existing_event.longitude = (
                            float(event['geo_epgs_4326_lon'])
                            if event.get('geo_epgs_4326_lon') else None
                        )
                        existing_event.latitude = (
                            float(event['geo_epgs_4326_lat'])
                            if event.get('geo_epgs_4326_lat') else None
                        )
                        existing_event.start_date = start_date
                        existing_event.end_date = end_date
                        existing_event.road_name = event.get('addresses_road_name')
                        existing_event.district = event.get('addresses_district_name')
                        existing_event.street_number = event.get('addresses_start_street_number')
                        existing_event.modified = modified_date
                        existing_event.save()
                    else:
                        # Si no existe, crear uno nuevo
                        CulturalEvent.objects.create(
                            register_id=event['register_id'],
                            name=event.get('name'),
                            longitude=float(event['geo_epgs_4326_lon']) if event.get('geo_epgs_4326_lon') else None,
                            latitude=float(event['geo_epgs_4326_lat']) if event.get('geo_epgs_4326_lat') else None,
                            start_date=start_date,
                            end_date=end_date,
                            road_name=event.get('addresses_road_name'),
                            district=event.get('addresses_district_name'),
                            street_number=event.get('addresses_start_street_number'),
                            modified=modified_date,
                        )
                        print(f"Evento creado: {event.get('name')}")
                except Exception as e:  # pylint: disable=broad-exception-caught
                    print(f"Error procesando evento: {event.get('name')}. Detalles: {e}")
    except requests.RequestException as e:
        print(f"Error al consumir la API: {e}")


def get_bearer_token(service_account_file):
    """
    Genera un Bearer Token usando las credenciales del servicio.
    """
    credentials, _ = google.auth.load_credentials_from_file(
        service_account_file, scopes=["https://www.googleapis.com/auth/cloud-platform"]
    )
    credentials.refresh(Request())
    return credentials.token

def send_fcm_notification(fcm_tokens, title, message_content, payload):
    """
    Envía notificaciones push a una lista de tokens FCM utilizando la API HTTP v1.
    """
    if not fcm_tokens:
        print("No hay tokens FCM disponibles.")
        return

    # Carga el Bearer Token
    SERVICE_ACCOUNT_FILE = './config/firebase-key.json'  # pylint: disable=invalid-name
    try:
        bearer_token = get_bearer_token(SERVICE_ACCOUNT_FILE)
    except Exception as e:  # pylint: disable=broad-exception-caught
        print(f"Error al obtener el Bearer Token: {e}")
        return

    # Define los encabezados
    headers = {
        "Authorization": f"Bearer {bearer_token}",
        "Content-Type": "application/json",
    }

    # Asegurarse de que todos los valores del payload sean cadenas y aplanar la estructura
    data_payload = {k: str(v) for k, v in payload.items()}  # Conversión a string

    # Define el mensaje
    url = "https://fcm.googleapis.com/v1/projects/pes-bcngo/messages:send"
    for token in fcm_tokens:
        message = {
            "message": {
                "token": token,
                "notification": {
                    "title": title,
                    "body": message_content,
                },
                "data": data_payload,  # Carga el payload aplanado
            }
        }

        try:
            response = requests.post(url, headers=headers, data=json.dumps(message))  # pylint: disable=missing-timeout
            if response.status_code == 200:
                print(f"Notificación enviada correctamente al token: {token}")
            else:
                print(f"Error al enviar la notificación al token {token}: {response.status_code} {response.text}")
        except requests.RequestException as e:
            print(f"Error de conexión al enviar la notificación al token {token}: {e}")
