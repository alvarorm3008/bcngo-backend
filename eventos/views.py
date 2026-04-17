"""
Vistas relacionadas con eventos culturales, favoritos, chats grupales y gestión de mensajes.
"""

from datetime import datetime, timedelta  # pylint: disable=unused-import

from django.http import JsonResponse  # pylint: disable=unused-import
from django.utils.timezone import now
from django.utils.dateparse import parse_datetime
from django.utils import timezone
from django.db.models import Q, Prefetch
from django.shortcuts import get_object_or_404
from rest_framework.response import Response
from rest_framework import status, generics
from rest_framework.views import APIView
from rest_framework.pagination import PageNumberPagination
from rest_framework.permissions import IsAuthenticated
from rest_framework.exceptions import ValidationError
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi  # pylint: disable=unused-import

from .models import CulturalEvent, FavoriteEvent, EventGroupChat, Participant, Message
from .serializers import (
    CulturalEventSerializer, FavoriteEventSerializer, MessageCreateSerializer,
    MessageSerializer, EventGroupChatSerializer, MessageReportedSerializer
)
from .services import send_fcm_notification

# pylint: disable=too-many-lines


class CulturalEventListView(APIView):
    """
    Vista para listar los eventos culturales disponibles, incluyendo información sobre si son 
    favoritos del usuario autenticado.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Obtiene una lista de eventos culturales ordenados por fecha de inicio.",
        responses={
            200: CulturalEventSerializer(many=True),
            403: "No tienes permisos para realizar esta acción.",
        }
    )

    def get(self, request, format=None):  # pylint: disable=unused-argument, redefined-builtin
        """
        Devuelve una lista de eventos culturales ordenados por fecha de inicio.
        """
        # Obtener todos los eventos
        events = CulturalEvent.objects.all().order_by('start_date')

        # Prefetch los eventos favoritos del usuario autenticado
        user_favorites = FavoriteEvent.objects.filter(user=request.user)

        # Prefetch los eventos favoritos
        events = events.prefetch_related(
            Prefetch('favorited_by', queryset=user_favorites, to_attr='user_favorites')
        )

        # Serializar los eventos con el contexto del usuario autenticado
        serializer = CulturalEventSerializer(events, many=True, context={'request': request})

        # Retornar la respuesta con los eventos serializados
        return Response(serializer.data, status=status.HTTP_200_OK)


class CurrentEventsView(APIView):
    """
    Vista para obtener los eventos que están en curso en el momento actual.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Obtiene los eventos que están en curso en el momento actual.",
        responses={
            200: CulturalEventSerializer(many=True),
            403: "No tienes permisos para realizar esta acción.",
        }
    )

    def get(self, request):
        """
        Devuelve los eventos en curso basados en la hora actual.
        """
        # Obtener la fecha y hora actuales
        current_time = now()

        # Filtrar los eventos que están en curso
        # Si no tienen start_date, considerar que la fecha de inicio es igual a la fecha de fin
        current_events = CulturalEvent.objects.filter(
            Q(start_date__lte=current_time, end_date__gte=current_time)  # Evento en curso
        ).order_by('-start_date')

        # Prefetch los favoritos del usuario autenticado
        user_favorites = FavoriteEvent.objects.filter(user=request.user)
        current_events = current_events.prefetch_related(
            Prefetch('favorited_by', queryset=user_favorites, to_attr='user_favorites')
        )

        # Serializar los datos con el contexto del usuario autenticado
        serializer = CulturalEventSerializer(current_events, many=True, context={'request': request})

        # Devolver los eventos en formato JSON
        return Response(serializer.data)


