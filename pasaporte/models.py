"""
Modelos para la aplicación de Pasaporte Digital.
"""

from django.db import models
from django.conf import settings
from puntosdeinteres.models import PointOfInterest


class Passport(models.Model):
    """
    Modelo para representar un pasaporte digital de un usuario.
    """
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="passport"
    )
    points_of_interest = models.ManyToManyField(
        PointOfInterest,
        through="PassportPoint",
        related_name="passports"
    )

    def __str__(self):
        return f"Pasaporte de {self.user.username}"

    def initialize_essential_points(self):
        """
        Inicializa los puntos esenciales en el pasaporte.
        """
        essential_points = PointOfInterest.objects.filter(is_essential=True)
        for point in essential_points:
            PassportPoint.objects.get_or_create(
                passport=self,
                point_of_interest=point,
                is_marked=False  # Inicialmente no marcado
            )


class PassportPoint(models.Model):
    """
    Modelo intermedio para registrar un punto de interés en un pasaporte.
    """
    passport = models.ForeignKey(
        Passport,
        on_delete=models.CASCADE,
        related_name="passport_points"
    )
    point_of_interest = models.ForeignKey(
        PointOfInterest,
        on_delete=models.CASCADE,
        related_name="passport_points"
    )
    is_marked = models.BooleanField(default=False)  # Estado del punto en el pasaporte

    def __str__(self):
        status = "marcado" if self.is_marked else "no marcado"
        return f"{self.point_of_interest.name} ({status})"
