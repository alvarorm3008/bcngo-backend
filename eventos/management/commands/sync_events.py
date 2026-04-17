"""
Comando para sincronizar eventos culturales desde la API de Open Data Barcelona.
"""

from django.core.management.base import BaseCommand
from eventos.services import sync_events_from_api

class Command(BaseCommand):
    """
    Comando personalizado para sincronizar eventos desde la API de Open Data Barcelona.
    """
    help = 'Sincroniza eventos desde la API de Open Data Barcelona'

    def handle(self, *args, **kwargs):  # pylint: disable=unused-argument
        sync_events_from_api()
        self.stdout.write(self.style.SUCCESS('Sincronización completada'))