class EventsByDayView(APIView):
    """
    Vista para obtener los eventos que ocurren en un día específico.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Obtiene los eventos que ocurren en un día específico.",
        manual_parameters=[
            openapi.Parameter(
                'day',
                openapi.IN_PATH,
                description="Fecha en formato YYYY-MM-DD",
                type=openapi.TYPE_STRING,
                required=True
            )
        ],
        responses={
            200: CulturalEventSerializer(many=True),
            400: "Invalid date format",
            404: "No events found"
        }
    )
    def get(self, request, day):
        """
        Devuelve los eventos que ocurren en un día dado.
        """
        # Intentar convertir el parámetro de fecha a un objeto datetime
        try:
            day_date = parse_datetime(day)  # Se espera un formato YYYY-MM-DD
        except Exception as e:
            raise ValidationError(f"Formato de fecha inválido. Error: {e}")  # pylint: disable=raise-missing-from

        # Asegurarse de que la fecha se haya convertido correctamente
        if not day_date:
            raise ValidationError("La fecha proporcionada es inválida.")

        # Convertir a una fecha 'aware' (si el soporte de zonas horarias está activo)
        if timezone.is_naive(day_date):
            day_date = timezone.make_aware(day_date, timezone.get_current_timezone())

        # Obtener los eventos del día solicitado
        events_on_day = CulturalEvent.objects.filter(
            Q(start_date__lte=day_date, end_date__gte=day_date) |  # Evento que ocurre en este día
            Q(start_date__date=day_date.date(), end_date__date=day_date.date())  # Si las fechas son el mismo día
        ).order_by('-start_date')

        # Prefetch los favoritos del usuario autenticado
        user_favorites = FavoriteEvent.objects.filter(user=request.user)
        events_on_day = events_on_day.prefetch_related(
            Prefetch('favorited_by', queryset=user_favorites, to_attr='user_favorites')
        )

        # Serializar los eventos con el contexto del usuario autenticado
        serializer = CulturalEventSerializer(events_on_day, many=True, context={'request': request})

        # Devolver la respuesta en formato JSON
        return Response(serializer.data, status=status.HTTP_200_OK)


class EventsOfMonthView(APIView):
    """
    Vista para obtener los eventos que ocurren dentro del mes actual.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Obtiene los eventos que ocurren dentro del mes actual.",
        responses={
            200: CulturalEventSerializer(many=True),
            403: "No tienes permisos para realizar esta acción.",
        }
    )
    def get(self, request):
        """
        Devuelve los eventos que ocurren durante el mes actual.
        """
        # Obtener la fecha actual (aware)
        current_time = now()

        # Obtener el primer y último día del mes actual
        first_day_of_month = current_time.replace(day=1)
        if current_time.month == 12:
            last_day_of_month = current_time.replace(year=current_time.year + 1, month=1, day=1) - timedelta(days=1)
        else:
            last_day_of_month = current_time.replace(month=current_time.month + 1, day=1) - timedelta(days=1)

        # Filtrar los eventos que ocurren dentro del mes actual
        events_this_month = CulturalEvent.objects.filter(
            Q(start_date__gte=first_day_of_month, start_date__lte=last_day_of_month) |  # Start_date dentro del mes
            Q(end_date__gte=first_day_of_month, end_date__lte=last_day_of_month) |  # Evento con end_date dentro del mes
            Q(start_date__lte=last_day_of_month, end_date__gte=first_day_of_month)  # Evento que abarca el mes
        ).order_by('start_date')

        # Prefetch los favoritos del usuario autenticado
        user_favorites = FavoriteEvent.objects.filter(user=request.user)
        events_this_month = events_this_month.prefetch_related(
            Prefetch('favorited_by', queryset=user_favorites, to_attr='user_favorites')
        )

        # Serializar los eventos con el contexto del usuario autenticado
        serializer = CulturalEventSerializer(events_this_month, many=True, context={'request': request})

        # Devolver los eventos en formato JSON
        return Response(serializer.data)


