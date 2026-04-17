"""
URL patterns for the Itinerario app.
"""

from django.urls import path
from . import views
from .views import ItinerarioAutomaticoView

urlpatterns = [
    # API views
    path(
        'api/itinerary/<int:pk>/',
        views.api_get_itinerary,
        name='api_get_itinerary'
    ),
    path(
        'api/create/',
        views.api_crear_itinerario,
        name='api_crear_itinerario'
    ),
    path(
        'api/createautomatic/',
        ItinerarioAutomaticoView.as_view(),
        name='api_crear_itinerario_automatico'
    ),
    path(
        'api/<int:pk>/delete/',
        views.api_borrar_itinerario,
        name='api_borrar_itinerario'
    ),
    path(
        'api/list/',
        views.api_list_itinerario,
        name='api_list_itinerario'
    ),
    path(
        'api/<int:pk>/items/',
        views.api_get_itinerary_items,
        name='api_get_itinerary_items'
    ),
    path(
        'api/<int:pk>/days/<int:day>/update-points/',
        views.api_update_points_in_itinerary,
        name='remove-points-from-itinerary'
    ),
    path(
        'api/<int:pk>/update-multiple-days/',
        views.api_update_multiple_days_in_itinerary,
        name='update-multiple-days'
    ),
    path(
        'api/<int:pk>/edit-name/',
        views.api_update_itinerary_name,
        name='update-itinerary-name'
    ),
]
