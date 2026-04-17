"""
Módulo de servicios para gestionar puntos de interés.

Incluye funciones para obtener, filtrar y guardar puntos de interés
desde APIs externas, así como para gestionar la base de datos.
"""

import requests
from django.db import connection
from django.utils.dateparse import parse_datetime
from django.utils.timezone import make_aware, is_aware, utc  # pylint: disable=no-name-in-module
from puntosdeinteres.models import PointOfInterest

# Función para obtener los datos desde una API dada
def fetch_data_from_api(url):
    """
    Obtiene datos desde una API externa dada una URL.
    """
    response = requests.get(url, timeout=10)
    if response.status_code == 200:
        data = response.json()
        if isinstance(data, list):
            return data
        return data.get("result", {}).get("records", [])
    return []

# Función para descartar los datos que no sean relevantes
def filter_data(name):
    """
    Filtra datos para descartar registros irrelevantes.
    """

     # Excepción para "Edifici de l'Antic Hospital de la Santa Creu"
    if "Edifici de l'Antic Hospital de la Santa Creu" in name:
        return True  # No excluir este registro específico

    # Excepción para "Parc Esportiu de Can Dragó"
    if "Parc Esportiu de Can Dragó" in name:
        return True  # Excluir este registro específico

    # Excepción para "Estació de França"
    if "Estació de França" in name:
        return True  # Excluir este registro específico

    # Lista de palabras clave a excluir
    keywords_to_exclude = [
        "Institut", "Escola", "Universitat", "Facultat", "Farmàcia", "Restaurant",
        "Hospital", "Casal", "Col·legi", "Alberg", "Forn", "Fàbrica", "Bar ",
        "Esportiu", "Campus", "barri", "Cooperativa", "Centre Educatiu", "Club",
        "Fundació", "Clínica", "Associació", "Camiseria", "Colònia", "Pavelló",
        "Centre Cívic", "Unió", "Carrer ", "Ca ", "Velodrom", "Seu ", "Estació",
        "Finca", "Cristalleries", "Habitatge", "Centre Municipal", "Villa ",
        "Residència ", "Pastisseria", "Vivendes", "Religioses", "Centre cultural",
        "Herboristeria", "Esplai", "Cereria", "Conjunt", "Centre penitenciari",
        "Centre Estudis", "Túnel", "Oratori", "Masia", "Centre Universitari",
        "Dipòsit", "Avinguda", "Passatge", "carrer", " Bombers", "Metro "
    ]

    # Asegurarse de que name sea un string y limpiar espacios en blanco
    name = str(name)

    # Verificar si alguna de las palabras clave está en el nombre
    if any(keyword in name for keyword in keywords_to_exclude):
        return False  # Si encuentra alguna de las palabras clave, descarta el registro

    return True  # Si no contiene ninguna de las palabras clave, lo acepta


# Función para asignar una categoría en función de palabras clave
def asignar_categoria(name):
    """
    Asigna una categoría a un punto de interés según palabras clave.
    """
    keywords_to_category = {
        "Parques": ["Parc", "Park", "Jardins", "Jardí", "Jardinets", "Hort", "Mirador"],
        "Arquitectura": [
            "Temple", "Palau", "Basílica", "Edifici", "Casa", "Cases", "Can",
            "Parròquia", "Biblioteca", "Plaça", "Camí", "Passeig", "Avinguda",
            "Carrer", "Monestir", "Santuari", "Castell", "Hotel", "Torre", "Molí",
            "Pont", "Memorial", "Xemeneia", "Auditori", "Porta", "Ermita",
            "Viaducte", "Capella", "Far", "Aqüaducte", "Estació"
        ],
        "Arte": ["Teatre", "Escultura", "Monument", "Font", "Ateneu", "Mural"],
        "Compras": ["Mercat", "Eix", "Associació", "Centre Comercial", "Drogueria", "Bodega"],
        "Museos": ["Museu", "Galeria", "Exposició"]
    }
    # Asegurarse de que name sea un string y limpiar espacios en blanco
    name = str(name)

    # Revisar si el nombre contiene alguna de las palabras clave
    for category, keywords in keywords_to_category.items():
        for keyword in keywords:
            if keyword in name:
                return category  # Retornar la categoría correspondiente
    # Si no coincide con ninguna palabra clave, asignar "Ocio" como categoría por defect
    return "Ocio"


