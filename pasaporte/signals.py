"""
Señales para la creación de pasaportes al registrar usuarios.
"""

from django.db.models.signals import post_save
from django.dispatch import receiver
from django.contrib.auth import get_user_model
from puntosdeinteres.models import PointOfInterest
from .models import Passport, PassportPoint

User = get_user_model()


@receiver(post_save, sender=User)
def create_passport_for_user(sender, instance, created, **kwargs):  # pylint: disable=unused-argument
    """
    Crea un pasaporte y añade puntos esenciales para nuevos usuarios.
    """
    if created:
        print(f"Se creó un usuario: {instance.username}")

        # Crear el pasaporte
        passport = Passport.objects.create(user=instance)
        print(f"Pasaporte creado para usuario: {instance.username}")

        # Añadir puntos esenciales al pasaporte como no marcados
        essential_points = PointOfInterest.objects.filter(esencial=True)
        passport_points = [
            PassportPoint(passport=passport, point_of_interest=point, is_marked=False)
            for point in essential_points
        ]
        PassportPoint.objects.bulk_create(passport_points)
        print(f"Puntos esenciales añadidos al pasaporte de {instance.username}")
