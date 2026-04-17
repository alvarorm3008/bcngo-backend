"""
Vistas de la aplicación de Pasaporte Digital.
"""

from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
from puntosdeinteres.models import PointOfInterest
from .models import Passport, PassportPoint
from .serializers import PassportSerializer  # pylint: disable=unused-import


class PassportDetailView(APIView):
    """
    Obtiene el pasaporte del usuario autenticado.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Consulta el pasaporte del usuario autenticado.",
        responses={
            200: openapi.Response(
                description="Pasaporte recuperado con éxito.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "message": openapi.Schema(type=openapi.TYPE_STRING, example="Pasaporte recuperado con éxito."),
                        "data": openapi.Schema(
                            type=openapi.TYPE_ARRAY,
                            items=openapi.Schema(
                                type=openapi.TYPE_OBJECT,
                                properties={
                                    "point_of_interest": openapi.Schema(
                                        type=openapi.TYPE_OBJECT,
                                        properties={
                                            "id": openapi.Schema(type=openapi.TYPE_INTEGER, example=1),
                                            "name": openapi.Schema(type=openapi.TYPE_STRING, example="Museo Nacional"),
                                            "category": openapi.Schema(type=openapi.TYPE_STRING, example="Museos"),
                                            "latitude": openapi.Schema(
                                                type=openapi.TYPE_NUMBER, format="float", example=41.3851),
                                            "longitude": openapi.Schema(
                                                type=openapi.TYPE_NUMBER, format="float", example=2.1734),
                                        }
                                    ),
                                    "is_marked": openapi.Schema(type=openapi.TYPE_BOOLEAN, example=True),
                                }
                            )
                        ),
                    }
                )
            ),
            404: "No se encontró un pasaporte para este usuario.",
        }
    )
    def get(self, request):
        """
        Devuelve el pasaporte del usuario autenticado con todos los puntos
        de interés y su estado (marcado o no).
        """
        user = request.user
        try:
            passport = Passport.objects.get(user=user)
        except Passport.DoesNotExist:
            return Response(
                {"error": "No se encontró un pasaporte para este usuario."},
                status=status.HTTP_404_NOT_FOUND
            )

        passport_points = PassportPoint.objects.filter(passport=passport)
        data = [
            {
                "point_of_interest": {
                    "id": point.point_of_interest.id,
                    "name": point.point_of_interest.name,
                    "category": point.point_of_interest.category,
                    "latitude": point.point_of_interest.latitude,
                    "longitude": point.point_of_interest.longitude,
                },
                "is_marked": point.is_marked
            }
            for point in passport_points
        ]
        return Response(
            {"message": "Pasaporte recuperado con éxito.", "data": data},
            status=status.HTTP_200_OK,
        )


class MarkPointView(APIView):
    """
    Marca un punto de interés en el pasaporte.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Marca un punto de interés en el pasaporte del usuario autenticado.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                'point_id': openapi.Schema(type=openapi.TYPE_INTEGER, description="ID del punto de interés"),
            },
            required=['point_id']
        ),
        responses={
            201: openapi.Response(
                description="Punto de interés marcado con éxito.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "message": openapi.Schema(
                            type=openapi.TYPE_STRING, example="Punto de interés marcado con éxito.")
                    }
                )
            ),
            400: openapi.Response(
                description="Error en la solicitud.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "error": openapi.Schema(
                            type=openapi.TYPE_STRING, example="Debe proporcionar el ID del punto de interés.")
                    }
                )
            ),
            404: openapi.Response(
                description="Punto de interés no encontrado.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "error": openapi.Schema(
                            type=openapi.TYPE_STRING, example="Punto de interés con ID 5 no encontrado.")
                    }
                )
            ),
        }
    )
    def post(self, request):
        """
        Marca un punto de interés como visitado en el pasaporte del usuario autenticado.
        """
        user = request.user
        point_id = request.data.get("point_id")

        if not point_id:
            return Response(
                {"error": "Debe proporcionar el ID del punto de interés."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            point_of_interest = PointOfInterest.objects.get(id=point_id)
        except PointOfInterest.DoesNotExist:
            return Response(
                {"error": f"Punto de interés con ID {point_id} no encontrado."},
                status=status.HTTP_404_NOT_FOUND,
            )

        passport = Passport.objects.get(user=user)

        try:
            passport_point = PassportPoint.objects.get(passport=passport, point_of_interest=point_of_interest)
            if passport_point.is_marked:
                return Response(
                    {"message": "El punto de interés ya está marcado en el pasaporte."},
                    status=status.HTTP_200_OK,
                )
            passport_point.is_marked = True
            passport_point.save()
        except PassportPoint.DoesNotExist:
            return Response(
                {"error": f"El punto con ID {point_id} no es un punto esencial asignado al pasaporte."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        return Response(
            {"message": "Punto de interés marcado con éxito."},
            status=status.HTTP_201_CREATED,
        )
