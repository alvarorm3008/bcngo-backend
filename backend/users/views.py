"""
Vistas para la aplicación de usuarios.
"""

import datetime
import jwt

from django.conf import settings
from django.contrib.auth.tokens import PasswordResetTokenGenerator
from django.core.cache import cache
from django.core.mail import send_mail
from django.http import HttpResponseBadRequest
from django.shortcuts import render, redirect
from django.urls import reverse
from django.utils.encoding import force_bytes
from django.utils.http import urlsafe_base64_decode, urlsafe_base64_encode

from firebase_admin import auth
from rest_framework.exceptions import AuthenticationFailed
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

from users.models import Users
from users.serializers import UserSerializer, UserViewSerializer

# pylint: disable=too-many-lines

class RegisterApiView(APIView):
    """
    Vista API para registrar un nuevo usuario utilizando un serializador.
    """

    @swagger_auto_schema(
        operation_description="Registra un nuevo usuario en la aplicación.",
        request_body=UserSerializer,
        responses={
            201: openapi.Response(
                description="Usuario creado exitosamente",
                schema=UserSerializer
            ),
            400: "Error de validación en los datos enviados",
        },
    )
    def post(self, request):
        """
        Maneja solicitudes POST para crear un usuario.
        """
        serializer = UserSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        payload = {
            'id': user.id,
            'email': user.email,
            'username': user.username,
            'exp': datetime.datetime.utcnow() + datetime.timedelta(days=150),
            'iat': datetime.datetime.utcnow()
        }
        token = jwt.encode(payload, settings.SECRET_KEY, algorithm='HS256')
        return Response({'jwt': token}, status=status.HTTP_201_CREATED)


class LoginGoogleView(APIView):
    """
    Vista API para el inicio de sesión del usuario con Google.
    """

    def post(self, request):
        """
        Maneja solicitudes POST para comprobar que el token de Firebase es válido
        y devolver un token JWT para acceder a las vistas del servidor.
        """
        firebase_token = request.data.get('token')

        if not firebase_token:
            return Response({'detail': 'Token de Firebase no proporcionado.'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            # Verificar el token de Firebase
            decoded_token = auth.verify_id_token(firebase_token)
            email = decoded_token['email']

        except Exception:  # pylint: disable=broad-exception-caught
            return Response({'detail': 'Token inválido o expirado.'}, status=status.HTTP_401_UNAUTHORIZED)

        # Intentar obtener el usuario o crearlo si no existe
        user, create = Users.objects.get_or_create(email=email)

        # Verificar si el usuario está bloqueado
        if not user.is_active:
            return Response({'detail': 'La cuenta está bloqueada.'}, status=status.HTTP_403_FORBIDDEN)

        # Si el usuario fue creado o el campo google es False, actualizarlo
        if create or not user.google:
            user.google = True
            user.username = self.generate_unique_username(email.split('@')[0])
            user.save()


        payload = {
            'id': user.id,
            'email': user.email,
            'username': user.username,
            'exp': datetime.datetime.utcnow() + datetime.timedelta(days=150),
            'iat': datetime.datetime.utcnow()
        }
        token = jwt.encode(payload, settings.SECRET_KEY, algorithm='HS256')
        return Response({'jwt': token}, status=status.HTTP_200_OK)
    
    @staticmethod
    def generate_unique_username(base_username):
        """
        Genera un username único basado en el nombre base.
        """
        username = base_username
        counter = 1
        while Users.objects.filter(username=username).exists():
            username = f"{base_username}{counter}"
            counter += 1
        return username


class LoginView(APIView):
    """
    Vista API para el inicio de sesión del usuario.
    """

    @swagger_auto_schema(
        operation_description="Autentica al usuario y devuelve un token JWT.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                'email': openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Correo electrónico del usuario",
                    example="user@example.com"
                ),
                'password': openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Contraseña del usuario",
                    example="mypassword123"
                ),
            },
            required=['email', 'password']
        ),
        responses={
            200: openapi.Response(
                description="Autenticación exitosa. Devuelve un token JWT.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        'jwt': openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Token de acceso JWT"
                        )
                    }
                )
            ),
            401: "Usuario o contraseña incorrectos",
            403: "El usuario está bloqueado",
        },
    )
    def post(self, request):
        """
        Maneja solicitudes POST para autenticar al usuario y devolver un token JWT.
        """
        email = request.data.get('email')
        password = request.data.get('password')
        user = Users.objects.filter(email=email).first()
        
        if user is None:
            raise AuthenticationFailed('Usuario no encontrado')
        
        # Verificar si el usuario está bloqueado
        if not user.is_active:
            raise AuthenticationFailed('La cuenta está bloqueada')

        if not user.check_password(password):
            raise AuthenticationFailed('Contraseña incorrecta')

        payload = {
            'id': user.id,
            'email': user.email,
            'username': user.username,
            'exp': datetime.datetime.utcnow() + datetime.timedelta(days=150),
            'iat': datetime.datetime.utcnow()
        }
        token = jwt.encode(payload, settings.SECRET_KEY, algorithm='HS256')
        return Response({'jwt': token}, status=status.HTTP_200_OK)


