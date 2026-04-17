"""
Este módulo contiene pruebas para los endpoints de la aplicación 'users'.
"""

import datetime
import jwt
from django.conf import settings
from django.urls import reverse
from rest_framework.test import APITestCase
from rest_framework import status
from users.models import Users


class ProfileViewTest(APITestCase):
    """
    Pruebas para el endpoint 'ProfileView'.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        self.user = Users.objects.create_user(
            username="testuser",
            email="testuser@example.com",
            password="testpassword123"
        )
        self.token = jwt.encode(
            {
                'id': self.user.id,
                'email': self.user.email,
                'username': self.user.username,
            },
            settings.SECRET_KEY,
            algorithm='HS256'
        )
        self.url = reverse('ProfileView')  # Genera la URL dinámicamente

    def test_profile_view_authenticated(self):
        """
        Verifica que un usuario autenticado puede obtener su perfil.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        response = self.client.get(self.url)  # Llama a la URL generada dinámicamente
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['email'], self.user.email)
        self.assertEqual(response.data['username'], self.user.username)

    def test_profile_view_unauthenticated(self):
        """
        Verifica que un usuario no autenticado recibe un error 401.
        """
        response = self.client.get(self.url)  # Llama a la URL generada dinámicamente
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_profile_view_invalid_token(self):
        """
        Verifica que un token inválido genera un error 401.
        """
        self.client.credentials(HTTP_AUTHORIZATION='Bearer invalidtoken')
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_profile_view_expired_token(self):
        """
        Verifica que un token expirado genera un error 401.
        """
        expired_token = jwt.encode(
            {
                'id': self.user.id,
                'email': self.user.email,
                'username': self.user.username,
                'exp': datetime.datetime.utcnow() - datetime.timedelta(days=1),
                'iat': datetime.datetime.utcnow() - datetime.timedelta(days=2),
            },
            settings.SECRET_KEY,
            algorithm='HS256'
        )
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {expired_token}')
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class DeleteProfileViewTest(APITestCase):
    """
    Pruebas para el endpoint 'DeleteProfileView'.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        self.user = Users.objects.create_user(
            username="deleteuser",
            email="deleteuser@example.com",
            password="deletepassword123"
        )
        self.token = jwt.encode(
            {
                'id': self.user.id,
                'email': self.user.email,
                'username': self.user.username,
            },
            settings.SECRET_KEY,
            algorithm='HS256'
        )
        self.url = reverse('DeleteProfileView')

    def test_delete_profile_authenticated(self):
        """
        Verifica que un usuario autenticado puede eliminar su cuenta.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        response = self.client.delete(self.url)

        # Verifica el código de estado actualizado
        self.assertEqual(response.status_code, status.HTTP_200_OK)

        # Asegúrate de que el usuario fue eliminado
        self.assertFalse(Users.objects.filter(id=self.user.id).exists())

    def test_delete_profile_unauthenticated(self):
        """
        Verifica que un usuario no autenticado recibe un error 401.
        """
        response = self.client.delete(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_delete_profile_invalid_token(self):
        """
        Verifica que un token inválido genera un error 401.
        """
        self.client.credentials(HTTP_AUTHORIZATION='Bearer invalidtoken')
        response = self.client.delete(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_delete_profile_expired_token(self):
        """
        Verifica que un token expirado genera un error 401.
        """
        expired_token = jwt.encode(
            {
                'id': self.user.id,
                'email': self.user.email,
                'username': self.user.username,
                'exp': datetime.datetime.utcnow() - datetime.timedelta(days=1),
                'iat': datetime.datetime.utcnow() - datetime.timedelta(days=2),
            },
            settings.SECRET_KEY,
            algorithm='HS256'
        )
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {expired_token}')
        response = self.client.delete(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)



class EditProfileViewTest(APITestCase):
    """
    Pruebas para el endpoint 'EditProfileView'.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        self.user = Users.objects.create_user(
            username="testuser",
            email="testuser@example.com",
            password="testpassword123",
            notifications=True
        )
        self.token = jwt.encode(
            {
                'id': self.user.id,
                'email': self.user.email,
                'username': self.user.username,
            },
            settings.SECRET_KEY,
            algorithm='HS256'
        )
        self.url = reverse('EditProfileView')

    def test_edit_profile_username(self):
        """
        Verifica que un usuario autenticado puede actualizar su username.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        payload = {'username': 'updateduser'}
        response = self.client.patch(self.url, payload, format='json')

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['username'], 'updateduser')

        self.user.refresh_from_db()
        self.assertEqual(self.user.username, 'updateduser')

    def test_edit_profile_notifications(self):
        """
        Verifica que un usuario autenticado puede actualizar las notificaciones.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        payload = {'notifications': False}
        response = self.client.patch(self.url, payload, format='json')

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['notifications'], False)

        self.user.refresh_from_db()
        self.assertEqual(self.user.notifications, False)

    def test_edit_profile_both_fields(self):
        """
        Verifica que un usuario autenticado puede actualizar ambos campos.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        payload = {'username': 'updateduser', 'notifications': False}
        response = self.client.patch(self.url, payload, format='json')

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['username'], 'updateduser')
        self.assertEqual(response.data['notifications'], False)

        self.user.refresh_from_db()
        self.assertEqual(self.user.username, 'updateduser')
        self.assertEqual(self.user.notifications, False)

    def test_edit_profile_invalid_notifications(self):
        """
        Verifica que enviar un valor no booleano en 'notifications' genera un error 400.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        payload = {'notifications': 'invalid'}
        response = self.client.patch(self.url, payload, format='json')

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('error', response.data)
        self.assertEqual(response.data['error'], "El campo 'notifications' debe ser un valor booleano.")

    def test_edit_profile_no_fields(self):
        """
        Verifica que no enviar ningún campo genera un error 400.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        payload = {}
        response = self.client.patch(self.url, payload, format='json')

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('error', response.data)
        self.assertEqual(response.data['error'], "Debe proporcionar al menos 'username' o 'notifications'.")

    def test_edit_profile_unauthenticated(self):
        """
        Verifica que un usuario no autenticado recibe un error 401.
        """
        payload = {'username': 'updateduser'}
        response = self.client.patch(self.url, payload, format='json')
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class ChangePasswordViewTest(APITestCase):
    """
    Pruebas para el endpoint 'ChangePasswordView'.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        self.user = Users.objects.create_user(
            username="testuser",
            email="testuser@example.com",
            password="oldpassword123"
        )
        self.token = jwt.encode(
            {
                'id': self.user.id,
                'email': self.user.email,
                'username': self.user.username,
            },
            settings.SECRET_KEY,
            algorithm='HS256'
        )
        self.url = reverse('ChangePasswordView')

    def test_change_password_authenticated(self):
        """
        Verifica que un usuario autenticado puede cambiar su contraseña.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        payload = {
            'current_password': 'oldpassword123',
            'new_password': 'newpassword456'
        }
        response = self.client.post(self.url, payload, format='json')

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['message'], 'Contraseña cambiada exitosamente.')

        self.user.refresh_from_db()
        self.assertTrue(self.user.check_password('newpassword456'))

    def test_change_password_invalid_current_password(self):
        """
        Verifica que si un usuario proporciona una contraseña actual incorrecta recibe un error 400.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        payload = {
            'current_password': 'wrongpassword',
            'new_password': 'newpassword456'
        }
        response = self.client.post(self.url, payload, format='json')

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('error', response.data)
        self.assertEqual(response.data['error'], 'La contraseña actual es incorrecta.')

    def test_change_password_unauthenticated(self):
        """
        Verifica que un usuario no autenticado recibe un error 401.
        """
        payload = {
            'current_password': 'oldpassword123',
            'new_password': 'newpassword456'
        }
        response = self.client.post(self.url, payload, format='json')

        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class IsAdminViewTestCase(APITestCase):
    """
    Test para el endpoint IsAdminView.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        # Crear un usuario regular
        self.user = Users.objects.create_user(
            username="user",
            email="user@example.com",
            password="password123"
        )
        # Crear un administrador
        self.admin = Users.objects.create_superuser(
            username="admin",
            email="admin@example.com",
            password="adminpassword123"
        )
        self.user_token = jwt.encode(
            {
                'id': self.user.id,
                'email': self.user.email,
                'username': self.user.username,
            },
            settings.SECRET_KEY,
            algorithm='HS256'
        )
        self.admin_token = jwt.encode(
            {
                'id': self.admin.id,
                'email': self.admin.email,
                'username': self.admin.username,
            },
            settings.SECRET_KEY,
            algorithm='HS256'
        )
        self.url = '/users/is_admin'  # URL del endpoint

    def test_is_admin_authenticated_admin(self):
        """
        Prueba que un administrador autenticado devuelve is_admin: True.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data, {"is_admin": True})

    def test_is_admin_unauthenticated_user(self):
        """
        Prueba que un usuario no autenticado no puede acceder al endpoint.
        """
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class UserListViewTest(APITestCase):
    """
    Pruebas para el endpoint 'UserListView'.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        self.user = Users.objects.create_user(
            username="user1",
            email="user1@example.com",
            password="password123"
        )
        self.user2 = Users.objects.create_user(
            username="user2",
            email="user2@example.com",
            password="password123"
        )
        self.token = self.client.post(reverse('LoginView'), {
            "email": self.user.email,
            "password": "password123"
        }).data['jwt']
        self.url = reverse('UserListView')

    def test_user_list_authenticated(self):
        """
        Verifica que un usuario autenticado puede obtener la lista de correos.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.token}')
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("emails", response.data)
        self.assertIn(self.user.email, response.data["emails"])
        self.assertIn(self.user2.email, response.data["emails"])

    def test_user_list_unauthenticated(self):
        """
        Verifica que un usuario no autenticado recibe un error 401.
        """
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class MakeAdminViewTest(APITestCase):
    """
    Pruebas para el endpoint 'MakeAdminView'.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        self.admin = Users.objects.create_superuser(
            username="admin",
            email="admin@example.com",
            password="adminpassword123"
        )
        self.user = Users.objects.create_user(
            username="user",
            email="user@example.com",
            password="userpassword123"
        )
        self.admin_token = self.client.post(reverse('LoginView'), {
            "email": self.admin.email,
            "password": "adminpassword123"
        }).data['jwt']
        self.url = reverse('MakeAdminView')

    def test_make_admin_success(self):
        """
        Verifica que un administrador puede convertir a un usuario en administrador.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        response = self.client.post(self.url, {"email": self.user.email}, format="json")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data["email"], self.user.email)
        self.assertTrue(Users.objects.get(email=self.user.email).is_staff)

    def test_make_admin_user_already_admin(self):
        """
        Verifica que no se puede convertir en administrador a un usuario que ya lo es.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        self.user.is_staff = True
        self.user.save()
        response = self.client.post(self.url, {"email": self.user.email}, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("error", response.data)
        self.assertEqual(response.data["error"], "El usuario ya es administrador.")

    def test_make_admin_user_not_found(self):
        """
        Verifica que se genera un error 400 si el usuario no existe.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        response = self.client.post(self.url, {"email": "nonexistent@example.com"}, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("error", response.data)
        self.assertEqual(response.data["error"], "El usuario con este correo no existe.")

    def test_make_admin_unauthenticated(self):
        """
        Verifica que un usuario no autenticado recibe un error 401.
        """
        response = self.client.post(self.url, {"email": self.user.email}, format="json")
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class BlockUnblockUserViewTest(APITestCase):
    """
    Pruebas para los endpoints 'BlockUserView' y 'UnblockUserView'.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        self.admin = Users.objects.create_superuser(
            username="admin",
            email="admin@example.com",
            password="adminpassword123"
        )
        self.user = Users.objects.create_user(
            username="user",
            email="user@example.com",
            password="userpassword123"
        )
        self.admin_token = self.client.post(reverse('LoginView'), {
            "email": self.admin.email,
            "password": "adminpassword123"
        }).data['jwt']
        self.block_user_url = reverse('BlockUserView')
        self.unblock_user_url = reverse('UnblockUserView')

    def test_block_user_success(self):
        """
        Verifica que un administrador puede bloquear un usuario exitosamente.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        response = self.client.post(self.block_user_url, {"email": self.user.email}, format="json")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data["email"], self.user.email)
        self.assertFalse(Users.objects.get(email=self.user.email).is_active)

    def test_block_user_already_blocked(self):
        """
        Verifica que no se puede bloquear a un usuario ya bloqueado.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        self.user.is_active = False
        self.user.save()
        response = self.client.post(self.block_user_url, {"email": self.user.email}, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("error", response.data)
        self.assertEqual(response.data["error"], "El usuario ya está bloqueado.")

    def test_unblock_user_success(self):
        """
        Verifica que un administrador puede desbloquear un usuario exitosamente.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        self.user.is_active = False
        self.user.save()
        response = self.client.post(self.unblock_user_url, {"email": self.user.email}, format="json")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data["email"], self.user.email)
        self.assertTrue(Users.objects.get(email=self.user.email).is_active)

    def test_unblock_user_already_active(self):
        """
        Verifica que no se puede desbloquear a un usuario ya activo.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        response = self.client.post(self.unblock_user_url, {"email": self.user.email}, format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("error", response.data)
        self.assertEqual(response.data["error"], "El usuario ya está desbloqueado.")

    def test_block_unblock_user_unauthenticated(self):
        """
        Verifica que un usuario no autenticado no puede bloquear o desbloquear cuentas.
        """
        block_response = self.client.post(self.block_user_url, {"email": self.user.email}, format="json")
        unblock_response = self.client.post(self.unblock_user_url, {"email": self.user.email}, format="json")
        self.assertEqual(block_response.status_code, status.HTTP_401_UNAUTHORIZED)
        self.assertEqual(unblock_response.status_code, status.HTTP_401_UNAUTHORIZED)


class ListBlockedUsersViewTest(APITestCase):
    """
    Pruebas para el endpoint 'ListBlockedUsersView'.
    """

    def setUp(self):
        """
        Configuración inicial para los tests.
        """
        self.admin = Users.objects.create_superuser(
            username="admin",
            email="admin@example.com",
            password="adminpassword123"
        )
        self.user1 = Users.objects.create_user(
            username="user1",
            email="user1@example.com",
            password="userpassword123",
            is_active=False
        )
        self.user2 = Users.objects.create_user(
            username="user2",
            email="user2@example.com",
            password="userpassword123"
        )
        self.admin_token = self.client.post(reverse('LoginView'), {
            "email": self.admin.email,
            "password": "adminpassword123"
        }).data['jwt']
        self.url = reverse('ListBlockedUsersView')

    def test_list_blocked_users_success(self):
        """
        Verifica que se puede obtener una lista de usuarios bloqueados.
        """
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.admin_token}')
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("blocked_users", response.data)
        self.assertIn(self.user1.email, response.data["blocked_users"])
        self.assertNotIn(self.user2.email, response.data["blocked_users"])

    def test_list_blocked_users_unauthenticated(self):
        """
        Verifica que un usuario no autenticado no puede acceder al endpoint.
        """
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)
