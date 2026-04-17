"""
Este módulo proporciona la autenticación basada en tokens JWT para la aplicación 'users'.
"""

import jwt
from django.conf import settings
from rest_framework.authentication import BaseAuthentication
from rest_framework.exceptions import AuthenticationFailed
from users.models import Users

class JWTAuthentication(BaseAuthentication):
    """
    Clase de autenticación personalizada para manejar tokens JWT.
    """
    def authenticate(self, request):
        """
        Autentica al usuario basado en el token JWT proporcionado en 
            los encabezados de la solicitud.
        """
        auth_header = request.headers.get('Authorization')
        if not auth_header:
            return None

        try:
            token = auth_header.split(' ')[1]  # 'Bearer <token>'
            payload = jwt.decode(token, settings.SECRET_KEY, algorithms=['HS256'])
        except jwt.ExpiredSignatureError as exc:
            raise AuthenticationFailed('El token ha expirado') from exc
        except jwt.InvalidTokenError as exc:
            raise AuthenticationFailed('Token inválido') from exc

        user = Users.objects.filter(id=payload['id']).first()
        if not user:
            raise AuthenticationFailed('Usuario no encontrado')

        return (user, token)

    def authenticate_header(self, request):
        """
        Define el esquema de autenticación que espera el cliente.
        """
        return 'Bearer'
