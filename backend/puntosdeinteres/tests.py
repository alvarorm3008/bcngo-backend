# pylint: disable=no-member
"""
Este módulo contiene pruebas unitarias para las vistas de la aplicación de Puntos de Interés.
"""

from unittest.mock import patch  # pylint: disable=unused-import
from django.test import TestCase
from django.urls import reverse

from rest_framework.test import APIClient
from rest_framework import status

from users.models import Users
from puntosdeinteres.models import PointOfInterest, Review



class PointOfInterestDetailApiTestCase(TestCase):
    """
    Pruebas para la API de detalles de un punto de interés.
    """

    def setUp(self):
        """
        Configuración inicial para las pruebas de detalles de puntos de interés.
        """
        self.park = PointOfInterest.objects.create(  # pylint: disable=no-member
            name="Parque Güell",
            address_name="Carrer d'Olot",
            latitude=41.4145,
            longitude=2.1527,
            category="Parques"
        )
        self.architecture = PointOfInterest.objects.create(  # pylint: disable=no-member
            name="Casa Batlló",
            address_name="Pg. de Gràcia",
            latitude=41.3917,
            longitude=2.1649,
            category="Arquitectura"
        )

    def test_point_of_interest_detail_api_returns_correct_point(self):
        """
        Verifica que la API devuelve el punto de interés correcto para un ID válido.
        """
        # Llamada a la API de detalles para "Parque Güell"
        url = reverse('pointsofinterest-detail', args=[self.park.id])
        response = self.client.get(url)

        # Verificar que el status code es 200
        self.assertEqual(response.status_code, 200)
        # Verificar que los detalles devueltos son los esperados
        data = response.json()
        self.assertEqual(data['name'], "Parque Güell")
        self.assertEqual(data['address_name'], "Carrer d'Olot")
        self.assertAlmostEqual(data['latitude'], 41.4145)
        self.assertAlmostEqual(data['longitude'], 2.1527)
        self.assertEqual(data['category'], "Parques")

    def test_point_of_interest_detail_api_returns_404_for_nonexistent_id(self):
        """
        Verifica que la API devuelve un error 404 para un ID inexistente.
        """
        # Llamada a la API de detalles con un ID inexistente
        url = reverse('pointsofinterest-detail', args=[9999])  # ID que no existe
        response = self.client.get(url)
        # Verificar que el status code es 404
        self.assertEqual(response.status_code, 404)
        # Verificar el mensaje de error
        data = response.json()
        self.assertIn("error", data)
        self.assertEqual(data["error"], "Punto de interés no encontrado.")


class ParksApiTestCase(TestCase):
    """
    Pruebas para la API de puntos de interés de la categoría 'Parques'.
    """

    def setUp(self):
        """
        Configuración inicial para las pruebas de la API de 'Parques'.
        """
        self.park1 = PointOfInterest.objects.create(  # pylint: disable=no-member
            name="Parque Güell",
            address_name="Carrer d'Olot",
            latitude=41.4145,
            longitude=2.1527,
            category="Parques"
        )
        self.park2 = PointOfInterest.objects.create(  # pylint: disable=no-member
            name="Ciutadella Park",
            address_name="Passeig de Picasso",
            latitude=41.389,
            longitude=2.181,
            category="Parques"
        )
        self.architecture = PointOfInterest.objects.create(  # pylint: disable=no-member
            name="Casa Batlló",
            address_name="Pg. de Gràcia",
            latitude=41.3917,
            longitude=2.1649,
            category="Arquitectura"
        )

    def test_parks_api_returns_correct_points(self):
        """
        Verifica que la API devuelve únicamente los puntos de interés de categoría 'Parques'.
        """
        url = reverse('parks-list')  # El nombre de la ruta en urls.py
        response = self.client.get(url)

        # Verificar que el status code es 200
        self.assertEqual(response.status_code, 200)

        # Verificar que solo se devuelven los puntos de categoría "Parques"
        data = response.json()
        self.assertEqual(len(data), 2)

        # Verificar que los puntos de interés devueltos son los esperados
        park_names = [park['name'] for park in data]
        self.assertIn("Parque Güell", park_names)
        self.assertIn("Ciutadella Park", park_names)
        self.assertNotIn("Casa Batlló", park_names)

        # Verificar los detalles de un punto de interés específico
        park_guell = next((p for p in data if p['name'] == "Parque Güell"), None)
        self.assertIsNotNone(park_guell)
        self.assertEqual(park_guell['address_name'], "Carrer d'Olot")
        self.assertAlmostEqual(park_guell['latitude'], 41.4145)
        self.assertAlmostEqual(park_guell['longitude'], 2.1527)
        self.assertEqual(park_guell['category'], "Parques")

        # Verificar que todos los puntos devueltos tienen las claves necesarias
        required_keys = {"name", "address_name", "latitude", "longitude", "category"}
        for point in data:
            self.assertTrue(required_keys.issubset(point.keys()))

