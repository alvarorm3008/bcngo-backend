"""
Este módulo define los modelos de datos para la aplicación de eventos,
incluyendo el modelo CulturalEvent para almacenar información sobre los eventos culturales.
"""
from django.db import models
from django.conf import settings
from rest_framework import serializers  # pylint: disable=unused-import

class CulturalEvent(models.Model):
    """
    Representa un evento cultural en la base de datos, con detalles como nombre,
    fechas, ubicación, y coordenadas geográficas.
    """
    register_id = models.CharField(max_length=255, unique=True)  # Identificador único
    name = models.CharField(max_length=255)  # Nombre del evento
    start_date = models.DateTimeField(null=True)  # Fecha de inicio
    end_date = models.DateTimeField()  # Fecha de fin
    district = models.CharField(max_length=255)  # Distrito
    road_name = models.CharField(max_length=255, blank=True, null=True)  # Calle
    street_number = models.CharField(max_length=50, blank=True, null=True)  # Número
    latitude = models.FloatField(null=True)  # Coordenada latitud
    longitude = models.FloatField(null=True)  # Coordenada longitud
    modified = models.DateTimeField()  # Fecha de modificación

    # pylint: disable=E0307
    def __str__(self):
        return self.name

class FavoriteEvent(models.Model):
    """
    Modelo que relaciona usuarios con eventos culturales favoritos.
    """
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name='favorite_events')
    event = models.ForeignKey('CulturalEvent', on_delete=models.CASCADE, related_name='favorited_by')
    added_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        """
        Meta opciones para FavoriteEvent.
        """
        unique_together = ('user', 'event')  # Un usuario no puede guardar el mismo evento dos veces
        verbose_name = 'Favorite Event'
        verbose_name_plural = 'Favorite Events'

    def __str__(self):
        return f"{self.user.email} - {self.event.name}"


class EventGroupChat(models.Model):
    """
    Representa un chat grupal para un evento cultural.
    """
    event = models.ForeignKey(CulturalEvent, on_delete=models.CASCADE)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Chat for {self.event.name}"

class Participant(models.Model):
    """
    Representa un participante en un chat grupal de un evento cultural.
    """
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    chat = models.ForeignKey(EventGroupChat, on_delete=models.CASCADE, related_name='participants')
    fcm_token = models.CharField(blank=True, null=True)  # Token de FCM para enviar notificaciones
    joined_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        """
        Meta opciones para Participant.
        """
        unique_together = ('user', 'chat')  # Un usuario no puede unirse al mismo chat más de una vez

    def __str__(self):
        return f"{self.user.email} in {self.chat.event.name}"

class Message(models.Model):
    """
    Representa un mensaje en un chat de un evento cultural.
    """
    chat = models.ForeignKey(EventGroupChat, on_delete=models.CASCADE)
    creator = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    message = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    reports_count = models.PositiveIntegerField(default=0)

    def __str__(self):
        return f"Message by {self.creator.username} in {self.chat.event.name}"
