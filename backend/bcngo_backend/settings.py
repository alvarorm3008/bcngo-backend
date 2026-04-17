"""
Configuración de settings para el proyecto BCNGO.

Incluye configuraciones de base de datos, aplicaciones instaladas,
middleware, autenticación y otros parámetros importantes.
"""

import os
from pathlib import Path
import firebase_admin
from firebase_admin import credentials
from decouple import config

BASE_DIR = Path(__file__).resolve().parent.parent

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

ALLOWED_HOSTS = ['*']  # Cambiar según tus necesidades de producción

# PostgreSQL Database Configuration (Asegúrate de configurar tu usuario y contraseña en Lightsail)
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': 'postgres',  # El nombre de la base de datos
        'USER': 'dbmasteruser',  # El nombre del usuario
        'PASSWORD': '!4xAaDg=b8qCpR?OzoH(~%sd,aa,K9xx',  # La contraseña del usuario
        'HOST': 'ls-686562ce17dc0fa4b71068cf3183ca0d431d88ad.cpckiocu6f1j.eu-west-3.rds.amazonaws.com',
        'PORT': '5432',  # El puerto por defecto de PostgreSQL
    }
}

# Configuración de autenticación y registro con django-allauth
INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'users',
    'puntosdeinteres',
    'rest_framework',
    'corsheaders',
    'itinerario',
    'drf_yasg',
    'rest_framework_simplejwt',
    'pasaporte.apps.PasaporteConfig',
    'eventos',
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
    'corsheaders.middleware.CorsMiddleware',
]

ROOT_URLCONF = 'bcngo_backend.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [BASE_DIR / 'bcngo_backend/templates'],  # Directorios de plantillas
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'bcngo_backend.wsgi.application'

# Password validation
AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]

# Algoritmos de encriptación de contraseñas
PASSWORD_HASHERS = [
    'django.contrib.auth.hashers.Argon2PasswordHasher',
    'django.contrib.auth.hashers.PBKDF2PasswordHasher',
    'django.contrib.auth.hashers.PBKDF2SHA1PasswordHasher',
    'django.contrib.auth.hashers.BCryptSHA256PasswordHasher',
]

REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'users.authentication.JWTAuthentication',  # Define esta clase a continuación
    ],
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.AllowAny',
    ],
}

LANGUAGE_CODE = 'en-us'
USE_TZ = True
TIME_ZONE = 'Europe/Madrid'

USE_I18N = True

# Static files (CSS, JavaScript, Images)
STATIC_URL = 'static/'

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

CORS_ALLOW_ALL_ORIGINS = True  # Para pruebas

SITE_ID = 1

AUTHENTICATION_BACKENDS = [
    'django.contrib.auth.backends.ModelBackend',
]

AUTH_USER_MODEL = 'users.Users'

SECRET_KEY = '1=f@#dl_auo0-l+c%$wu*kep^llm-e8^+p#@*kt*@a35i6yn4+'

SESSION_ENGINE = 'django.contrib.sessions.backends.db'

# Inicializar Firebase Admin si no se ha inicializado previamente
FIREBASE_CREDENTIALS_PATH = os.getenv('FIREBASE_KEY_PATH', './config/firebase-key.json')

if len(firebase_admin._apps) == 0:  # pylint: disable=protected-access
    cred = credentials.Certificate(FIREBASE_CREDENTIALS_PATH)
    firebase_admin.initialize_app(cred)

# Mail para recuperar password
EMAIL_BACKEND = 'django.core.mail.backends.smtp.EmailBackend'
EMAIL_HOST = 'smtp.gmail.com'
EMAIL_PORT = 587
EMAIL_USE_TLS = True
EMAIL_HOST_USER = config('EMAIL_HOST_USER')
EMAIL_HOST_PASSWORD = config('EMAIL_HOST_PASSWORD')
PASSWORD_RESET_TIMEOUT = 600