class CreatePointOfInterestTestCase(TestCase):
    """
    Pruebas completas para la creación de puntos de interés.
    """

    def setUp(self):
        """
        Configuración inicial para las pruebas.
        """
        # Crear un administrador y un usuario regular
        self.admin_user = Users.objects.create_user(
            username="adminuser",
            email="admin@example.com",
            password="adminpassword123",
            is_staff=True
        )
        self.regular_user = Users.objects.create_user(
            username="regularuser",
            email="regular@example.com",
            password="regularpassword123",
            is_staff=False
        )
        self.create_url = reverse("pointsofinterest-create")

        # Crear un cliente API
        self.client = APIClient()

    def authenticate_as_admin(self):
        """
        Autentica el cliente como administrador.
        """
        response = self.client.post(
            reverse("LoginView"),
            {"email": "admin@example.com", "password": "adminpassword123"},
            format="json"
        )
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {response.data['jwt']}")

    def authenticate_as_regular_user(self):
        """
        Autentica el cliente como un usuario regular.
        """
        response = self.client.post(
            reverse("LoginView"),
            {"email": "regular@example.com", "password": "regularpassword123"},
            format="json"
        )
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {response.data['jwt']}")

    def test_admin_can_create_point_of_interest(self):
        """
        Verifica que un administrador autenticado puede crear un punto de interés.
        """
        self.authenticate_as_admin()
        payload = {
            "name": "Punto de Prueba",
            "address_name": "Calle Falsa",
            "street_number": "123",
            "latitude": 40.416775,
            "longitude": -3.703790,
            "phone": "+34912345678",
            "web_url": "https://puntodeprueba.example.com",
            "category": "Parques",
            "esencial": False
        }
        response = self.client.post(self.create_url, payload, format="json")
        self.assertEqual(response.status_code, 201)
        self.assertEqual(PointOfInterest.objects.count(), 1)
        self.assertEqual(PointOfInterest.objects.first().name, "Punto de Prueba")

    def test_regular_user_cannot_create_point_of_interest(self):
        """
        Verifica que un usuario no administrador no puede crear un punto de interés.
        """
        self.authenticate_as_regular_user()
        payload = {
            "name": "Punto No Permitido",
            "address_name": "Calle Prohibida",
            "latitude": 40.123456,
            "longitude": -3.654321,
            "category": "Ocio",
        }
        response = self.client.post(self.create_url, payload, format="json")
        self.assertEqual(response.status_code, 403)  # Forbidden
        self.assertEqual(PointOfInterest.objects.count(), 0)

    def test_missing_required_fields(self):
        """
        Verifica que el endpoint devuelve un error si faltan campos obligatorios.
        """
        self.authenticate_as_admin()
        payload = {
            "address_name": "Calle Sin Nombre",
            "latitude": 40.416775,
            "longitude": -3.703790,
            "category": "Parques",
        }
        response = self.client.post(self.create_url, payload, format="json")
        self.assertEqual(response.status_code, 400)  # Bad Request
        self.assertIn("name", response.data)  # El campo 'name' es obligatorio

    def test_duplicate_point_of_interest(self):
        """
        Verifica que no se pueden crear puntos de interés duplicados.
        """
        self.authenticate_as_admin()
        PointOfInterest.objects.create(  # pylint: disable=no-member
            name="Punto Duplicado",
            address_name="Calle Única",
            latitude=40.416775,
            longitude=-3.703790,
            category="Parques"
        )
        payload = {
            "name": "Punto Duplicado",  # Mismo nombre
            "address_name": "Calle Única",  # Misma dirección
            "latitude": 40.416775,
            "longitude": -3.703790,
            "category": "Parques",
        }
        response = self.client.post(self.create_url, payload, format="json")
        self.assertEqual(response.status_code, 400)  # Bad Request
        self.assertIn("non_field_errors", response.data)  # Comprobar que el error está en 'non_field_errors'

        # Verifica el mensaje exacto del error
        self.assertEqual(
            str(response.data["non_field_errors"][0]),  # Convertir a string para compararlo
            "Ya existe un punto de interés con el mismo nombre y dirección."
        )




