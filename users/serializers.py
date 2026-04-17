"""
Este módulo contiene el serializador para el modelo 'Users'.
"""

from rest_framework import serializers
from .models import Users

class UserSerializer(serializers.ModelSerializer):
    """
    Serializador para el modelo 'Users'.
    """
    class Meta: # pylint: disable=too-few-public-methods
        """
        Meta información para el serializador de usuarios.
        """
        model = Users
        fields = ['id', 'email', 'password', 'username']
        extra_kwargs = {'password': {'write_only': True}}

    def create(self, validated_data):
        """
        Sobrescribe el método 'create' para manejar el hashing de contraseñas.
        """
        password = validated_data.pop('password', None)
        instance = self.Meta.model(**validated_data)
        if password is not None:
            instance.set_password(password)
        instance.save()
        return instance

class UserViewSerializer(serializers.ModelSerializer):
    """
    Serializador para el modelo 'Users'.
    """
    class Meta: # pylint: disable=too-few-public-methods
        """
        Meta información para el serializador de usuarios.
        """
        model = Users
        fields = ['id', 'email', 'username', 'notifications']


class EditProfileSerializer(serializers.ModelSerializer):
    """
    Serializador para editar el perfil del usuario.
    """
    class Meta:  # pylint: disable=too-few-public-methods
        """
        Meta información para el serializador de edición de perfil.
        """
        model = Users
        fields = ['username']  # Solo se permite editar el campo 'username' de momento
