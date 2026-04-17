"""
Configuración de la aplicación Pasaporte.
"""

from django.apps import AppConfig


class PasaporteConfig(AppConfig):
    """
    Configuración para la aplicación Pasaporte.
    """
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'pasaporte'

    def ready(self):
        """
        Método que se ejecuta cuando la configuración de la aplicación está lista.
        """
        print("PasaporteConfig ready() ejecutado")
        import pasaporte.signals  # pylint: disable=import-outside-toplevel, unused-import
