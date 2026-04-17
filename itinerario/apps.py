"""
Apps module for Itinerario app
"""

from django.apps import AppConfig


class ItinerarioConfig(AppConfig):
    """App configuration for the Itinerario app."""
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'itinerario'
