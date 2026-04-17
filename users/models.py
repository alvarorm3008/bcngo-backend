"""
Este módulo contiene la definición de modelos para la aplicación 'users'.
"""

import re
from django.db import models
from django.contrib.auth.models import AbstractUser


class Users(AbstractUser):
    """
    Modelo de usuario extendido que utiliza el correo electrónico como identificador único.
    """
    email = models.EmailField(unique=True)
    password = models.CharField(max_length=255, default="", null=True)
    username = models.CharField(max_length=255, unique=True)
    notifications = models.BooleanField(default=True)
    google = models.BooleanField(default=False)

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = []

    class Meta:  # pylint: disable=too-few-public-methods
        """
        Meta información del modelo 'Users'.
        """
        verbose_name = 'User'
        verbose_name_plural = 'Users'

    def __str__(self):
        """
        Devuelve una representación legible del usuario.
        """
        return str(self.username)

    def is_valid(self):
        """
        Valida los campos del usuario: email y username.
        """
        if not re.findall(r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$", self.email):
            raise ValueError("Invalid email")
