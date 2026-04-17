"""
Comando personalizado para sincronizar puntos de interés desde la API de Open Data Barcelona.
"""

from django.core.management.base import BaseCommand
from puntosdeinteres.services import sync_points_of_interest_from_api


class Command(BaseCommand):
    """
    Comando para sincronizar puntos de interés desde la API de Open Data Barcelona.
    """

    help = 'Sincroniza puntos de interés desde la API de Open Data Barcelona.'

    def handle(self, *args, **kwargs):  # pylint: disable=unused-argument
        """
        Método principal que ejecuta el comando.
        """
        sync_points_of_interest_from_api()
        self.stdout.write(self.style.SUCCESS('Sincronización de puntos de interés completada.'))
