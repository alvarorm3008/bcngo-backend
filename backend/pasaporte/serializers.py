"""
Serializadores para la aplicación Pasaporte Digital.
"""

from rest_framework import serializers
from puntosdeinteres.models import PointOfInterest
from .models import PassportPoint, Passport


class PointOfInterestSerializer(serializers.ModelSerializer):
    """
    Serializador básico para puntos de interés.
    """
    class Meta:
        """
        Metadatos del serializer PointOfInterestSerializer.
        """
        model = PointOfInterest
        ref_name = "PassportPointOfInterest"  # Nombre único para evitar conflictos en Swagger
        fields = ['id', 'name', 'category', 'latitude', 'longitude']


class PassportPointSerializer(serializers.ModelSerializer):
    """
    Serializador para puntos en el pasaporte.
    """
    point_of_interest = PointOfInterestSerializer()

    class Meta:
        """
        Metadatos del serializer PassportPointSerializer.
        """
        model = PassportPoint
        fields = ['point_of_interest', 'is_marked']


class PassportSerializer(serializers.ModelSerializer):
    """
    Serializador para el modelo de pasaporte.
    """
    passport_points = PassportPointSerializer(many=True)

    class Meta:
        """
        Metadatos del serializer PassportSerializer.
        """
        model = Passport
        fields = ['user', 'passport_points']
