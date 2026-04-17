# pylint: disable=no-member

"""
Este módulo define las vistas para la aplicación de Puntos de Interés.

Incluye vistas para listar todos los puntos de interés, obtener detalles
de un punto de interés por su ID y filtrar puntos de interés por categoría.
"""

from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
from django.http import JsonResponse  # pylint: disable=unused-import

from .services import (
    get_all_pointsofinterest,
    get_pointsofinterest_by_id,
)
from .models import PointOfInterest, Review
from .serializers import PointOfInterestSerializer, ReviewSerializer

class PointOfInterestApiView(APIView):
    """
    API View para listar todos los puntos de interés.
    """

    @swagger_auto_schema(
        operation_description="Obtiene una lista de todos los puntos de interés.",
        responses={
            200: PointOfInterestSerializer(many=True),
            500: "Error interno del servidor",
        },
    )
    def get(self, request):
        """
        Devuelve una lista de todos los puntos de interés.
        """
        try:
            # Llama a la función del servicio que obtiene todos los puntos de interés
            points_of_interest = get_all_pointsofinterest()
            # Serializa los datos
            serializer = PointOfInterestSerializer(points_of_interest, many=True)
            # Devuelve la respuesta con los datos serializados
            if request.accepted_renderer.media_type == 'text/html':
                return Response(serializer.data, status=status.HTTP_200_OK)

            return Response(
                serializer.data,
                status=status.HTTP_200_OK,
                content_type='application/json; charset=utf-8'
            )
        except Exception as e:  # pylint: disable=broad-exception-caught
            print(e)
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

class PointOfInterestDetailApiView(APIView):
    """
    API View para obtener detalles de un punto de interés por ID.
    """

    @swagger_auto_schema(
        operation_description="Obtiene un punto de interés por su ID.",
        manual_parameters=[
            openapi.Parameter(
                'point_of_interest_id',
                openapi.IN_PATH,
                description="ID del punto de interés",
                type=openapi.TYPE_INTEGER,
                required=True,
            )
        ],
        responses={
            200: PointOfInterestSerializer(),
            404: "Punto de interés no encontrado",
            500: "Error interno del servidor",
        },
    )
    def get(self, request, point_of_interest_id):
        """
        Obtiene los detalles de un punto de interés por su ID.
        """
        try:
            # Llama a la función del servicio que obtiene un punto de interés por id
            point_of_interest = get_pointsofinterest_by_id(point_of_interest_id)
            # Serializa los datos
            serializer = PointOfInterestSerializer(point_of_interest)
            # Devuelve la respuesta con los datos serializados
            if request.accepted_renderer.media_type == 'text/html':
                return Response(serializer.data, status=status.HTTP_200_OK)

            return Response(
                serializer.data,
                status=status.HTTP_200_OK,
                content_type='application/json; charset=utf-8'
            )

        except PointOfInterest.DoesNotExist:
            return Response(
                {"error": "Punto de interés no encontrado."},
                status=status.HTTP_404_NOT_FOUND,
            )
        except Exception as e:  # pylint: disable=broad-exception-caught
            print(e)
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

