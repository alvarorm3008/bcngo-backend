"""
Comando personalizado para eliminar todos los puntos de interés
de la base de datos y reiniciar la secuencia de IDs.
"""

from django.core.management.base import BaseCommand
from puntosdeinteres.services import clear_pointsofinterest_table


class Command(BaseCommand):
    """
    Comando para eliminar todos los puntos de interés de la base de datos.
    """

    help = 'Elimina todos los puntos de interés de la base de datos y reinicia la secuencia de IDs.'

    def handle(self, *args, **kwargs):  # pylint: disable=unused-argument
        """
        Método principal que ejecuta el comando.
        """
        clear_pointsofinterest_table()
        self.stdout.write(self.style.SUCCESS('Puntos eliminados.'))
