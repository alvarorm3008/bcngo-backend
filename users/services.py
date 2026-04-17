"""
Este módulo contiene los servicios relacionados con el modelo 'Users'.
"""

from .models import Users

def create_user(data):
    """
    Crea un nuevo usuario a partir de los datos proporcionados.
    """
    user = Users()
    user.username = data["username"]
    user.email = data["email"]
    user.set_password(data["password"])

    if user.is_valid():
        user.save()
        return user

    return None