# Función para procesar los registros y extraer la información relevante
def process_records(records, category):
    """
    Procesa y filtra registros obtenidos de la API.
    """
    filtered_data = []

    for record in records:

        name = record.get("name")
        register_id = record.get("register_id")
        modified_date = record.get("modified")  # Fecha de la última modificación

        # Filtrar y clasificar los puntos de interés cultural
        if category != "OcioNocturno":
            category = None  # Asegurarse de que category se inicialice aquí
            if not filter_data(name):
                print("descartado " + name)
                # Si el nombre contiene palabras clave excluidas, pasa al siguiente registro
                continue

            category = asignar_categoria(name)

        geo_data = record.get("geo_epgs_4326_latlon") or {}
        phone = None
        web_url = None

        # Acceder a las categorías de atributos
        for category_data in record.get("attribute_categories", []):
            for attribute in category_data.get("attributes", []):
                for value in attribute.get("values", []):
                    if attribute.get("name") == "Tel.":
                        phone = value.get("value")  # Obtener el número de teléfono
                    elif attribute.get("name") == "Web":
                        web_url = value.get("value")  # Obtener la URL de la web

        filtered_record = {
            "name": name,
            "register_id": register_id,
            "modified": modified_date,
            "address_name": record.get("addresses", [{}])[0].get("address_name"),
            "start_street_number": record.get("addresses", [{}])[0].get("start_street_number"),
            "latitude": geo_data.get("lat"),
            "longitude": geo_data.get("lon"),
            "phone": phone,
            "web_url": web_url,
            "category": category  

        }

        filtered_data.append(filtered_record)

    return filtered_data

def es_esencial(name):
    """
    Determina si un punto de interés es esencial.
    """
    keywords_esentials = [
        "Temple Expiatori de la Sagrada Família - Basílica",
        "Park Güell", "Casa Batlló", "Monument Arc de Triomf de Barcelona",
        "Espai Gaudí: Pis, Golfes i Terrat", "Parc de la Ciutadella",
        "Santa Església Catedral Basílica de Barcelona", "Castell de Montjuïc",
        "Mercat Boqueria - Sant Josep", "La Rambla",
        "La Plaça de Catalunya", "El Gòtic",
        "Parc del Laberint d'Horta", "Poble Espanyol de Barcelona",
        "La plaça d'Espanya", "CosmoCaixa Barcelona",
        "L'Anella Olímpica", "Platja de la Barceloneta", "Hotel W Barcelona - HB-004411 *Edifici Vela",
        "Museu Nacional d'Art de Catalunya", "Museu d'Art Contemporani de Barcelona",
        "Centre Comercial L'Illa Diagonal", "Spotify Camp Nou * Tancat per remodelació",
        "El Portal de l'Àngel", "El Passeig de Gràcia", "Teatre Nacional de Catalunya",
        "Monument a Cristòfol Colom", "Sant Pau Recinte Modernista",
        "Palau de la Música Catalana", "Parròquia de Santa Maria del Mar - Basílica",
        "Moll de la Fusta"
    ]

    # Verificar si el nombre está en la lista de esenciales
    return name in keywords_esentials


# Función para vaciar la tabla y reiniciar la secuencia de IDs
def clear_pointsofinterest_table():
    """
    Vacía la tabla de puntos de interés y reinicia la secuencia de IDs.
    """
    print("Vaciando la tabla pointofinterest...")

    # Borrar todos los registros
    PointOfInterest.objects.all().delete()  # pylint: disable=no-member

    # Reiniciar la secuencia de IDs
    with connection.cursor() as cursor:
        cursor.execute("ALTER SEQUENCE puntosdeinteres_pointofinterest_id_seq RESTART WITH 1;")

    print("Tabla vaciada y secuencia de IDs reiniciada.")


def convert_to_aware(datetime_obj):
    """Convierte una fecha naive a aware, si es necesario."""
    if datetime_obj and not is_aware(datetime_obj):
        return make_aware(datetime_obj, utc)
    return datetime_obj

# Función principal para guardar los datos en la base de datos