class ParksApiView(APIView):
    """
    API View para obtener los puntos de interés de categoría 'Parques'.
    """
    @swagger_auto_schema(
        operation_description="Obtiene una lista de puntos de interés cuya categoría es 'Parques'.",
        responses={
            200: PointOfInterestSerializer(many=True),
            500: "Error interno del servidor",
        },
    )
    def get(self, request):
        """
        Devuelve una lista de puntos de interés cuya categoría es 'Parques'.
        """
        try:
            # Filtrar los puntos de interés con categoría "Parques"
            parks = PointOfInterest.objects.filter(category="Parques")
            # Serializar los datos
            serializer = PointOfInterestSerializer(parks, many=True)

            # Manejar diferentes formatos de respuesta
            if request.accepted_renderer.media_type == 'text/html':
                return Response(serializer.data, status=status.HTTP_200_OK)

            return Response(
                serializer.data,
                status=status.HTTP_200_OK,
                content_type='application/json; charset=utf-8'
            )

        except Exception as e:  # pylint: disable=broad-exception-caught
            print(e)
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class CreatePointOfInterestApiView(APIView):
    """
    API View para crear un nuevo punto de interés.
    Solo los administradores pueden acceder.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Crea un nuevo punto de interés. Solo para administradores.",
        request_body=PointOfInterestSerializer,
        responses={
            201: PointOfInterestSerializer(),
            400: "Error en los datos enviados o el punto de interés ya existe.",
            403: "No tienes permisos para realizar esta acción.",
        },
    )
    def post(self, request):
        """
        Maneja la creación de un nuevo punto de interés.
        """
        # Verificar que el usuario es administrador
        if not request.user.is_staff:
            return Response(
                {"error": "No tienes permisos para realizar esta acción."},
                status=status.HTTP_403_FORBIDDEN
            )

        serializer = PointOfInterestSerializer(data=request.data)
        if serializer.is_valid():
            # Verificar duplicados
            name = serializer.validated_data.get("name")
            address_name = serializer.validated_data.get("address_name")
            if PointOfInterest.objects.filter(name=name, address_name=address_name).exists():
                return Response(
                    {"error": "Ya existe un punto de interés con el mismo nombre y ubicación."},
                    status=status.HTTP_400_BAD_REQUEST
                )

            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class DeletePointOfInterestApiView(APIView):
    """
    API View para eliminar un punto de interés.
    """
    permission_classes = [IsAuthenticated]  # Requiere que el usuario esté autenticado

    @swagger_auto_schema(
        operation_description="Elimina un punto de interés por su ID.",
        manual_parameters=[
            openapi.Parameter(
                'point_of_interest_id',
                openapi.IN_PATH,
                description="ID del punto de interés a eliminar",
                type=openapi.TYPE_INTEGER,
                required=True,
            )
        ],
        responses={
            204: "Punto de interés eliminado correctamente.",
            401: "Autenticación requerida para acceder a este endpoint.",
            403: "No tienes permisos para realizar esta acción.",
            404: "Punto de interés no encontrado.",
        },
    )
    def delete(self, request, point_of_interest_id):
        """
        Maneja solicitudes DELETE para eliminar un punto de interés.
        """
        # Verificar si el usuario es administrador
        if not request.user.is_staff:
            return Response(
                {"error": "No tienes permisos para realizar esta acción."},
                status=status.HTTP_403_FORBIDDEN,
            )

        try:
            # Buscar el punto de interés por ID
            point_of_interest = PointOfInterest.objects.get(id=point_of_interest_id)
            # Eliminar el punto de interés
            point_of_interest.delete()
            return Response(
                {"message": "Punto de interés eliminado correctamente."},
                status=status.HTTP_204_NO_CONTENT,
            )
        except PointOfInterest.DoesNotExist:
            return Response(
                {"error": "Punto de interés no encontrado."},
                status=status.HTTP_404_NOT_FOUND,
            )


class CreateReviewApiView(APIView):
    """
    API View para crear una nueva reseña para un punto de interés.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Crea una nueva reseña asociada a un punto de interés.",
        request_body=ReviewSerializer,
        responses={
            201: openapi.Response(
                description="Reseña creada exitosamente.",
                schema=ReviewSerializer
            ),
            400: "Error en los datos enviados.",
            404: "Punto de interés no encontrado.",
        },
    )
    def post(self, request):
        """
        Maneja la creación de una nueva reseña.
        """
        serializer = ReviewSerializer(data=request.data)
        if serializer.is_valid():
            try:
                point_of_interest = PointOfInterest.objects.get(id=serializer.validated_data['point_of_interest'].id)
                serializer.save(
                    point_of_interest=point_of_interest,
                    username=request.user.username
                )
                return Response(serializer.data, status=status.HTTP_201_CREATED)
            except PointOfInterest.DoesNotExist:
                return Response(
                    {"error": "Punto de interés no encontrado."},
                    status=status.HTTP_404_NOT_FOUND,
                )
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class DeleteReviewApiView(APIView):
    """
    API View para eliminar una reseña.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Elimina una reseña por su ID.",
        manual_parameters=[
            openapi.Parameter(
                'review_id',
                openapi.IN_PATH,
                description="ID de la reseña a eliminar",
                type=openapi.TYPE_INTEGER,
                required=True,
            )
        ],
        responses={
            204: "Reseña eliminada correctamente.",
            403: "No tienes permisos para eliminar esta reseña.",
            404: "Reseña no encontrada.",
        },
    )
    def delete(self, request, review_id):
        """
        Maneja la eliminación de una reseña.
        """
        try:
            review = Review.objects.get(id=review_id)

            # Los administradores pueden eliminar cualquier reseña
            if request.user.is_staff or review.user_email == request.user.email:
                review.delete()
                return Response(
                    {"message": "Reseña eliminada correctamente."},
                    status=status.HTTP_204_NO_CONTENT
                )

            # Si no es administrador ni autor, no tiene permisos
            return Response(
                {"error": "No tienes permisos para eliminar esta reseña."},
                status=status.HTTP_403_FORBIDDEN
            )
        except Review.DoesNotExist:
            return Response(
                {"error": "Reseña no encontrada."},
                status=status.HTTP_404_NOT_FOUND
            )


class ListReviewsByPointApiView(APIView):
    """
    API View para listar todas las reseñas de un punto de interés.
    """
    @swagger_auto_schema(
        operation_description="Devuelve todas las reseñas de un punto de interés.",
        manual_parameters=[
            openapi.Parameter(
                'point_of_interest_id',
                openapi.IN_PATH,
                description="ID del punto de interés",
                type=openapi.TYPE_INTEGER,
                required=True,
            )
        ],
        responses={
            200: ReviewSerializer(many=True),
            404: "Punto de interés no encontrado.",
        },
    )
    def get(self, request, point_of_interest_id): # pylint: disable=unused-argument
        """
        Maneja solicitudes GET para listar las reseñas de un punto de interés.
        """
        try:
            point_of_interest = PointOfInterest.objects.get(id=point_of_interest_id)
            reviews = point_of_interest.reviews.all()
            serializer = ReviewSerializer(reviews, many=True)
            return Response(serializer.data, status=status.HTTP_200_OK)
        except PointOfInterest.DoesNotExist:
            return Response(
                {"error": "Punto de interés no encontrado."},
                status=status.HTTP_404_NOT_FOUND,
            )


class ReportReviewApiView(APIView):
    """
    API View para reportar una reseña.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Reporta una reseña incrementando su contador de reportes.",
        manual_parameters=[
            openapi.Parameter(
                'review_id',
                openapi.IN_PATH,
                description="ID de la reseña a reportar",
                type=openapi.TYPE_INTEGER,
                required=True,
            )
        ],
        responses={
            200: "Reseña reportada correctamente.",
            404: "Reseña no encontrada.",
        },
    )
    def post(self, request, review_id): # pylint: disable=unused-argument
        """
        Maneja la acción de reportar una reseña.
        """
        try:
            review = Review.objects.get(id=review_id)
            review.reports_count += 1
            review.save()
            return Response({"message": "Reseña reportada correctamente."}, status=status.HTTP_200_OK)
        except Review.DoesNotExist:
            return Response(
                {"error": "Reseña no encontrada."},
                status=status.HTTP_404_NOT_FOUND,
            )


class ListReportedReviewsApiView(APIView):
    """
    API View para listar todas las reseñas reportadas.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Devuelve una lista de todas las reseñas reportadas (reports_count >= 1).",
        responses={
            200: openapi.Response(
                description="Lista de reseñas reportadas obtenida con éxito.",
                schema=ReviewSerializer(many=True)
            ),
            403: "No tienes permisos para realizar esta acción.",
        },
    )
    def get(self, request):
        """
        Maneja solicitudes GET para listar las reseñas reportadas.
        """
        if not request.user.is_staff:
            return Response(
                {"error": "No tienes permisos para realizar esta acción."},
                status=status.HTTP_403_FORBIDDEN,
            )

        reported_reviews = Review.objects.filter(reports_count__gte=1)
        serializer = ReviewSerializer(reported_reviews, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)
