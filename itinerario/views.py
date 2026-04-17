"""
Vistas para la aplicación de itinerarios.
"""

from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.views import APIView
from django.shortcuts import get_object_or_404
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
from puntosdeinteres.models import PointOfInterest
from .models import Itinerario, ItineraryItem
from .serializers import ItinerarioSerializer, ItineraryItemSerializer, ItinerarioListSerializer


class ItinerarioAutomaticoView(APIView):
    """
    Vista para crear itinerarios automáticamente.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Crea un itinerario automáticamente basado en los datos proporcionados.",
        request_body=ItinerarioSerializer,
        responses={
            201: ItinerarioListSerializer,
            400: "Errores de validación en los datos proporcionados."
        }
    )
    def post(self, request):
        """
        Maneja la creación de un itinerario automáticamente.
        """
        serializer = ItinerarioSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            itinerario = serializer.save()
            return Response(ItinerarioListSerializer(itinerario).data)
        return Response(serializer.errors, status=400)


@swagger_auto_schema(
    method='patch',
    operation_description="Actualiza el nombre de un itinerario específico.",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            "name": openapi.Schema(type=openapi.TYPE_STRING, description="Nuevo nombre del itinerario.")
        },
        required=["name"]
    ),
    responses={
        200: openapi.Response(
            description="Nombre del itinerario actualizado correctamente.",
            schema=openapi.Schema(
                type=openapi.TYPE_OBJECT,
                properties={
                    "message": openapi.Schema(type=openapi.TYPE_STRING, example="Itinerary name updated successfully."),
                    "name": openapi.Schema(type=openapi.TYPE_STRING, example="Nuevo Itinerario")
                }
            )
        ),
        400: openapi.Response(
            description="Nombre no proporcionado o inválido.",
            schema=openapi.Schema(
                type=openapi.TYPE_OBJECT,
                properties={
                    "error": openapi.Schema(type=openapi.TYPE_STRING, example="Name field is required.")
                }
            )
        ),
        404: "Itinerario no encontrado."
    }
)
@api_view(['PATCH'])
@permission_classes([IsAuthenticated])
def api_update_itinerary_name(request, pk):
    """
    Actualiza el nombre de un itinerario específico.
    """
    itinerary = get_object_or_404(Itinerario, pk=pk, user=request.user)
    new_name = request.data.get('name')
    if not new_name:
        return Response({"error": "Name field is required."}, status=status.HTTP_400_BAD_REQUEST)

    itinerary.name = new_name
    itinerary.save()

    return Response(
        {"message": "Itinerary name updated successfully.", "name": itinerary.name},
        status=status.HTTP_200_OK
    )


@swagger_auto_schema(
    method='put',
    operation_description="Actualiza puntos de interés para múltiples días en un itinerario específico.",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            "items": openapi.Schema(
                type=openapi.TYPE_ARRAY,
                items=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "day": openapi.Schema(
                            type=openapi.TYPE_INTEGER,
                            description="Día del itinerario que se desea actualizar."
                        ),
                        "points": openapi.Schema(
                            type=openapi.TYPE_ARRAY,
                            items=openapi.Schema(type=openapi.TYPE_STRING),
                            description="Lista de nombres de los puntos de interés para este día."
                        )
                    }
                )
            )
        },
        required=["items"]
    ),
    responses={
        200: openapi.Response(
            description="Puntos de interés actualizados exitosamente.",
            schema=ItineraryItemSerializer(many=True)
        ),
        400: "Errores en los datos proporcionados.",
        404: "Itinerario o puntos no encontrados."
    }
)
@api_view(['PUT'])
@permission_classes([IsAuthenticated])
def api_update_multiple_days_in_itinerary(request, pk):
    """
    Actualiza los puntos de interés para varios días en un itinerario.
    """
    itinerary = get_object_or_404(Itinerario, pk=pk, user=request.user)
    updates = request.data.get('items', [])

    if not updates:
        return Response({"error": "No updates provided."}, status=status.HTTP_400_BAD_REQUEST)

    for update in updates:
        day = update.get('day')
        if not day:
            continue  # Skip invalid entries

        itinerary_item = get_object_or_404(ItineraryItem, itinerary=itinerary, day=day)
        new_points_names = update.get('points', [])
        new_points = PointOfInterest.objects.filter(name__in=new_points_names) # pylint: disable=E1101

        if len(new_points) != len(new_points_names):
            missing = set(new_points_names) - set(new_points.values_list('name', flat=True))
            return Response(
                {"error": f"Some points were not found: {', '.join(missing)}"},
                status=status.HTTP_400_BAD_REQUEST
            )
        itinerary_item.points.set(new_points)
        itinerary_item.save()

    serializer = ItineraryItemSerializer(itinerary.items.all(), many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@swagger_auto_schema(
    method='put',
    operation_description="Actualiza puntos de interés para un día específico en un itinerario.",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            "points": openapi.Schema(
                type=openapi.TYPE_ARRAY,
                items=openapi.Schema(type=openapi.TYPE_INTEGER),
                description="Lista de IDs de los puntos de interés para este día."
            )
        },
        required=["points"]
    ),
    responses={
        200: openapi.Response(
            description="Puntos de interés actualizados exitosamente.",
            schema=ItineraryItemSerializer
        ),
        404: "Itinerario, día o puntos no encontrados."
    }
)
@api_view(['PUT'])
@permission_classes([IsAuthenticated])
def api_update_points_in_itinerary(request, pk, day):
    """
    Actualiza los puntos de interés para un día específico en un itinerario.
    """
    itinerary = get_object_or_404(Itinerario, pk=pk, user=request.user)
    itinerary_item = get_object_or_404(ItineraryItem, itinerary=itinerary, day=day)

    new_points_ids = request.data.get('points', [])
    new_points = PointOfInterest.objects.filter(pk__in=new_points_ids)
    itinerary_item.points.set(new_points)
    serializer = ItineraryItemSerializer(itinerary_item)
    return Response(serializer.data, status=status.HTTP_200_OK)


@swagger_auto_schema(
    method='get',
    operation_description="Obtiene los detalles de un itinerario específico.",
    responses={
        200: openapi.Response(
            description="Detalles del itinerario obtenidos exitosamente.",
            schema=ItinerarioListSerializer
        ),
        404: "Itinerario no encontrado."
    }
)
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def api_get_itinerary(request, pk):
    """
    Obtiene los detalles de un itinerario específico por su clave primaria (pk).
    """
    itinerary = get_object_or_404(Itinerario, pk=pk, user=request.user)
    serializer = ItinerarioListSerializer(itinerary)
    return Response(serializer.data, status=status.HTTP_200_OK)


@swagger_auto_schema(
    method='post',
    operation_description="Crea un nuevo itinerario para el usuario autenticado.",
    request_body=ItinerarioSerializer,
    responses={
        201: openapi.Response(
            description="Itinerario creado exitosamente.",
            schema=ItinerarioListSerializer
        ),
        400: "Errores de validación en los datos proporcionados."
    }
)
@api_view(['POST'])
@permission_classes([IsAuthenticated])
def api_crear_itinerario(request):
    """
    Crea un nuevo itinerario para el usuario autenticado.
    """
    serializer = ItinerarioSerializer(data=request.data, context={'request': request})
    if serializer.is_valid():
        serializer.save()
        list_serializer = ItinerarioListSerializer(serializer.instance)
        return Response(list_serializer.data, status=status.HTTP_201_CREATED)
    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@swagger_auto_schema(
    method='delete',
    operation_description="Elimina un itinerario específico del usuario actual.",
    responses={
        200: openapi.Response(
            description="Itinerario eliminado exitosamente.",
            schema=openapi.Schema(
                type=openapi.TYPE_OBJECT,
                properties={
                    "message": openapi.Schema(
                        type=openapi.TYPE_STRING,
                        example="The itinerary has been deleted successfully."
                    )
                }
            )
        ),
        404: "Itinerario no encontrado."
    }
)
@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
def api_borrar_itinerario(request, pk):
    """
    Elimina un itinerario específico del usuario actual.
    """
    itinerary = get_object_or_404(Itinerario, pk=pk, user=request.user)
    itinerary.delete()
    return Response({"message": "The itinerary has been deleted successfully."}, status=status.HTTP_200_OK)


@swagger_auto_schema(
    method='get',
    operation_description="Lista todos los itinerarios del usuario autenticado.",
    responses={
        200: openapi.Response(
            description="Lista de itinerarios obtenida exitosamente.",
            schema=ItinerarioListSerializer(many=True)
        )
    }
)
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def api_list_itinerario(request):
    """
    Lista todos los itinerarios del usuario actual.
    """
    itineraries = Itinerario.objects.filter(user=request.user)
    serializer = ItinerarioListSerializer(itineraries, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@swagger_auto_schema(
    method='get',
    operation_description="Obtiene los detalles de los elementos de un itinerario específico.",
    responses={
        200: openapi.Response(
            description="Elementos del itinerario obtenidos exitosamente.",
            schema=ItineraryItemSerializer(many=True)
        ),
        404: "Itinerario no encontrado."
    }
)
@api_view(['GET'])
@permission_classes([IsAuthenticated])
def api_get_itinerary_items(request, pk):
    """
    Recupera los detalles de los elementos de un itinerario específico.
    """
    itinerary = get_object_or_404(Itinerario, pk=pk, user=request.user)
    items = itinerary.items.all()
    serializer = ItineraryItemSerializer(items, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)
