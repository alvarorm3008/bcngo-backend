"""
Vistas para la aplicación Servicio.

Incluye la vista para obtener paradas cercanas utilizando un servicio externo.
"""

from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from rest_framework.exceptions import ValidationError
import requests
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

class NearbyStopsView(APIView):
    """
    Vista para obtener las paradas cercanas a una ubicación dada.

    Requiere que el usuario esté autenticado.
    """
    permission_classes = [IsAuthenticated]  # Solo usuarios autenticados

    @swagger_auto_schema(
        operation_description="Obtiene las paradas cercanas a una ubicación especificada.",
        manual_parameters=[
            openapi.Parameter(
                'latitud',
                openapi.IN_QUERY,
                description="Latitud de la ubicación inicial.",
                type=openapi.TYPE_STRING,
                required=True
            ),
            openapi.Parameter(
                'longitud',
                openapi.IN_QUERY,
                description="Longitud de la ubicación inicial.",
                type=openapi.TYPE_STRING,
                required=True
            ),
            openapi.Parameter(
                'distancia',
                openapi.IN_QUERY,
                description="Distancia máxima en metros para buscar paradas cercanas.",
                type=openapi.TYPE_STRING,
                required=True
            ),
        ],
        responses={
            200: openapi.Response(
                description="Lista de paradas cercanas obtenida con éxito.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "stops": openapi.Schema(
                            type=openapi.TYPE_ARRAY,
                            items=openapi.Schema(type=openapi.TYPE_OBJECT),
                            description="Lista de paradas cercanas."
                        )
                    }
                )
            ),
            400: "Error en los parámetros proporcionados.",
            500: "Error en la conexión con el servicio externo o error del servidor."
        }
    )
    def get(self, request):
        """
        Maneja solicitudes GET para obtener paradas cercanas.

        Se esperan los parámetros 'latitud', 'longitud' y 'distancia' en el query string.
        """
        # Obtener los parámetros desde el query string
        latitud = request.query_params.get('latitud')
        longitud = request.query_params.get('longitud')
        distancia = request.query_params.get('distancia')

        # Validar parámetros
        if not (latitud and longitud and distancia):
            raise ValidationError("Los parámetros 'latitud', 'longitud' y 'distancia' son obligatorios.")

        # Hacer la solicitud al servicio externo
        url = 'https://bcnlink-5496869b9083.herokuapp.com/transportproper'
        headers = {
            'latitud': latitud,
            'longitud': longitud,
            'distancia': distancia,
        }
        try:
            response = requests.get(url, headers=headers, timeout=10)  # Añadido timeout
            response.raise_for_status()  # Maneja errores HTTP

            # Opcional: Formatear la respuesta
            return Response(response.json())
        except requests.exceptions.RequestException as e:
            return Response({'error': str(e)}, status=500)