class EventsOfWeekView(APIView):
    """
    Vista para obtener los eventos que ocurren durante la semana actual.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Obtiene los eventos que ocurren durante la semana en curso.",
        responses={
            200: CulturalEventSerializer(many=True),
            404: "No events found"
        }
    )

    def get(self, request):
        """
        Devuelve los eventos que ocurren durante la semana en curso.
        """
        # Obtener la fecha actual (aware)
        current_time = timezone.now()

        # Obtener el primer día de la semana actual (lunes)
        start_of_week = current_time - timedelta(days=current_time.weekday())  # Lunes de esta semana
        end_of_week = start_of_week + timedelta(days=6)  # Domingo de esta semana

        # Filtrar los eventos que ocurren dentro de la semana actual
        events_this_week = CulturalEvent.objects.filter(
            Q(start_date__gte=start_of_week, start_date__lte=end_of_week) |  # Evento con start_date dentro de la semana
            Q(end_date__gte=start_of_week, end_date__lte=end_of_week) |  # Evento con end_date dentro de la semana
            Q(start_date__lte=end_of_week, end_date__gte=start_of_week)  # Evento que abarca la semana
        ).order_by('start_date')

        # Prefetch los favoritos del usuario autenticado
        user_favorites = FavoriteEvent.objects.filter(user=request.user)
        events_this_week = events_this_week.prefetch_related(
            Prefetch('favorited_by', queryset=user_favorites, to_attr='user_favorites')
        )

        # Serializar los eventos con el contexto del usuario autenticado
        serializer = CulturalEventSerializer(events_this_week, many=True, context={'request': request})

        # Devolver los eventos en formato JSON
        return Response(serializer.data)


class EventDetailView(APIView):
    """
    Vista para obtener los detalles de un evento cultural.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Obtiene los detalles de un evento cultural por ID.",
        responses={
            200: CulturalEventSerializer(),
            404: "Event not found"
        }
    )
    def get(self, request, id):  # pylint: disable=redefined-builtin
        """
        Obtiene los detalles de un evento cultural por ID.
        """
        try:
            event = CulturalEvent.objects.get(id=id)
        except CulturalEvent.DoesNotExist:
            return Response({"error": "Event not found"}, status=status.HTTP_404_NOT_FOUND)

        # Prefetch los favoritos del usuario autenticado
        user_favorites = FavoriteEvent.objects.filter(user=request.user)
        event = CulturalEvent.objects.prefetch_related(
            Prefetch('favorited_by', queryset=user_favorites, to_attr='user_favorites')
        ).get(id=id)

        # Serializar el evento con el contexto del usuario autenticado
        serializer = CulturalEventSerializer(event, context={'request': request})

        # Devolver el evento en formato JSON con código 200 OK
        return Response(serializer.data, status=status.HTTP_200_OK)

class FavoriteEventView(APIView):
    """
    Endpoint para que un usuario guarde un evento como favorito o lo quite de favoritos.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Agrega un evento a la lista de favoritos del usuario autenticado.",
        responses={
            201: FavoriteEventSerializer(),
            404: "Event not found",
            409: "Event already in favorites"
        }
    )

    def post(self, request, *args, **kwargs):  # pylint: disable=unused-argument
        """
        Agrega un evento a la lista de favoritos del usuario autenticado.
        """
        user = request.user
        event_id = kwargs.get('id')  # Tomamos el ID de la URL para consistencia

        try:
            event = CulturalEvent.objects.get(id=event_id)
        except CulturalEvent.DoesNotExist:
            return Response({"error": "Event not found"}, status=status.HTTP_404_NOT_FOUND)

        favorite, created = FavoriteEvent.objects.get_or_create(user=user, event=event)
        if not created:
            return Response({"error": "Event already in favorites"}, status=status.HTTP_409_CONFLICT)

        # Serializar el evento favorito añadido
        serializer = FavoriteEventSerializer(favorite)
        return Response(serializer.data, status=status.HTTP_201_CREATED)

    @swagger_auto_schema(
        operation_description="Elimina un evento de la lista de favoritos del usuario autenticado.",
        responses={
            200: openapi.Response(
                description="Evento eliminado de favoritos exitosamente.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "message": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="Event removed from favorites successfully"
                        )
                    }
                )
            ),
            404: openapi.Response(
                description="Evento no encontrado en favoritos.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "error": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="Event not found in favorites"
                        )
                    }
                )
            )
        }
    )
    def delete(self, request, *args, **kwargs):  # pylint: disable=unused-argument
        """
        Elimina un evento de la lista de favoritos del usuario autenticado.
        """
        user = request.user
        event_id = kwargs.get('id')  # Consistente con el método POST

        try:
            favorite = FavoriteEvent.objects.get(user=user, event__id=event_id)
            favorite.delete()
            return Response({"message": "Event removed from favorites successfully"}, status=status.HTTP_200_OK)
        except FavoriteEvent.DoesNotExist:
            return Response({"error": "Event not found in favorites"}, status=status.HTTP_404_NOT_FOUND)


class ListFavoriteEventsView(generics.ListAPIView):
    """
    Endpoint para listar los eventos favoritos del usuario autenticado.
    """
    permission_classes = [IsAuthenticated]
    serializer_class = CulturalEventSerializer

    @swagger_auto_schema(
        operation_description="Devuelve los eventos favoritos del usuario autenticado.",
        responses={
            200: CulturalEventSerializer(many=True),
            403: "No tienes permisos para realizar esta acción.",
        }
    )
    def get_queryset(self):
        """
        Devuelve los eventos favoritos del usuario autenticado.
        """
        user = self.request.user

        # Filtrar los eventos favoritos del usuario
        user_favorites = FavoriteEvent.objects.filter(user=user)

        # Prefetch para optimizar la consulta
        return CulturalEvent.objects.filter(favorited_by__user=user).prefetch_related(
            Prefetch('favorited_by', queryset=user_favorites, to_attr='user_favorites')
        )

class UserChatsView(APIView):
    """
    Devuelve los chats en los que el usuario es participante.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Devuelve una lista de chats grupales en los que el usuario autenticado participa.",
        responses={
            200: EventGroupChatSerializer(many=True),
            403: "No tienes permisos para realizar esta acción.",
        }
    )
    def get(self, request):
        """
        Devuelve una lista de chats grupales en los que el usuario autenticado participa.
        """
        user = request.user

        # Obtener todos los chats en los que el usuario es participante
        chats = EventGroupChat.objects.filter(participants__user=user).distinct()

        # Serializar los datos de los chats
        serializer = EventGroupChatSerializer(chats, many=True)

        return Response(serializer.data, status=status.HTTP_200_OK)


