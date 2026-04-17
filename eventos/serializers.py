"""
Serializadores para el modelo CulturalEvent.
"""

from rest_framework import serializers  # pylint: disable=import-error
from .models import CulturalEvent, FavoriteEvent, Message, EventGroupChat, Participant

class CulturalEventSerializer(serializers.ModelSerializer):
    """
    Serializador para el modelo CulturalEvent.
    """
    start_date = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S.%fZ')
    end_date = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S.%fZ')
    is_favorite = serializers.SerializerMethodField()

    class Meta:
        """
        Clase Meta para definir el modelo y los campos a serializar.
        """
        model = CulturalEvent
        fields = [
            'id', 'name', 'longitude', 'latitude', 'start_date',
            'end_date', 'road_name', 'district', 'street_number', 'is_favorite'
        ]

    def get_is_favorite(self, obj):
        """
        Determina si el evento es favorito para el usuario autenticado.
        """
        # Accedemos al atributo 'user_favorites' que contiene los eventos favoritos precargados
        user_favorites = getattr(obj, 'user_favorites', [])
        return obj.id in [favorite.event_id for favorite in user_favorites]


class FavoriteEventSerializer(serializers.ModelSerializer):
    """
    Serializador para el modelo FavoriteEvent.
    """

    class Meta:
        """
        Clase Meta para definir el modelo y los campos a serializar.
        """
        model = FavoriteEvent
        fields = ['user', 'event', 'added_at']
        read_only_fields = ['user', 'added_at']


class MessageCreateSerializer(serializers.ModelSerializer):
    """
    Serializador para la creación de mensajes en un chat.
    """

    class Meta:
        """
        Clase Meta para definir el modelo y los campos a serializar.
        """
        model = Message
        fields = ['message']

    def validate(self, data):  # pylint: disable=arguments-renamed
        """
        Valida que el usuario sea participante del chat.
        """
        user = self.context['request'].user
        chat_id = self.context['chat_id']

        try:
            chat = EventGroupChat.objects.get(id=chat_id)
        except EventGroupChat.DoesNotExist:
            raise serializers.ValidationError("El chat no fue encontrado.")  # pylint: disable=raise-missing-from

        if not Participant.objects.filter(user=user, chat=chat).exists():
            raise serializers.ValidationError("El usuario no es participante de este chat.")

        data['chat'] = chat
        return data

    def create(self, validated_data):
        """
        Crea y devuelve una instancia de mensaje.
        """
        return Message.objects.create(
            chat=validated_data['chat'],
            creator=self.context['request'].user,
            message=validated_data['message']
        )

class MessageSerializer(serializers.ModelSerializer):
    """
    Serializador para representar un mensaje en el chat.
    """
    creator_name = serializers.CharField(source='creator.username', read_only=True)
    created_at = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S.%fZ')

    class Meta:
        """
        Clase Meta para definir el modelo y los campos a serializar.
        """
        model = Message
        fields = ['id', 'message', 'creator_name', 'created_at']

class MessageReportedSerializer(serializers.ModelSerializer):
    """
    Serializador para representar un mensaje en el chat.
    """
    creator_name = serializers.CharField(source='creator.username', read_only=True)
    created_at = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S')

    class Meta:
        """
        Clase Meta para definir el modelo y los campos a serializar.
        """
        model = Message
        fields = ['id', 'message', 'creator_name', 'created_at', 'reports_count']

class ParticipantSerializer(serializers.ModelSerializer):
    """
    Serializador para mostrar información de los participantes.
    """
    username = serializers.CharField(source='user.username', read_only=True)
    joined_at = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S.%fZ')

    class Meta:
        """
        Clase Meta para definir el modelo y los campos a serializar.
        """
        model = Participant
        fields = ['username', 'fcm_token', 'joined_at']

class EventGroupChatSerializer(serializers.ModelSerializer):
    """
    Serializador para mostrar información del chat grupal.
    """
    participants = ParticipantSerializer(many=True, source='participants.all')
    event_name = serializers.CharField(source='event.name', read_only=True)
    created_at = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S.%fZ')

    class Meta:
        """
        Clase Meta para definir el modelo y los campos a serializar.
        """
        model = EventGroupChat
        fields = ['id', 'event', 'event_name', 'created_at', 'participants']
