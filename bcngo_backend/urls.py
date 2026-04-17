"""
Configuración de URLs para el proyecto BCNGo.

Este módulo define las rutas principales del proyecto, incluyendo las aplicaciones
y la configuración de Swagger para la documentación de la API.
"""

from django.contrib import admin
from django.urls import include, path
from drf_yasg.views import get_schema_view
from drf_yasg import openapi
from rest_framework.permissions import AllowAny

# Configuración de Swagger
SchemaView = get_schema_view(
    openapi.Info(
        title="BCNGo API",
        default_version="v1",
        description="Documentación de la API de BCNGo",
        terms_of_service="https://www.google.com/policies/terms/",
        contact=openapi.Contact(email="pesbcngo@gmail.com"),
        license=openapi.License(name="BSD License"),
    ),
    public=True,
    permission_classes=(AllowAny,),
)

urlpatterns = [
    path('admin/', admin.site.urls),
    path("puntosdeinteres/", include("puntosdeinteres.urls")),
    path("itinerario/", include("itinerario.urls")),
    path("users/", include("users.urls")),
    path('pasaporte/', include('pasaporte.urls')),
    path('eventos/', include('eventos.urls')),
    path('swagger/', SchemaView.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),
    path('redoc/', SchemaView.with_ui('redoc', cache_timeout=0), name='schema-redoc'),
    path('', include('servicio.urls')),
]