class IsGoogleView(APIView):
    """
    API View para comprobar si un usuario se ha registrado con Google.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Comprueba si el usuario autenticado se ha registrado con Google.",
        responses={  # pylint: disable=duplicate-key
            200: "El usuario se ha registrado con Google.",
            200: "El usuario no se ha registrado con Google."
        }
    )
    def get(self, request):
        """
        Devuelve si el usuario autenticado se ha registrado con Google.
        """
        return Response({"is_google": request.user.google}, status=status.HTTP_200_OK)

class DeleteProfileView(APIView):
    """
    Vista API para eliminar el perfil del usuario autenticado.
    """
    permission_classes = [IsAuthenticated]  # Requiere que el usuario esté autenticado

    @swagger_auto_schema(
        operation_description="Elimina el perfil del usuario autenticado.",
        responses={
            200: "Cuenta eliminada exitosamente.",
            401: "Autenticación requerida para acceder a este endpoint.",
        },
    )
    def delete(self, request):
        """
        Maneja solicitudes DELETE para eliminar el perfil del usuario autenticado.
        """
        try:
            user = request.user
            user.delete()
            return Response(status=status.HTTP_200_OK)
        except Exception as e:  # pylint: disable=unused-variable,broad-exception-caught
            return Response({'detail': 'Error al eliminar la cuenta.'}, status=status.HTTP_400_BAD_REQUEST)


class ProfileView(APIView):
    """
    Vista API para consultar el perfil del usuario autenticado.
    """
    permission_classes = [IsAuthenticated]  # Requiere que el usuario esté autenticado

    @swagger_auto_schema(
        operation_description="Obtiene los datos del perfil del usuario autenticado.",
        responses={
            200: openapi.Response(
                description="Datos del perfil del usuario autenticado",
                schema=UserViewSerializer
            ),
            401: "Autenticación requerida para acceder a este endpoint",
        },
    )
    def get(self, request):
        """
        Maneja solicitudes GET para obtener el perfil del usuario autenticado.
        """
        user = request.user
        serializer = UserViewSerializer(user)
        return Response(serializer.data, status=status.HTTP_200_OK)


class EditProfileView(APIView):
    """
    Vista API para editar el perfil del usuario autenticado.
    """
    permission_classes = [IsAuthenticated]  # Requiere que el usuario esté autenticado

    @swagger_auto_schema(
        operation_description="Actualiza el nombre de usuario del usuario autenticado.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                'username': openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Nuevo nombre de usuario",
                    example="newusername123"
                ),
            },
            required=['username']
        ),
        responses={
            200: openapi.Response(
                description="Perfil actualizado exitosamente",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        'username': openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Nombre de usuario actualizado"
                        )
                    }
                )
            ),
            400: "Error de validación en los datos enviados",
            401: "Autenticación requerida para acceder a este endpoint",
        },
    )
    def patch(self, request):
        """
        Maneja solicitudes PATCH para actualizar el perfil del usuario.
        Permite modificar el username y las notificaciones.
        """
        user = request.user
        username = request.data.get('username')
        notifications = request.data.get('notifications')

        # Validar si hay al menos un campo para actualizar
        if username is None and notifications is None:
            return Response(
                {"error": "Debe proporcionar al menos 'username' o 'notifications'."},
                status=status.HTTP_400_BAD_REQUEST
            )

        # Validar y actualizar username si está presente
        if username is not None:
            user.username = username

        # Validar y actualizar notifications si está presente
        if notifications is not None:
            if not isinstance(notifications, bool):
                return Response(
                    {"error": "El campo 'notifications' debe ser un valor booleano."},
                    status=status.HTTP_400_BAD_REQUEST
                )
            user.notifications = notifications

        # Guardar los cambios en el usuario
        user.save()

        return Response({
            "username": user.username,
            "notifications": user.notifications
        }, status=status.HTTP_200_OK)


class ChangePasswordView(APIView):
    """
    Vista API para cambiar la contraseña del usuario autenticado.
    """
    permission_classes = [IsAuthenticated]  # Requiere que el usuario esté autenticado

    @swagger_auto_schema(
        operation_description="Cambia la contraseña del usuario autenticado.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                'current_password': openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Contraseña actual del usuario",
                    example="currentpassword123"
                ),
                'new_password': openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Nueva contraseña del usuario",
                    example="newpassword456"
                ),
            },
            required=['current_password', 'new_password']
        ),
        responses={
            200: openapi.Response(
                description="Contraseña cambiada exitosamente."
            ),
            400: "Error en los datos enviados.",
            401: "Autenticación requerida para acceder a este endpoint.",
        },
    )
    def post(self, request):
        """
        Maneja solicitudes POST para cambiar la contraseña del usuario autenticado.
        """
        user = request.user
        current_password = request.data.get('current_password')
        new_password = request.data.get('new_password')

        # Verificar que la contraseña actual es correcta
        if not user.check_password(current_password):
            return Response(
                {"error": "La contraseña actual es incorrecta."},
                status=status.HTTP_400_BAD_REQUEST
            )

        # Cambiar la contraseña
        user.set_password(new_password)
        user.save()

        return Response(
            {"message": "Contraseña cambiada exitosamente."},
            status=status.HTTP_200_OK
        )


class IsAdminView(APIView):
    """
    API View para comprobar si un usuario es administrador.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Comprueba si el usuario autenticado es administrador.",
        responses={
            200: "El usuario es administrador.",
            403: "El usuario no es administrador."
        }
    )
    def get(self, request):
        """
        Devuelve si el usuario autenticado es administrador.
        """
        if request.user.is_staff:
            return Response({"is_admin": True}, status=status.HTTP_200_OK)
        return Response({"is_admin": False}, status=status.HTTP_403_FORBIDDEN)


