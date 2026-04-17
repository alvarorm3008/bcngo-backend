"""
Pruebas unitarias para la aplicación de Pasaporte.
"""

from django.test import TestCase
from django.contrib.auth import get_user_model
from puntosdeinteres.models import PointOfInterest
from pasaporte.models import Passport, PassportPoint

User = get_user_model()

class PassportTestCase(TestCase):
    """
    Test para validar la funcionalidad del modelo Passport.
    """

    @classmethod
    def setUpTestData(cls):
        """
        Configuración inicial compartida para todas las pruebas.
        """
        # Crear puntos de interés esenciales
        cls.essential_point1 = PointOfInterest.objects.create(
            name="Punto Esencial 1",
            category="Arte",
            latitude=41.3851,
            longitude=2.1734,
            esencial=True
        )
        cls.essential_point2 = PointOfInterest.objects.create(
            name="Punto Esencial 2",
            category="Arquitectura",
            latitude=41.3861,
            longitude=2.1744,
            esencial=True
        )

    def setUp(self):
        """
        Configuración antes de cada prueba.
        """
        self.user = User.objects.create_user(
            username="testuser",
            email="testuser@example.com",
            password="testpassword123"
        )
        self.passport = Passport.objects.get(user=self.user)

    def test_passport_creation_on_user_creation(self):
        """
        Verifica que se crea un pasaporte automáticamente al crear un usuario.
        """
        self.assertIsNotNone(self.passport)
        self.assertEqual(self.passport.user, self.user)
        self.assertEqual(self.passport.points_of_interest.count(), 2)

    def test_passport_points_are_added_as_not_marked(self):
        """
        Verifica que los puntos esenciales se añaden al pasaporte y están sin marcar inicialmente.
        """
        passport_points = PassportPoint.objects.filter(passport=self.passport)
        self.assertEqual(passport_points.count(), 2)
        for passport_point in passport_points:
            self.assertFalse(passport_point.is_marked)

    def test_marking_a_point(self):
        """
        Verifica que se puede marcar un punto de interés en el pasaporte.
        """
        passport_point = PassportPoint.objects.get(passport=self.passport, point_of_interest=self.essential_point1)
        passport_point.is_marked = True
        passport_point.save()
        passport_point.refresh_from_db()
        self.assertTrue(passport_point.is_marked)

    def test_passport_str_representation(self):
        """
        Verifica la representación en string del modelo Passport.
        """
        self.assertEqual(str(self.passport), f"Pasaporte de {self.user.username}")

    def test_passport_point_str_representation(self):
        """
        Verifica la representación en string del modelo PassportPoint.
        """
        passport_point = PassportPoint.objects.get(passport=self.passport, point_of_interest=self.essential_point1)
        self.assertEqual(str(passport_point), "Punto Esencial 1 (no marcado)")

    def test_marking_nonexistent_point(self):
        """
        Verifica que no se puede marcar un punto que no pertenece al pasaporte.
        """
        non_existent_point = PointOfInterest.objects.create(
            name="Punto No Existente",
            category="Ocio",
            latitude=41.3879,
            longitude=2.1699,
            esencial=False
        )
        with self.assertRaises(PassportPoint.DoesNotExist):
            PassportPoint.objects.get(passport=self.passport, point_of_interest=non_existent_point)

    def test_no_duplicate_passport_points(self):
        """
        Verifica que no se puedan duplicar puntos esenciales en un pasaporte.
        """
        PassportPoint.objects.get_or_create(passport=self.passport, point_of_interest=self.essential_point1)
        passport_points = PassportPoint.objects.filter(passport=self.passport, point_of_interest=self.essential_point1)
        self.assertEqual(passport_points.count(), 1)

    def test_multiple_users_have_independent_passports(self):
        """
        Verifica que múltiples usuarios tengan pasaportes independientes.
        """
        user2 = User.objects.create_user(
            username="testuser2",
            email="testuser2@example.com",
            password="testpassword123"
        )
        passport_user2 = Passport.objects.get(user=user2)

        self.assertNotEqual(self.passport, passport_user2)
        self.assertEqual(self.passport.points_of_interest.count(), 2)
        self.assertEqual(passport_user2.points_of_interest.count(), 2)

    def test_passport_without_essential_points(self):
        """
        Verifica que el pasaporte se crea correctamente cuando no hay puntos esenciales.
        """
        PointOfInterest.objects.all().delete()
        user_no_points = User.objects.create_user(
            username="usernopoints",
            email="usernopoints@example.com",
            password="testpassword123"
        )
        passport = Passport.objects.get(user=user_no_points)
        self.assertEqual(passport.points_of_interest.count(), 0)
