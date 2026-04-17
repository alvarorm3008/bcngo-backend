"""
Definición de las rutas para la aplicación Servicio.
"""

from django.urls import path
from .views import NearbyStopsView

urlpatterns = [
    path('nearby-stops', NearbyStopsView.as_view(), name='nearby-stops'),
]