class CheckIfUserIsAdminView(APIView):
    """
    API View para comprobar si otro usuario es administrador, proporcionando su correo.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description=(
            "Permite a un administrador verificar si un usuario es administrador "
            "proporcionando su correo."
        ),
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                "email": openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Correo electrónico del usuario que se desea verificar.",
                    example="usuario@example.com"
                ),
            },
            required=["email"],
        ),
        responses={
            200: openapi.Response(
                description="Estado del usuario",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "is_admin": openapi.Schema(
                            type=openapi.TYPE_BOOLEAN,
                            description="Indica si el usuario es administrador."
                        )
                    }
                )
            ),
            400: "El campo email es obligatorio o el usuario no existe.",
            401: "Autenticación requerida.",
        },
    )
    def post(self, request):
        """
        Comprueba si otro usuario es administrador, proporcionando su correo.
        """
        email = request.data.get("email")
        if not email:
            return Response(
                {"error": "El campo 'email' es obligatorio."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            user = Users.objects.get(email=email)
            return Response({"is_admin": user.is_staff}, status=status.HTTP_200_OK)
        except Users.DoesNotExist:
            return Response(
                {"error": "El usuario con este correo no existe."},
                status=status.HTTP_400_BAD_REQUEST,
            )


class UserListView(APIView):
    """
    Vista API para obtener una lista de todos los correos electrónicos de los usuarios registrados.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Devuelve una lista de los correos electrónicos de todos los usuarios registrados.",
        responses={
            200: openapi.Response(
                description="Lista de correos electrónicos.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "emails": openapi.Schema(
                            type=openapi.TYPE_ARRAY,
                            items=openapi.Schema(type=openapi.TYPE_STRING),
                            description="Lista de correos electrónicos."
                        )
                    }
                )
            ),
            401: "Acceso denegado: autenticación requerida.",
        },
    )
    def get(self, request):  # pylint: disable=unused-argument
        """
        Maneja solicitudes GET para devolver todos los correos electrónicos de los usuarios.
        """
        emails = Users.objects.values_list('email', flat=True)
        return Response({"emails": list(emails)}, status=status.HTTP_200_OK)


