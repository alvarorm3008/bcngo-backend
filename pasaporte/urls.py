"""
URLs para la aplicación de Pasaporte Digital.
"""

from django.urls import path
from .views import PassportDetailView, MarkPointView

urlpatterns = [
    path('consultar/', PassportDetailView.as_view(), name='consultar_pasaporte'),
    path('marcar/', MarkPointView.as_view(), name='marcar_punto'),
]
