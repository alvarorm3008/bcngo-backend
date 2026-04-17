"""
Este módulo configura la interfaz de administración para la aplicación de Puntos de Interés.
"""

from django.contrib import admin
from .models import PointOfInterest

# Registrar el modelo PointOfInterest en el panel de administración.
admin.site.register(PointOfInterest)
