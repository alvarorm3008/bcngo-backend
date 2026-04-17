"""
Tests for ItineraryItem ordering.
"""

from django.test import TestCase
from django.contrib.auth import get_user_model
from itinerario.models import Itinerario, ItineraryItem


class ItineraryItemOrderTest(TestCase):
    """
    Test case for verifying the order of ItineraryItem objects.
    """

    def test_itinerary_item_order(self):
        """
        Test that ItineraryItem objects are correctly ordered by 'day'.
        """
        # Obtener el modelo de usuario
        user_model = get_user_model()

        # Crear un usuario de prueba
        user = user_model.objects.create_user(username="testuser", password="password123")

        # Crear itinerarios asociados al usuario
        itinerary1 = Itinerario.objects.create(name="Itinerario 1", user=user)
        itinerary2 = Itinerario.objects.create(name="Itinerario 2", user=user)

        # Crear elementos de itinerario
        ItineraryItem.objects.create(itinerary=itinerary1, day=2)
        ItineraryItem.objects.create(itinerary=itinerary1, day=1)
        ItineraryItem.objects.create(itinerary=itinerary2, day=1)

        # Consultar y verificar el orden
        items = ItineraryItem.objects.order_by('itinerary__id', 'day')
        self.assertEqual(
            list(items.values_list('itinerary__id', 'day')),
            [
                (itinerary1.id, 1),
                (itinerary1.id, 2),
                (itinerary2.id, 1),
            ]
        )
