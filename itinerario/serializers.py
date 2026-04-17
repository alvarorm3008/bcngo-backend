"""Serializers for the itinerario app."""

from rest_framework import serializers
from puntosdeinteres.models import PointOfInterest
from itinerario.services import select_points_for_itinerary
from .models import Itinerario, ItineraryItem



class ItineraryItemSerializer(serializers.ModelSerializer):
    """Serializer for ItineraryItem model."""
    points = serializers.StringRelatedField(
        many=True,
        required=False,
    )

    class Meta:
        """Meta class for ItineraryItemSerializer."""
        model = ItineraryItem
        fields = ['id', 'day', 'points']  # Aquí los puntos devolverán los nombres


class ItinerarioSerializer(serializers.ModelSerializer):
    """Serializer for Itinerario model."""
    items = ItineraryItemSerializer(many=True, read_only=True)
    manual = serializers.PrimaryKeyRelatedField(
        many=True, queryset=PointOfInterest.objects.all(), required=False
    )

    categories = serializers.ListField(
        child=serializers.CharField(), write_only=True, required=False
    )  # Campo adicional para categorías

    rarity = serializers.ChoiceField(
        choices=["Atypical", "Typical", "Very typical"], write_only=True, required=False
    )  # Campo adicional para rareza


    class Meta:
        """Meta class for ItinerarioSerializer."""
        model = Itinerario
        fields = [
            'id',
            'name',
            'user',
            'start_date',
            'end_date',
            'created_at',
            'updated_at',
            'favourite',
            'manual',  # Points manually added by users
            'items',  # Nested itinerary items
            'days_of_stay',
            'categories',  # Categorías para la creación automática
            'rarity'    # Rareza para la creación automática
        ]
        read_only_fields = ['created_at', 'updated_at', 'days_of_stay', 'user']


    def create(self, validated_data):
        """ Override the create method to handle automatic itinerary creation."""
        manual_data = validated_data.pop('manual', [])
        categories = validated_data.pop('categories', None)
        # Obtener rareza o usar "atipico" por defecto
        rarity = validated_data.pop('rarity', "Atypical")
        user = self.context['request'].user

        itinerario = Itinerario.objects.create(user=user, **validated_data)

        # Calcular los días de estancia
        days_of_stay = itinerario.days_of_stay
        if days_of_stay <= 0:
            raise serializers.ValidationError("La fecha de inicio y fin debe generar al menos un día de estancia.")

        if categories:
            try:
                # Lógica de selección delegada al servicio
                selected_points = select_points_for_itinerary(categories, days_of_stay, rarity)
                # Asignar los puntos seleccionados automáticamente
                itinerario.manual.set(selected_points)
            except ValueError as e:
                raise serializers.ValidationError(str(e))

        elif manual_data:
            itinerario.manual.set(manual_data)

        itinerario.generate_itinerary_points()
        return itinerario


    def update(self, instance, validated_data):
        """
        Override the update method to handle manual points updates.
        """
        manual_data = validated_data.pop('manual', None)  # Pop manual points if provided

        for attr, value in validated_data.items():
            setattr(instance, attr, value)

        if manual_data is not None:
            instance.manual.set(manual_data)

        instance.save()
        return instance


class ItinerarioListSerializer(serializers.ModelSerializer):
    """Serializer for listing Itinerario objects."""
    items = ItineraryItemSerializer(many=True, read_only=True)
    manual = serializers.PrimaryKeyRelatedField(
        many=True, queryset=PointOfInterest.objects.all(), required=False
    )

    # Cambiar la representación de user para devolver solo el ID
    user = serializers.IntegerField(source='user.id', read_only=True)
    created_at = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S.%fZ')
    updated_at = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S.%fZ')

    class Meta:
        """Meta class for ItinerarioListSerializer."""
        model = Itinerario
        fields = [
            'id',
            'name',
            'user',  # Ahora devuelve solo el ID del usuario
            'start_date',
            'end_date',
            'created_at',
            'updated_at',
            'favourite',
            'manual',
            'items',
            'days_of_stay',
        ]
        read_only_fields = ['created_at', 'updated_at', 'days_of_stay']