class MakeAdminView(APIView):
    """
    Vista API para asignar a un usuario como administrador (is_staff = True).
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Asigna a un usuario como administrador estableciendo is_staff = True.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                "email": openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Correo electrónico del usuario que se convertirá en administrador.",
                    example="usuario@example.com"
                ),
            },
            required=["email"],
        ),
        responses={
            200: openapi.Response(
                description="Usuario actualizado exitosamente.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "message": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Mensaje de confirmación."
                        ),
                        "email": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Correo electrónico del usuario actualizado."
                        )
                    }
                )
            ),
            400: "El usuario no existe o ya es administrador.",
            401: "Autenticación requerida.",
        },
    )
    def post(self, request):
        """
        Maneja solicitudes POST para convertir a un usuario en administrador.
        """
        email = request.data.get("email")
        if not email:
            return Response({"error": "El campo 'email' es obligatorio."}, status=status.HTTP_400_BAD_REQUEST)

        try:
            user = Users.objects.get(email=email)
            if user.is_staff:
                return Response(
                    {"error": "El usuario ya es administrador."},
                    status=status.HTTP_400_BAD_REQUEST
                )
            user.is_staff = True
            user.save()
            return Response(
                {"message": "Usuario actualizado exitosamente.", "email": user.email},
                status=status.HTTP_200_OK
            )
        except Users.DoesNotExist:
            return Response(
                {"error": "El usuario con este correo no existe."},
                status=status.HTTP_400_BAD_REQUEST
            )

class RemoveAdminView(APIView):
    """
    Vista API para quitar permisos de administrador (is_staff = False) a un usuario.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Quita permisos de administrador (is_staff = False) a un usuario.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                "email": openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Correo electrónico del usuario al que se quitarán los permisos de administrador.",
                    example="usuario@example.com"
                ),
            },
            required=["email"],
        ),
        responses={
            200: openapi.Response(
                description="Permisos de administrador quitados exitosamente.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "message": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Mensaje de confirmación."
                        ),
                        "email": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Correo electrónico del usuario actualizado."
                        )
                    }
                )
            ),
            400: "El usuario no existe, no es administrador o es un superusuario.",
            401: "Autenticación requerida.",
        },
    )
    def post(self, request):
        """
        Maneja solicitudes POST para quitar permisos de administrador.
        """
        email = request.data.get("email")
        if not email:
            return Response({"error": "El campo 'email' es obligatorio."}, status=status.HTTP_400_BAD_REQUEST)

        try:
            user = Users.objects.get(email=email)
            if not user.is_staff:
                return Response(
                    {"error": "El usuario no es administrador."},
                    status=status.HTTP_400_BAD_REQUEST
                )
            if user.is_superuser:
                return Response(
                    {"error": "No se pueden quitar permisos de administrador a un superusuario."},
                    status=status.HTTP_400_BAD_REQUEST
                )
            user.is_staff = False
            user.save()
            return Response(
                {"message": "Permisos de administrador quitados exitosamente.", "email": user.email},
                status=status.HTTP_200_OK
            )
        except Users.DoesNotExist:
            return Response(
                {"error": "El usuario con este correo no existe."},
                status=status.HTTP_400_BAD_REQUEST
            )