class IsUserInGroupView(APIView):
    """
    Verifica si un usuario es participante de un grupo de chat.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Verifica si el usuario autenticado es miembro de un chat grupal específico.",
        manual_parameters=[
            openapi.Parameter(
                'chat_id',
                openapi.IN_PATH,
                description="ID del chat grupal",
                type=openapi.TYPE_INTEGER,
                required=True
            )
        ],
        responses={
            200: openapi.Response(
                description="El usuario es participante del chat.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "is_participant": openapi.Schema(
                            type=openapi.TYPE_BOOLEAN,
                            example=True
                        )
                    }
                )
            ),
            404: openapi.Response(
                description="Chat not found.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="Chat not found."
                        )
                    }
                )
            )
        }
    )
    def get(self, request, chat_id):
        """
        Verifica si el usuario autenticado es miembro de un chat grupal específico.
        """
        user = request.user
        try:
            chat = EventGroupChat.objects.get(id=chat_id)
        except EventGroupChat.DoesNotExist:
            return Response(
                {"detail": "Chat not found."},
                status=status.HTTP_404_NOT_FOUND
            )

        # Verificar si el usuario es participante del chat
        is_participant = Participant.objects.filter(user=user, chat=chat).exists()
        return Response({"is_participant": is_participant}, status=status.HTTP_200_OK)

class JoinEventChatView(APIView):
    """
    Permite a un usuario unirse a un chat de evento y registrar su token FCM.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description=(
            "Permite al usuario unirse a un chat grupal de un evento cultural "
            "y registrar su token FCM."
        ),
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                'fcm_token': openapi.Schema(type=openapi.TYPE_STRING, description='Token FCM enviado desde el cliente')
            },
            required=['fcm_token']
        ),
        responses={
            200: openapi.Response(
                description="Joined chat successfully.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Joined chat successfully."),
                        "is_new_participant": openapi.Schema(type=openapi.TYPE_BOOLEAN, example=True),
                        "chat": openapi.Schema(type=openapi.TYPE_OBJECT, ref="#/components/schemas/EventGroupChat")
                    }
                )
            ),
            400: openapi.Response(
                description="FCM token is required.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="FCM token is required.")
                    }
                )
            ),
            404: openapi.Response(
                description="Event not found.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Event not found.")
                    }
                )
            )
        }
    )
    def post(self, request, event_id):
        """
        Permite al usuario unirse a un chat grupal de un evento cultural.
        """
        user = request.user
        fcm_token = request.data.get('fcm_token')  # Token FCM enviado desde el cliente

        if not fcm_token:
            return Response({"detail": "FCM token is required."}, status=status.HTTP_400_BAD_REQUEST)

        try:
            event = CulturalEvent.objects.get(id=event_id)
        except CulturalEvent.DoesNotExist:
            return Response({"detail": "Event not found."}, status=status.HTTP_404_NOT_FOUND)

        # Obtener o crear el chat del evento
        chat, _ = EventGroupChat.objects.get_or_create(event=event)

        # Verificar si el usuario ya es participante
        participant, created = Participant.objects.get_or_create(user=user, chat=chat)

        # Actualizar el token FCM si es necesario
        if participant.fcm_token != fcm_token:
            participant.fcm_token = fcm_token
            participant.save()

        # Serializar la información del chat
        serializer = EventGroupChatSerializer(chat)

        return Response(
            {
                "detail": "Joined chat successfully.",
                "is_new_participant": created,
                "chat": serializer.data,
            },
            status=status.HTTP_200_OK
        )

