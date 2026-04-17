"""
Definicion del modelo itinerario y sus relacionados items.
El modelo itinerario incluye el modelo ItineraryItem
que representa un item dentro de un itinerary, correspondiente a un dia especifico.
"""

from django.db import models
from django.conf import settings
from puntosdeinteres.models import PointOfInterest

class Itinerario(models.Model):
    """
    Represents an itinerary created by a user.
    """
    name = models.CharField(max_length=100, default="Nuevo Itinerario")
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="itineraries"
    )
    start_date = models.DateField(null=True, blank=True)
    end_date = models.DateField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    favourite = models.BooleanField(default=False)

    # Many-to-Many relationship for manually selected points
    manual = models.ManyToManyField(
        PointOfInterest, blank=True, related_name="manual_itineraries"
    )

    @property
    def days_of_stay(self):
        """
        Calculate the number of days of stay based on start_date and end_date.
        """
        if self.start_date and self.end_date:
            return (self.end_date - self.start_date).days + 1
        return 0

    def generate_itinerary_points(self):
        """
        Generate itinerary points by distributing manual points across days of stay.
        """
        days_of_stay = self.days_of_stay
        if days_of_stay <= 0:
            print("No hay días de estancia disponibles.")
            return

        # Obtener puntos manuales
        manual_points = list(self.manual.all()) # pylint: disable=E1101

        # Calcular división base y sobrantes
        points_per_day = len(manual_points) // days_of_stay
        extra_points = len(manual_points) % days_of_stay

        # Limpiar elementos existentes
        self.items.all().delete() # pylint: disable=E1101

        # Crear itinerario distribuyendo los puntos
        point_index = 0
        for day in range(1, days_of_stay + 1):
            itinerary_item = ItineraryItem.objects.create(itinerary=self, day=day)

            # Asignar puntos por día
            daily_points_count = points_per_day + (1 if extra_points > 0 else 0)
            daily_points = manual_points[point_index:point_index + daily_points_count]
            point_index += daily_points_count
            extra_points -= 1

            # Establecer los puntos al día actual
            itinerary_item.points.set(daily_points)
            itinerary_item.save()

    def __str__(self):
        return str(self.name)


class ItineraryItem(models.Model):
    """
    Represents an item within an itinerary, corresponding to a specific day.
    """
    itinerary = models.ForeignKey(
        Itinerario, on_delete=models.CASCADE, related_name='items'
    )
    day = models.IntegerField()  # Day in the itinerary
    points = models.ManyToManyField(
        PointOfInterest, related_name='itinerary_items'
    )

    class Meta:
        '''Ordenar por itinerario y luego por día'''
        ordering = ['itinerary__id', 'day']

    def __str__(self):
        points_names = ", ".join([point.name for point in self.points.all()])
        return f"{self.itinerary.name} - Day {self.day}: {points_names}"
