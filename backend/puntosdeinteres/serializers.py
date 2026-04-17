"""
Serializadores para los modelos de Puntos de Interés y Reseñas.

Define cómo los datos son serializados y validados.
"""
from rest_framework import serializers
from .models import PointOfInterest, Review


class PointOfInterestSerializer(serializers.ModelSerializer):
    """
    Serializador para el modelo PointOfInterest.
    Incluye validaciones personalizadas para prevenir duplicados.
    """

    class Meta:  # pylint: disable=too-few-public-methods
        """
        Meta clase que define el modelo y los campos a serializar.
        """
        model = PointOfInterest
        ref_name = "PointOfInterestModelSerializer"  # Nombre único para evitar conflictos
        fields = [
            "id", 
            "name", 
            "address_name", 
            "street_number", 
            "latitude", 
            "longitude", 
            "phone", 
            "web_url", 
            "category", 
            "esencial"
        ]

    def validate(self, attrs):  # pylint: disable=arguments-renamed
        """
        Validación personalizada para prevenir duplicados.
        """
        name = attrs.get("name")
        address_name = attrs.get("address_name")

        # Verificar si ya existe un punto de interés con el mismo nombre y dirección
        if PointOfInterest.objects.filter(name=name, address_name=address_name).exists():  # pylint: disable=no-member
            raise serializers.ValidationError(
                "Ya existe un punto de interés con el mismo nombre y dirección."
            )

        return attrs


class ReviewSerializer(serializers.ModelSerializer):
    """
    Serializador para el modelo Review.
    """

    created_at = serializers.SerializerMethodField()
    updated_at = serializers.SerializerMethodField()

    class Meta:  # pylint: disable=too-few-public-methods
        """
        Meta clase que define el modelo y los campos a serializar.
        """
        model = Review
        ref_name = "ReviewModelSerializer"  # Nombre único para evitar conflictos
        fields = [
            "id",
            "point_of_interest",
            "username",
            "user_email",
            "comment",
            "rating",
            "reports_count",
            "created_at",
            "updated_at",
        ]
        read_only_fields = ["reports_count", "created_at", "updated_at"]

    def get_created_at(self, obj):
        """
        Devuelve la fecha de creación en el formato deseado.
        """
        return obj.created_at.strftime("%Y-%m-%dT%H:%M:%S")

    def get_updated_at(self, obj):
        """
        Devuelve la fecha de actualización en el formato deseado.
        """
        return obj.updated_at.strftime("%Y-%m-%dT%H:%M:%S")

    def validate_rating(self, value):
        """
        Valida que la calificación esté dentro del rango permitido.
        """
        if not 1 <= value <= 5:
            raise serializers.ValidationError("La calificación debe estar entre 1 y 5.")
        return value