class ChatMessagePagination(PageNumberPagination):
    """
    Configuración de paginación para mensajes de chat.
    """
    page_size = 10  # Número de mensajes por página
    page_size_query_param = 'page_size'
    max_page_size = 100


class GetMessagesView(APIView):
    """
    Permite obtener los mensajes de un chat.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Devuelve los mensajes de un chat grupal, con paginación.",
        manual_parameters=[
            openapi.Parameter(
                'chat_id',
                openapi.IN_PATH,
                description="ID del chat grupal",
                type=openapi.TYPE_INTEGER,
                required=True
            )
        ],
        responses={
            200: openapi.Response(
                description="Mensajes obtenidos exitosamente.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "results": openapi.Schema(
                            type=openapi.TYPE_ARRAY,
                            items=openapi.Schema(
                                type=openapi.TYPE_OBJECT,
                                properties={
                                    "id": openapi.Schema(type=openapi.TYPE_INTEGER, example=1),
                                    "content": openapi.Schema(
                                        type=openapi.TYPE_STRING,
                                        example="Mensaje de prueba"
                                    ),
                                    "created_at": openapi.Schema(
                                        type=openapi.TYPE_STRING,
                                        example="2023-01-01T00:00:00Z"
                                    ),
                                    "user": openapi.Schema(
                                        type=openapi.TYPE_STRING,
                                        example="usuario@example.com"
                                    )
                                }
                            )
                        ),
                        "count": openapi.Schema(type=openapi.TYPE_INTEGER, example=10),
                        "next": openapi.Schema(type=openapi.TYPE_STRING, example=None),
                        "previous": openapi.Schema(type=openapi.TYPE_STRING, example=None)
                    }
                )
            ),
            403: openapi.Response(
                description="No tienes permisos para realizar esta acción.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="You are not a participant in this chat."
                        )
                    }
                )
            ),
            404: openapi.Response(
                description="Chat no encontrado.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Chat not found.")
                    }
                )
            )
        }
    )
    def get(self, request, chat_id):
        """
        Devuelve los mensajes de un chat grupal, con paginación.
        """
        # Intentar obtener el chat
        try:
            chat = EventGroupChat.objects.get(id=chat_id)
        except EventGroupChat.DoesNotExist:
            return Response(
                {"detail": "Chat not found."},
                status=status.HTTP_404_NOT_FOUND
            )

        # Verificar si el usuario es participante del chat
        user = request.user
        if not Participant.objects.filter(user=user, chat=chat).exists():
            return Response(
                {"detail": "You are not a participant in this chat."},
                status=status.HTTP_403_FORBIDDEN
            )

        # Obtener mensajes del chat ordenados por más recientes primero
        messages = Message.objects.filter(chat=chat).order_by('-created_at')

        # Aplicar paginación
        paginator = ChatMessagePagination()
        paginated_messages = paginator.paginate_queryset(messages, request)
        serializer = MessageSerializer(paginated_messages, many=True)

        # Responder con los mensajes paginados
        return paginator.get_paginated_response(serializer.data)


class LeaveEventChatView(APIView):
    """
    Permite a un usuario salir de un chat grupal de un evento cultural.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Permite al usuario salir de un chat grupal de un evento cultural.",
        manual_parameters=[
            openapi.Parameter(
                'chat_id',
                openapi.IN_PATH,
                description="ID del chat grupal",
                type=openapi.TYPE_INTEGER,
                required=True
            )
        ],
        responses={
            200: openapi.Response(
                description="You have successfully left the chat.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="You have successfully left the chat."
                        ),
                        "remaining_chats": openapi.Schema(
                            type=openapi.TYPE_ARRAY,
                            items=openapi.Schema(
                                type=openapi.TYPE_OBJECT,
                                ref="#/components/schemas/EventGroupChat"
                            )
                        )
                    }
                )
            ),
            400: openapi.Response(
                description="User is not a participant of this chat.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="User is not a participant of this chat."
                        )
                    }
                )
            ),
            404: openapi.Response(
                description="Chat not found.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Chat not found.")
                    }
                )
            )
        }
    )
    def post(self, request, chat_id):
        """
        Permite al usuario salir de un chat grupal de un evento cultural.
        """
        user = request.user

        # Obtener el chat
        chat = get_object_or_404(EventGroupChat, id=chat_id)

        # Verificar si el usuario es participante del chat
        try:
            participant = Participant.objects.get(user=user, chat=chat)
        except Participant.DoesNotExist:
            return Response(
                {"detail": "User is not a participant of this chat."},
                status=status.HTTP_400_BAD_REQUEST
            )

        # Eliminar el participante
        participant.delete()

        # Obtener los chats restantes en los que el usuario participa
        remaining_chats = EventGroupChat.objects.filter(participants__user=user).distinct()
        serializer = EventGroupChatSerializer(remaining_chats, many=True)

        return Response(
            {
                "detail": "You have successfully left the chat.",
                "remaining_chats": serializer.data,
            },
            status=status.HTTP_200_OK
        )

