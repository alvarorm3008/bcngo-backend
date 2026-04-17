"""
Módulo de vistas para la aplicación principal de BCNGo.
"""

from django.http import HttpResponse

def home_view():
    """
    Vista para la página de inicio.
    """
    return HttpResponse("Bienvenido a BCNGO!")