class BlockUserView(APIView):
    """
    Vista API para bloquear la cuenta de un usuario (is_active = False).
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Bloquea la cuenta de un usuario estableciendo is_active = False.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                "email": openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Correo electrónico del usuario que se bloqueará.",
                    example="usuario@example.com"
                ),
            },
            required=["email"],
        ),
        responses={
            200: openapi.Response(
                description="Usuario bloqueado exitosamente.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "message": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Mensaje de confirmación."
                        ),
                        "email": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Correo electrónico del usuario bloqueado."
                        )
                    }
                )
            ),
            400: "El usuario no existe, ya está bloqueado o es un superusuario.",
            401: "Autenticación requerida.",
        },
    )
    def post(self, request):
        """
        Maneja solicitudes POST para bloquear un usuario.
        """
        email = request.data.get("email")
        if not email:
            return Response({"error": "El campo 'email' es obligatorio."}, status=status.HTTP_400_BAD_REQUEST)

        try:
            user = Users.objects.get(email=email)

            if user.is_superuser:
                return Response(
                    {"error": "No se puede bloquear a un superusuario."},
                    status=status.HTTP_400_BAD_REQUEST
                )

            if not user.is_active:
                return Response(
                    {"error": "El usuario ya está bloqueado."},
                    status=status.HTTP_400_BAD_REQUEST
                )

            user.is_active = False
            user.save()

            return Response(
                {"message": "Usuario bloqueado exitosamente.", "email": user.email},
                status=status.HTTP_200_OK
            )
        except Users.DoesNotExist:
            return Response(
                {"error": "El usuario con este correo no existe."},
                status=status.HTTP_400_BAD_REQUEST
            )


class ListBlockedUsersView(APIView):
    """
    Vista API para obtener una lista de todos los usuarios bloqueados.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Devuelve una lista de los correos electrónicos de los usuarios bloqueados.",
        responses={
            200: openapi.Response(
                description="Lista de usuarios bloqueados.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "blocked_users": openapi.Schema(
                            type=openapi.TYPE_ARRAY,
                            items=openapi.Schema(type=openapi.TYPE_STRING),
                            description="Lista de correos electrónicos de usuarios bloqueados."
                        )
                    }
                )
            ),
            401: "Autenticación requerida.",
        },
    )
    def get(self, request):  # pylint: disable=unused-argument
        """
        Maneja solicitudes GET para devolver todos los usuarios bloqueados.
        """
        blocked_users = Users.objects.filter(is_active=False).values_list('email', flat=True)
        return Response({"blocked_users": list(blocked_users)}, status=status.HTTP_200_OK)


class UnblockUserView(APIView):
    """
    Vista API para desbloquear la cuenta de un usuario (is_active = True).
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_description="Desbloquea la cuenta de un usuario estableciendo is_active = True.",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                "email": openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Correo electrónico del usuario que se desbloqueará.",
                    example="usuario@example.com"
                ),
            },
            required=["email"],
        ),
        responses={
            200: openapi.Response(
                description="Usuario desbloqueado exitosamente.",
                schema=openapi.Schema(
                    type=openapi.TYPE_OBJECT,
                    properties={
                        "message": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Mensaje de confirmación."
                        ),
                        "email": openapi.Schema(
                            type=openapi.TYPE_STRING,
                            description="Correo electrónico del usuario desbloqueado."
                        )
                    }
                )
            ),
            400: "El usuario no existe o ya está desbloqueado.",
            401: "Autenticación requerida.",
        },
    )
    def post(self, request):
        """
        Maneja solicitudes POST para desbloquear un usuario.
        """
        email = request.data.get("email")
        if not email:
            return Response({"error": "El campo 'email' es obligatorio."}, status=status.HTTP_400_BAD_REQUEST)

        try:
            user = Users.objects.get(email=email)
            if user.is_active:
                return Response(
                    {"error": "El usuario ya está desbloqueado."},
                    status=status.HTTP_400_BAD_REQUEST
                )
            user.is_active = True
            user.save()
            return Response(
                {"message": "Usuario desbloqueado exitosamente.", "email": user.email},
                status=status.HTTP_200_OK
            )
        except Users.DoesNotExist:
            return Response(
                {"error": "El usuario con este correo no existe."},
                status=status.HTTP_400_BAD_REQUEST
            )


class CheckUserBlockedView(APIView):
    """
    Endpoint para comprobar si el usuario autenticado está bloqueado.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_summary="Comprobar si el usuario autenticado está bloqueado",
        operation_description="Verifica si la cuenta del usuario autenticado está bloqueada.",
        responses={
            200: openapi.Response(
                description="Estado del usuario",
                examples={
                    "application/json": {
                        "is_blocked": True,
                    }
                },
            ),
            403: openapi.Response(
                description="El usuario no está autenticado.",
            ),
        },
    )
    def get(self, request):
        """
        Comprueba si el usuario autenticado está bloqueado.
        """
        is_blocked = not request.user.is_active
        return Response({"is_blocked": is_blocked}, status=status.HTTP_200_OK)