class SendMessageView(APIView):
    """
    Permite enviar un mensaje en un chat grupal.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Envía un mensaje en un chat grupal y notifica a los demás participantes.",
        request_body=MessageCreateSerializer,
        manual_parameters=[
            openapi.Parameter(
                'chat_id',
                openapi.IN_PATH,
                description="ID del chat grupal",
                type=openapi.TYPE_INTEGER,
                required=True
            )
        ],
        responses={
            201: openapi.Response(
                description="Message sent successfully.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Message sent successfully."),
                        "message_id": openapi.Schema(type=openapi.TYPE_INTEGER, example=1)
                    }
                )
            ),
            400: openapi.Response(
                description="Invalid input.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Invalid input.")
                    }
                )
            )
        }
    )
    def post(self, request, chat_id):
        """
        Envía un mensaje en un chat grupal y notifica a los demás participantes.
        """
        serializer = MessageCreateSerializer(
            data=request.data,
            context={'request': request, 'chat_id': chat_id}
        )

        if serializer.is_valid():
            message = serializer.save()

            # Obtener el nombre del evento relacionado con el chat
            event_chat = EventGroupChat.objects.get(id=chat_id)
            event_name = event_chat.event.name

            # Obtener tokens FCM de los participantes, excluyendo al remitente
            participants = Participant.objects.filter(chat=message.chat).exclude(user=request.user)
            fcm_tokens = [p.fcm_token for p in participants if p.fcm_token]

            if fcm_tokens:
                title = f"Nuevo mensaje en el chat del evento {event_name}."
                # Construir el payload para las notificaciones
                message_content = f"{request.user.username}: {message.message}"
                payload = {
                    "type": "new_message",
                    "chat_id": chat_id,
                    "chat_name": event_name,
                    "message_id": message.id,
                    "message": message.message,
                }

                # Enviar notificaciones
                send_fcm_notification(fcm_tokens, title, message_content, payload)

            return Response(
                {"detail": "Message sent successfully.", "message_id": message.id},
                status=status.HTTP_201_CREATED
            )

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class DeleteMessageView(APIView):
    """
    Permite a un usuario eliminar su propio mensaje en un chat grupal
    y notifica a los demás participantes.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description=(
            "Elimina un mensaje enviado por el usuario en un chat grupal "
            "y notifica a los demás participantes."
        ),
        manual_parameters=[
            openapi.Parameter(
                'message_id',
                openapi.IN_PATH,
                description="ID del mensaje a eliminar",
                type=openapi.TYPE_INTEGER,
                required=True
            )
        ],
        responses={
            200: openapi.Response(
                description="Message deleted successfully.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="Message deleted successfully."
                        )
                    }
                )
            ),
            403: openapi.Response(
                description="You are not a participant of this chat.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="You are not a participant of this chat."
                        )
                    }
                )
            ),
            404: openapi.Response(
                description="Message not found or you are not the creator.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="Message not found or you are not the creator."
                        )
                    }
                )
            )
        }
    )
    def delete(self, request, message_id):
        """
        Elimina un mensaje enviado por el usuario en un chat grupal y notifica a los demás participantes.
        """
        user = request.user

        # Verificar que el mensaje existe y pertenece al usuario
        try:
            message = Message.objects.get(id=message_id, creator=user)
        except Message.DoesNotExist:
            return Response(
                {"detail": "Message not found or you are not the creator."},
                status=status.HTTP_404_NOT_FOUND
            )

        # Verificar que el usuario es participante del chat
        if not Participant.objects.filter(user=user, chat=message.chat).exists():
            return Response(
                {"detail": "You are not a participant of this chat."},
                status=status.HTTP_403_FORBIDDEN
            )

        # Guardar detalles del mensaje antes de eliminar
        chat_id = message.chat.id

        # Obtener el nombre del evento relacionado con el chat
        event_chat = EventGroupChat.objects.get(id=chat_id)
        event_name = event_chat.event.name

        message_id = message.id

        # Eliminar el mensaje
        message.delete()

        # Notificar a los demás participantes
        participants = Participant.objects.filter(chat=message.chat).exclude(user=user)
        fcm_tokens = [p.fcm_token for p in participants if p.fcm_token]

        if fcm_tokens:
            title = f"Un mensaje ha sido eliminado en el chat del evento {event_name}."
            message_content = f"Un mensaje ha sido eliminado en el chat del evento {event_name}."
            payload = {
                "type": "message_deleted",
                "chat_id": chat_id,
                "chat_name": event_name,
                "message_id": message_id,
            }
            send_fcm_notification(fcm_tokens, title, message_content, payload)

        return Response(
            {"detail": "Message deleted successfully."},
            status=status.HTTP_200_OK
        )