def sync_points_of_interest_from_api():
    """
    Guarda los puntos de interés obtenidos de APIs externas en la base de datos.
    """

    print("Obteniendo datos de la API OpenData Barcelona...")
    # URLs de las APIs
    url_culturalpoints = (
        "https://opendata-ajuntament.barcelona.cat/data/dataset/462e7ea8-aa84-4892-b93f-3bc9ab8e5b4"
        "b/resource/0043bdda-0143-46c3-be64-d35cbc3a86f6/download"
    )
    url_ocionoche = (
        "https://opendata-ajuntament.barcelona.cat/data/dataset/4b121b7e-c956-4fb0-bfd7-0ebc617e664"
        "3/resource/45e7d07c-7d14-4ef3-aa13-55add55d9a2c/download"
    )
    try:
        # Fetch data from both APIs
        records_culturalpoints = fetch_data_from_api(url_culturalpoints)
        records_ocionoche = fetch_data_from_api(url_ocionoche)

        # Procesar los registros
        filtered_data_culturalpoints = process_records(records_culturalpoints, category=None)
        filtered_data_ocionoche = process_records(records_ocionoche, category="OcioNocturno")

        # Guardar o actualizar cada punto de interés en la base de datos
        for data in filtered_data_culturalpoints + filtered_data_ocionoche:
            register_id = data["register_id"]
            # Convertir la fecha de modificación a un objeto datetime
            modified_date = convert_to_aware(parse_datetime(data["modified"]))

            # Buscar el punto de interés por su ID
            point_of_interest = PointOfInterest.objects.filter(register_id=register_id).first()

            if point_of_interest:
                # Si el punto ya existe, comparar las fechas de modificación
                if point_of_interest.modified < modified_date:
                    # Si la fecha de modificación del registro es más reciente, actualizar
                    print(f"Actualizando: {data['name']}")
                    point_of_interest.name = data["name"]
                    point_of_interest.address_name = data["address_name"]
                    point_of_interest.street_number = data["start_street_number"]
                    point_of_interest.latitude = data["latitude"]
                    point_of_interest.longitude = data["longitude"]
                    point_of_interest.phone = data["phone"]
                    point_of_interest.web_url = data["web_url"]
                    point_of_interest.category = data["category"]
                    point_of_interest.esencial = es_esencial(data["name"])  # Determinar si es esencial
                    point_of_interest.modified = modified_date  # Actualizar la fecha de modificación
                    point_of_interest.save()
            else:
                # Si el punto no existe, crearlo
                print(f"Creando: {data['name']}")
                PointOfInterest.objects.create(
                    register_id=register_id,
                    name=data["name"],
                    address_name=data["address_name"],
                    street_number=data["start_street_number"],
                    latitude=data["latitude"],
                    longitude=data["longitude"],
                    phone=data["phone"],
                    web_url=data["web_url"],
                    category=data["category"],
                    esencial=es_esencial(data["name"]),  # Determinar si es esencial
                    modified=modified_date  # Establecer la fecha de modificación
                )

        print("Datos guardados o actualizados en la base de datos.")
    except Exception as e:  # pylint: disable=broad-exception-caught
        print(f"Error al guardar los datos: {e}")



    # Función para obtener todos los puntos de interés
def get_all_pointsofinterest():
    """
    Obtiene todos los puntos de interés de la base de datos.
    """
    return PointOfInterest.objects.all()  # pylint: disable=no-member

def get_pointsofinterest_by_id(poi_id):
    """
    Obtiene un punto de interés por su ID.
    """
    return PointOfInterest.objects.get(id=poi_id)  # pylint: disable=no-member

    # Función para obtener un punto de interés por nombre
def get_pointsofinterest_by_name(name):
    """
    Obtiene un punto de interés por su nombre.
    """
    return PointOfInterest.objects.get(name=name)  # pylint: disable=no-member

    # Función para obtener los puntos de interés por categoría
def get_pointsofinterest_by_category(category):
    """
    Obtiene puntos de interés por categoría.
    """
    return PointOfInterest.objects.filter(category=category)  # pylint: disable=no-member

    # Función para obtener los puntos de interés esenciales
def get_pointsofinterest_by_esencial():
    """
    Obtiene puntos de interés esenciales.
    """
    return PointOfInterest.objects.filter( esencial=True)  # pylint: disable=no-member
