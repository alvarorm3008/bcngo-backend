"""
Configuración de la aplicación 'eventos' para el proyecto Django.
"""

from django.apps import AppConfig


class EventosConfig(AppConfig):
    """
    Configuración de la aplicación Eventos.
    """
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'eventos'
