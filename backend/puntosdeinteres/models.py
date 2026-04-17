"""
Este módulo define los modelos para la aplicación de Puntos de Interés.
Incluye el modelo PointOfInterest, que representa un punto de interés
con varios atributos como nombre, dirección, coordenadas y categoría,
y el modelo Review, que gestiona las reseñas de los puntos de interés.
"""

from django.db import models

class PointOfInterest(models.Model):
    """
    Representa un punto de interés con su nombre, ubicación, información de contacto
    y otros detalles relevantes.
    """
    name = models.CharField(max_length=255)
    register_id = models.CharField(max_length=255, null=True, blank=True)
    address_name = models.CharField(max_length=255, null=True, blank=True)
    street_number = models.CharField(max_length=255, null=True, blank=True)
    latitude = models.FloatField(null=True, blank=True)
    longitude = models.FloatField(null=True, blank=True)
    phone = models.CharField(max_length=20, null=True, blank=True)
    web_url = models.URLField(null=True, blank=True)
    category = models.CharField(max_length=255, null=True, blank=True)
    esencial = models.BooleanField(default=False)
    modified = models.DateTimeField(null = True)  # Fecha de modificación

    def __str__(self):
        """
        Devuelve una representación en forma de cadena de la instancia de PointOfInterest.
        """
        return str(self.name)


class Review(models.Model):
    """
    Representa una reseña asociada a un punto de interés.
    """
    point_of_interest = models.ForeignKey(PointOfInterest, on_delete=models.CASCADE, related_name="reviews")
    username = models.CharField(max_length=150)
    user_email = models.EmailField()
    comment = models.TextField(null=True, blank=True)
    rating = models.PositiveSmallIntegerField()
    reports_count = models.PositiveIntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        """
        Devuelve una representación en forma de cadena de la reseña.
        """
        return f"Reseña de {self.username} en {self.point_of_interest.name}"