class CheckUserBlockedByEmailView(APIView):
    """
    Endpoint para que un administrador compruebe si un usuario está bloqueado por su correo.
    """
    permission_classes = [IsAuthenticated]

    @swagger_auto_schema(
        operation_summary="Comprobar si un usuario está bloqueado por correo",
        operation_description=(
            "Permite a un administrador verificar si un usuario está bloqueado "
            "proporcionando su correo."
        ),
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            properties={
                "email": openapi.Schema(
                    type=openapi.TYPE_STRING,
                    description="Correo electrónico del usuario a verificar.",
                    example="usuario@example.com"
                ),
            },
            required=["email"],
        ),
        responses={
            200: openapi.Response(
                description="Estado del usuario",
                examples={
                    "application/json": {
                        "email": "usuario@example.com",
                        "is_blocked": True,
                    }
                },
            ),
            400: "El campo 'email' es obligatorio o el usuario no existe.",
            403: "Permisos insuficientes.",
        },
    )
    def post(self, request):
        """
        Verifica si un usuario está bloqueado por su correo.
        """
        email = request.data.get("email")
        if not email:
            return Response(
                {"error": "El campo 'email' es obligatorio."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            user = Users.objects.get(email=email)
            is_blocked = not user.is_active
            return Response(
                {"is_blocked": is_blocked},
                status=status.HTTP_200_OK,
            )
        except Users.DoesNotExist:
            return Response(
                {"error": "El usuario con este correo no existe."},
                status=status.HTTP_400_BAD_REQUEST,
            )


class PasswordResetRequestView(APIView):
    """
    Vista para solicitar un enlace de restablecimiento de contraseña.
    """

    @swagger_auto_schema(
        operation_summary="Solicitar restablecimiento de contraseña",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=["email"],
            properties={
                "email": openapi.Schema(type=openapi.TYPE_STRING, description="Correo electrónico del usuario")
            },
        ),
        responses={
            200: openapi.Response(
                description="Correo de recuperación enviado con éxito.",
                examples={
                    "application/json": {"message": "Correo de recuperación enviado con éxito."}
                },
            ),
            404: openapi.Response(
                description="Usuario no encontrado.",
                examples={
                    "application/json": {"error": "Usuario no encontrado."}
                },
            ),
        },
    )
    def post(self, request):
        """
        Solicita un enlace de restablecimiento de contraseña para un usuario.
        """
        email = request.data.get("email")

        try:
            user = Users.objects.get(email=email)
        except Users.DoesNotExist:
            return Response({"error": "Usuario no encontrado."}, status=status.HTTP_404_NOT_FOUND)

        if user.google:
            return Response({"error": "No se puede restablecer la contraseña de un usuario de Google."},
                            status=status.HTTP_400_BAD_REQUEST)

        token_generator = PasswordResetTokenGenerator()
        token = token_generator.make_token(user)
        reset_url = request.build_absolute_uri(
            reverse('password-reset-confirm', kwargs={'uid': urlsafe_base64_encode(force_bytes(user.id)),
                                                      'token': token})
        )

        # Enviar correo electrónico con el enlace
        send_mail(
            subject="Restablecimiento de contraseña",
            message=f"Utiliza este enlace para restablecer tu contraseña: {reset_url}",
            from_email=settings.DEFAULT_FROM_EMAIL,
            recipient_list=[user.email],
        )

        return Response({"message": "Correo de recuperación enviado con éxito."}, status=status.HTTP_200_OK)


class PasswordResetConfirmView(APIView):
    """
    Vista para confirmar el restablecimiento de contraseña y establecer una nueva.
    """

    @swagger_auto_schema(
        operation_summary="Mostrar formulario para restablecer contraseña",
        manual_parameters=[
            openapi.Parameter("uid", openapi.IN_PATH, description="UID del usuario", type=openapi.TYPE_STRING),
            openapi.Parameter("token", openapi.IN_PATH, description="Token de restablecimiento",
                              type=openapi.TYPE_STRING),
        ],
        responses={
            200: openapi.Response(
                description="Formulario de restablecimiento cargado con éxito.",
            ),
            400: openapi.Response(
                description="El enlace es inválido, ha expirado o ya fue utilizado.",
            ),
        },
    )
    def get(self, request, uid, token):
        """
        Muestra el formulario de restablecimiento de contraseña.
        """
        print(f"[DEBUG] GET request recibido con UID: {uid} y Token: {token}")

        # Verificar si el token está marcado como utilizado
        cache_key = f"password_reset_token_{token}"
        if cache.get(cache_key):
            print(f"[DEBUG] Token ya utilizado: {token}")
            return HttpResponseBadRequest("El enlace ya fue utilizado para cambiar la contraseña.")

        print(f"[DEBUG] Token válido para uso: {token}")
        return render(request, "users/password_reset_confirm.html", {"uid": uid, "token": token})

    @swagger_auto_schema(
        operation_summary="Procesar nueva contraseña",
        request_body=openapi.Schema(
            type=openapi.TYPE_OBJECT,
            required=["password", "password_confirm"],
            properties={
                "password": openapi.Schema(type=openapi.TYPE_STRING, description="Nueva contraseña"),
                "password_confirm": openapi.Schema(
                    type=openapi.TYPE_STRING, description="Confirmación de la nueva contraseña"
                ),
            },
        ),
        responses={
            302: openapi.Response(
                description="Redirige a la página de éxito tras restablecer la contraseña.",
            ),
            400: openapi.Response(
                description="Error en el restablecimiento de la contraseña.",
            ),
        },
    )
    def post(self, request, uid, token):  # pylint: disable=too-many-return-statements
        """
        Procesa la nueva contraseña.
        """
        print("[DEBUG] Iniciando proceso de validación del enlace.")

        # Verificar si el token ya fue utilizado
        cache_key = f"password_reset_token_{token}"
        if cache.get(cache_key):
            print(f"[ERROR] El token ya fue utilizado: {token}")
            return HttpResponseBadRequest("El enlace ya fue utilizado para cambiar la contraseña.")

        try:
            user_id = urlsafe_base64_decode(uid).decode()
            print(f"[DEBUG] UID decodificado: {user_id}")
            user = Users.objects.get(id=user_id)
            print(f"[DEBUG] Usuario encontrado: {user}")
        except (Users.DoesNotExist, ValueError, TypeError) as e:
            print(f"[ERROR] Error al obtener el usuario: {e}")
            return HttpResponseBadRequest("El enlace es inválido o ha expirado.")

        token_generator = PasswordResetTokenGenerator()
        if not token_generator.check_token(user, token):
            print("[ERROR] Token inválido o expirado.")
            return HttpResponseBadRequest("El enlace ha caducado o es inválido.")

        # Validar las contraseñas
        password = request.POST.get("password")
        password_confirm = request.POST.get("password_confirm")
        print(f"[DEBUG] Contraseñas recibidas: password='{password}', password_confirm='{password_confirm}'")

        if password != password_confirm:
            print("[WARNING] Las contraseñas no coinciden.")
            return render(request, "users/password_reset_confirm.html", {
                "error": "Las contraseñas no coinciden.",
                "uid": uid,
                "token": token,
            })

        # Validar requisitos de la contraseña
        if len(password) < 6 or not any(char.isdigit() for char in password):
            print("[WARNING] Contraseña no cumple con los requisitos mínimos.")
            return render(request, "users/password_reset_confirm.html", {
                "error": "La contraseña debe tener al menos 6 caracteres y contener al menos un número.",
                "uid": uid,
                "token": token,
            })

        try:
            # Establecer la nueva contraseña
            print("[DEBUG] Estableciendo nueva contraseña.")
            user.set_password(password)
            user.save()
            print("[DEBUG] Contraseña actualizada con éxito.")

            # Marcar el token como utilizado en la caché
            cache.set(cache_key, True, timeout=600)
            print(f"[DEBUG] Token marcado como utilizado: {token}")

            # Redirigir al usuario a una página de éxito
            return redirect("password_reset_success")
        except Exception as e:  # pylint: disable=broad-exception-caught
            print(f"[ERROR] Error al guardar la nueva contraseña: {e}")
            return HttpResponseBadRequest("Ha ocurrido un error al restablecer la contraseña. Inténtalo de nuevo.")


class PasswordResetSuccessView(APIView):
    """
    Vista para mostrar un mensaje de éxito tras restablecer la contraseña.
    """

    def get(self, request):
        """
        Renderiza la página de éxito.
        """
        return render(request, "users/password_reset_success.html")
    

class DeleteUserAdmin(APIView):
    """
    Vista para eliminar un usuario por un administrador.
    """
    permission_classes = [IsAuthenticated]

    def delete(self, request, id):
        """
        Elimina un usuario por un administrador.
        """
        try:
            user = Users.objects.get(id=id)
            user.delete()
            return Response(status=status.HTTP_200_OK)
        except Users.DoesNotExist:
            return Response({'detail': 'Usuario no encontrado.'}, status=status.HTTP_404_NOT_FOUND)
        except Exception as e:  # pylint: disable=broad-except
            return Response({'detail': 'Error al eliminar el usuario.'}, status=status.HTTP_400_BAD_REQUEST)
    
                
