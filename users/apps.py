"""
Este módulo define la configuración de la aplicación 'users'.
"""

from django.apps import AppConfig


class UsersConfig(AppConfig):
    """
    Configuración de la aplicación 'users'.
    """
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'users'