class DeletePointOfInterestTestCase(TestCase):
    """
    Pruebas para la funcionalidad de eliminar un punto de interés.
    """

    def setUp(self):
        """
        Configuración inicial para las pruebas de eliminación.
        """
        self.admin_user = Users.objects.create_superuser(  # Crear un administrador
            username="admin",
            email="admin@example.com",
            password="adminpassword"
        )
        self.regular_user = Users.objects.create_user(  # Crear un usuario normal
            username="user",
            email="user@example.com",
            password="userpassword"
        )
        self.client = APIClient()

        # Crear un punto de interés de prueba
        self.point_of_interest = PointOfInterest.objects.create(
            name="Punto de Prueba",
            address_name="Calle de Prueba",
            latitude=40.416775,
            longitude=-3.703790,
            category="Parques"
        )

        self.delete_url = reverse(
            "pointsofinterest-delete", args=[self.point_of_interest.id]
        )  # URL para eliminar el punto de interés

    def test_admin_can_delete_point_of_interest(self):
        """
        Verifica que un administrador autenticado puede eliminar un punto de interés.
        """
        self.client.force_authenticate(user=self.admin_user)  # Autenticar como administrador
        response = self.client.delete(self.delete_url)
        self.assertEqual(response.status_code, 204)  # 204 No Content
        self.assertFalse(
            PointOfInterest.objects.filter(id=self.point_of_interest.id).exists()
        )  # Verificar que ya no existe

    def test_user_cannot_delete_point_of_interest(self):
        """
        Verifica que un usuario regular no puede eliminar un punto de interés.
        """
        self.client.force_authenticate(user=self.regular_user)  # Autenticar como usuario normal
        response = self.client.delete(self.delete_url)
        self.assertEqual(response.status_code, 403)  # 403 Forbidden
        self.assertTrue(
            PointOfInterest.objects.filter(id=self.point_of_interest.id).exists()
        )  # Verificar que el punto de interés sigue existiendo

    def test_cannot_delete_nonexistent_point_of_interest(self):
        """
        Verifica que intentar eliminar un punto de interés inexistente devuelve un error 404.
        """
        self.client.force_authenticate(user=self.admin_user)  # Autenticar como administrador
        nonexistent_url = reverse("pointsofinterest-delete", args=[9999])  # ID inexistente
        response = self.client.delete(nonexistent_url)
        self.assertEqual(response.status_code, 404)  # 404 Not Found
        self.assertIn("error", response.data)  # Verificar que hay un mensaje de error

    def test_unauthenticated_user_cannot_delete_point_of_interest(self):
        """
        Verifica que un usuario no autenticado no puede eliminar un punto de interés.
        """
        self.client.force_authenticate(user=None)  # No autenticar al usuario
        response = self.client.delete(self.delete_url)
        self.assertEqual(response.status_code, 401)  # 401 Unauthorized
        self.assertTrue(
            PointOfInterest.objects.filter(id=self.point_of_interest.id).exists()
        )  # Verificar que el punto de interés sigue existiendo