class ReportMessageView(APIView):
    """
    Permite a un usuario reportar un mensaje en un chat grupal.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Permite al usuario reportar un mensaje en un chat grupal.",
        manual_parameters=[
            openapi.Parameter(
                'message_id',
                openapi.IN_PATH,
                description="ID del mensaje a reportar",
                type=openapi.TYPE_INTEGER,
                required=True
            )
        ],
        responses={
            200: openapi.Response(
                description="Message reported successfully.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Message reported successfully.")
                    }
                )
            ),
            400: openapi.Response(
                description="You cannot report your own message.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="You cannot report your own message."
                        )
                    }
                )
            ),
            403: openapi.Response(
                description="You are not a participant of this chat.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="You are not a participant of this chat."
                        )
                    }
                )
            ),
            404: openapi.Response(
                description="Message not found.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Message not found.")
                    }
                )
            )
        }
    )
    def post(self, request, message_id):
        """
        Permite al usuario reportar un mensaje en un chat grupal.
        """
        user = request.user

        # Verificar que el mensaje existe
        try:
            message = Message.objects.get(id=message_id)
        except Message.DoesNotExist:
            return Response(
                {"detail": "Message not found."},
                status=status.HTTP_404_NOT_FOUND
            )

        # Verificar que el usuario es participante del chat
        if not Participant.objects.filter(user=user, chat=message.chat).exists():
            return Response(
                {"detail": "You are not a participant of this chat."},
                status=status.HTTP_403_FORBIDDEN
            )

        # Verificar que el usuario no esté reportando su propio mensaje
        if message.creator == user:
            return Response(
                {"detail": "You cannot report your own message."},
                status=status.HTTP_400_BAD_REQUEST
            )

        # Crear un reporte del mensaje
        message.reports_count += 1
        message.save()

        return Response(
            {"detail": "Message reported successfully."},
            status=status.HTTP_200_OK
        )

class ListReportedMessagesView(APIView):
    """
    Permite a un administrador listar los mensajes reportados.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description=(
            "Devuelve una lista de mensajes reportados en chats grupales "
            "(solo para administradores)."
        ),
        responses={
            200: openapi.Response(
                description="Lista de mensajes reportados obtenida exitosamente.",
                schema=openapi.Schema(
                    type=openapi.TYPE_ARRAY,
                    items=openapi.Schema(
                        type=openapi.TYPE_OBJECT,
                        properties={
                            "id": openapi.Schema(type=openapi.TYPE_INTEGER, example=1),
                            "message": openapi.Schema(type=openapi.TYPE_STRING, example="Mensaje reportado"),
                            "creator_name": openapi.Schema(type=openapi.TYPE_STRING, example="usuario"),
                            "created_at": openapi.Schema(type=openapi.TYPE_STRING, example="2023-01-01T00:00:00"),
                            "reports_count": openapi.Schema(type=openapi.TYPE_INTEGER, example=3)
                        }
                    )
                )
            ),
            403: openapi.Response(
                description="No tienes permisos para realizar esta acción.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="You do not have permission to perform this action."
                        )
                    }
                )
            )
        }
    )
    def get(self, request):
        """
        Devuelve una lista de mensajes reportados en chats grupales (solo para administradores).
        """
        # Verificar que el usuario sea un administrador
        if not request.user.is_staff:
            return Response(
                {"detail": "You do not have permission to perform this action."},
                status=status.HTTP_403_FORBIDDEN
            )
        # Filtrar los mensajes reportados
        reported_messages = Message.objects.filter(reports_count__gt=0)

        # Serializar los mensajes
        serializer = MessageReportedSerializer(reported_messages, many=True)

        return Response(serializer.data, status=status.HTTP_200_OK)