class ReviewApiTestCase(TestCase):
    """
    Pruebas para los endpoints relacionados con reseñas.
    """

    def setUp(self):
        """
        Configuración inicial para las pruebas.
        """
        # Crear usuarios
        self.admin_user = Users.objects.create_user(
            username="admin",
            email="admin@example.com",
            password="adminpassword",
            is_staff=True
        )
        self.regular_user = Users.objects.create_user(
            username="regularuser",
            email="regular@example.com",
            password="regularpassword"
        )

        # Crear un punto de interés
        self.point_of_interest = PointOfInterest.objects.create(
            name="Punto de Interés Prueba",
            address_name="Calle Falsa 123",
            latitude=40.416775,
            longitude=-3.703790,
            category="Parques"
        )

        # Crear un cliente API
        self.client = APIClient()

    def authenticate_as_regular_user(self):
        """
        Autentica el cliente como usuario regular.
        """
        response = self.client.post(
            reverse("LoginView"),
            {"email": "regular@example.com", "password": "regularpassword"},
            format="json"
        )
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {response.data['jwt']}")

    def authenticate_as_admin(self):
        """
        Autentica el cliente como administrador.
        """
        response = self.client.post(
            reverse("LoginView"),
            {"email": "admin@example.com", "password": "adminpassword"},
            format="json"
        )
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {response.data['jwt']}")

    def test_create_review(self):
        """
        Verifica que un usuario autenticado puede crear una reseña.
        """
        self.authenticate_as_regular_user()
        payload = {
            "point_of_interest": self.point_of_interest.id,
            "username": "regularuser",
            "user_email": "regular@example.com",
            "comment": "Excelente lugar para visitar.",
            "rating": 5
        }
        response = self.client.post(reverse("review-create"), payload, format="json")
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        review = Review.objects.first()
        self.assertEqual(review.username, "regularuser")
        self.assertEqual(review.comment, "Excelente lugar para visitar.")
        self.assertEqual(review.rating, 5)

    def test_delete_own_review(self):
        """
        Verifica que un usuario puede eliminar su propia reseña.
        """
        self.authenticate_as_regular_user()
        review = Review.objects.create(
            point_of_interest=self.point_of_interest,
            username="regularuser",
            user_email="regular@example.com",
            comment="Reseña propia",
            rating=4
        )
        response = self.client.delete(reverse("review-delete", args=[review.id]))
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Review.objects.filter(id=review.id).exists())

    def test_cannot_delete_other_user_review(self):
        """
        Verifica que un usuario no puede eliminar la reseña de otro usuario.
        """
        # Crear la reseña con un usuario diferente al autenticado
        review = Review.objects.create(
            point_of_interest=self.point_of_interest,
            username="admin",
            user_email="admin@example.com",
            comment="Reseña de otro usuario",
            rating=3
        )

        self.authenticate_as_regular_user()
        response = self.client.delete(reverse("review-delete", args=[review.id]))
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_admin_can_delete_any_review(self):
        """
        Verifica que un administrador puede eliminar cualquier reseña.
        """
        self.authenticate_as_admin()
        review = Review.objects.create(
            point_of_interest=self.point_of_interest,
            username="regularuser",
            user_email="regular@example.com",
            comment="Reseña de usuario regular",
            rating=4
        )
        response = self.client.delete(reverse("review-delete", args=[review.id]))
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Review.objects.filter(id=review.id).exists())

    def test_list_reviews_by_point(self):
        """
        Verifica que se pueden listar todas las reseñas de un punto de interés.
        """
        Review.objects.create(
            point_of_interest=self.point_of_interest,
            username="regularuser",
            user_email="regular@example.com",
            comment="Primera reseña",
            rating=4
        )
        Review.objects.create(
            point_of_interest=self.point_of_interest,
            username="admin",
            user_email="admin@example.com",
            comment="Segunda reseña",
            rating=5
        )
        response = self.client.get(reverse("reviews-list-by-point", args=[self.point_of_interest.id]))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        data = response.json()
        self.assertEqual(len(data), 2)
        self.assertEqual(data[0]["username"], "regularuser")
        self.assertEqual(data[1]["username"], "admin")

    def test_report_review(self):
        """
        Verifica que un usuario puede reportar una reseña.
        """
        review = Review.objects.create(
            point_of_interest=self.point_of_interest,
            username="admin",
            user_email="admin@example.com",
            comment="Reseña para reportar",
            rating=2
        )
        self.authenticate_as_regular_user()
        response = self.client.post(reverse("review-report", args=[review.id]))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        review.refresh_from_db()
        self.assertEqual(review.reports_count, 1)

    def test_list_reported_reviews_as_admin(self):
        """
        Verifica que un administrador puede listar todas las reseñas reportadas.
        """
        Review.objects.create(
            point_of_interest=self.point_of_interest,
            username="regularuser",
            user_email="regular@example.com",
            comment="Reseña reportada",
            rating=1,
            reports_count=2
        )
        self.authenticate_as_admin()
        response = self.client.get(reverse("reviews-reported-list"))
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        data = response.json()
        self.assertEqual(len(data), 1)
        self.assertEqual(data[0]["comment"], "Reseña reportada")