class DeleteAnyMessageView(APIView):
    """
    Permite a un administrador eliminar cualquier mensaje en un chat grupal
    y notifica a los demás participantes.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Permite a un administrador eliminar un mensaje específico en un chat grupal.",
        manual_parameters=[
            openapi.Parameter(
                'message_id',
                openapi.IN_PATH,
                description="ID del mensaje a eliminar",
                type=openapi.TYPE_INTEGER,
                required=True
            )
        ],
        responses={
            200: openapi.Response(
                description="Message deleted successfully.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="Message deleted successfully."
                        )
                    }
                )
            ),
            403: openapi.Response(
                description="You do not have permission to perform this action.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            example="You do not have permission to perform this action."
                        )
                    }
                )
            ),
            404: openapi.Response(
                description="Message not found.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "detail": openapi.Schema(type=openapi.TYPE_STRING, example="Message not found.")
                    }
                )
            )
        }
    )
    def delete(self, request, message_id):
        """
        Permite a un administrador eliminar un mensaje específico en un chat grupal.
        """
        user = request.user

        # Verificar que el usuario es administrador
        if not user.is_staff:
            return Response(
                {"detail": "You do not have permission to perform this action."},
                status=status.HTTP_403_FORBIDDEN
            )

        # Verificar que el mensaje existe
        try:
            message = Message.objects.get(id=message_id)
        except Message.DoesNotExist:
            return Response(
                {"detail": "Message not found."},
                status=status.HTTP_404_NOT_FOUND
            )

        # Guardar detalles del mensaje antes de eliminar
        chat_id = message.chat.id

        # Obtener el nombre del evento relacionado con el chat
        event_chat = EventGroupChat.objects.get(id=chat_id)
        event_name = event_chat.event.name

        message_id = message.id

        # Eliminar el mensaje
        message.delete()

        # Notificar a los demás participantes
        participants = Participant.objects.filter(chat=message.chat).exclude(user=user)
        fcm_tokens = [p.fcm_token for p in participants if p.fcm_token]

        if fcm_tokens:
            title = f"Un mensaje ha sido eliminado por un moderador en el chat del evento {event_name}."
            message_content = f"Un mensaje ha sido eliminado por un moderador en el chat del evento {event_name}."
            payload = {
                "type": "message_deleted",
                "chat_id": chat_id,
                "chat_name": event_name,
                "message_id": message_id,
            }
            send_fcm_notification(fcm_tokens, title, message_content, payload)

        return Response(
            {"detail": "Message deleted successfully."},
            status=status.HTTP_200_OK
        )
