# BCNGo — Backend API

API REST del proyecto **BCNGo**: aplicación orientada a cultura y ocio en Barcelona (eventos, puntos de interés, itinerarios, pasaporte cultural y usuarios).

Stack principal: **Django 4.2**, **Django REST Framework**, **PostgreSQL**, autenticación **JWT**, notificaciones con **Firebase** y documentación **OpenAPI (Swagger / ReDoc)**.

---

## Contenido

- [Requisitos](#requisitos)
- [Puesta en marcha con Docker](#puesta-en-marcha-con-docker)
- [Puesta en marcha en local (sin Docker)](#puesta-en-marcha-en-local-sin-docker)
- [Variables de entorno](#variables-de-entorno)
- [Documentación de la API](#documentación-de-la-api)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Comandos de gestión útiles](#comandos-de-gestión-útiles)
- [Tests y calidad](#tests-y-calidad)
- [CI/CD](#cicd)

---

## Requisitos

| Entorno | Versión recomendada |
|--------|---------------------|
| Python | 3.9+ (alineado con el `Dockerfile`) |
| PostgreSQL | Compatible con `psycopg2-binary` del `requirements.txt` |
| Docker / Docker Compose | Para el flujo containerizado descrito abajo |

---

## Puesta en marcha con Docker

1. Clona el repositorio y entra en la carpeta del proyecto.

2. Asegúrate de tener el fichero de credenciales de Firebase en la ruta esperada (por defecto `./config/firebase-key.json`, o la que indiques con `FIREBASE_KEY_PATH`).

3. Levanta los servicios:

   ```bash
   docker compose up --build
   ```

   La aplicación queda expuesta en **http://localhost:8000** (puerto mapeado en `docker-compose.yml`).

4. En otra terminal, aplica migraciones (primera vez o tras cambios en modelos):

   ```bash
   docker exec bcngo-container python manage.py migrate
   ```

5. Para detener:

   ```bash
   docker compose down
   ```

> **Nota:** En la imagen se configura **cron** para ejecutar diariamente la sincronización de eventos (`sync_events`). Revisa `config/mycron` si necesitas otro horario o comandos.

---

## Puesta en marcha en local (sin Docker)

1. Crea y activa un entorno virtual:

   ```bash
   python3 -m venv venv
   source venv/bin/activate   # Linux / macOS
   # .\venv\Scripts\activate  # Windows
   ```

2. Instala dependencias:

   ```bash
   pip install -r requirements.txt
   ```

3. Configura PostgreSQL y ajusta `DATABASES` en `bcngo_backend/settings.py` (o migra la configuración a variables de entorno en tu despliegue). El usuario, contraseña y base deben coincidir con tu instancia local.

4. Exporta las variables necesarias (ver siguiente sección), incluido el path al JSON de Firebase.

5. Aplica migraciones y arranca el servidor de desarrollo:

   ```bash
   python manage.py migrate
   python manage.py runserver
   ```

---

## Variables de entorno

| Variable | Descripción |
|----------|-------------|
| `FIREBASE_KEY_PATH` | Ruta al JSON de credenciales de Firebase Admin. Por defecto: `./config/firebase-key.json`. |
| `EMAIL_HOST_USER` | Usuario SMTP (p. ej. recuperación de contraseña). |
| `EMAIL_HOST_PASSWORD` | Contraseña o app password del buzón. |

Estas claves se leen con `python-decouple` donde aplica; puedes usar un fichero `.env` en la raíz del proyecto (no lo subas al repositorio si contiene secretos).

**Buenas prácticas:** no versiones claves reales de base de datos, `SECRET_KEY` ni el JSON de Firebase. Añade `firebase-key.json` y `.env` a `.gitignore` en entornos reales si aún no están ignorados.

---

## Documentación de la API

Con el servidor en marcha:

| Recurso | URL |
|--------|-----|
| **Swagger UI** | http://localhost:8000/swagger/ |
| **ReDoc** | http://localhost:8000/redoc/ |
| **Admin Django** | http://localhost:8000/admin/ |

Prefijos principales de la API (ver `bcngo_backend/urls.py`):

| Prefijo | Módulo |
|---------|--------|
| `/puntosdeinteres/` | Puntos de interés y reseñas |
| `/itinerario/` | Itinerarios |
| `/users/` | Usuarios y autenticación |
| `/pasaporte/` | Pasaporte cultural |
| `/eventos/` | Eventos y funcionalidades asociadas |
| `/` (vía `servicio`) | Integración con el servicio YAML externo |

---

## Estructura del proyecto

| Ruta | Rol |
|------|-----|
| `bcngo_backend/` | Settings, URLs raíz, plantillas (p. ej. reset de contraseña) |
| `config/` | Cron y recursos de configuración (p. ej. Firebase) |
| `users/` | Modelo de usuario personalizado, JWT, vistas de auth |
| `puntosdeinteres/` | POIs, sincronización con APIs externas |
| `itinerario/` | Itinerarios |
| `pasaporte/` | Pasaporte cultural |
| `eventos/` | Eventos, chats de grupo, favoritos, sincronización |
| `servicio/` | Endpoints del servicio recibido / contrato YAML |

---

## Comandos de gestión útiles

```bash
# Sincronizar eventos desde la fuente externa
python manage.py sync_events

# Sincronizar puntos de interés
python manage.py sync_points_of_interest

# Mantenimiento de puntos (ver ayuda del comando)
python manage.py delete_puntos
```

---

## Tests y calidad

```bash
# Tests Django
python manage.py test --noinput

# Pytest (requiere `pytest-django`; `DJANGO_SETTINGS_MODULE` en pytest.ini)
pytest
```

Análisis estático con Pylint (el CI lo ejecuta dentro del contenedor):

```bash
pylint . --exit-zero
```

---

## CI/CD

En `.github/workflows/ci-django.yml`:

1. Se construye y levanta el proyecto con **Docker Compose**.
2. Se espera a PostgreSQL, se ejecuta **Pylint**, **migraciones** y **`manage.py test`**.
3. Tras un push a `main` / `develop`, un job de **deploy** puede actualizar la instancia remota vía SSH (requiere secretos configurados en el repositorio, p. ej. `LIGHTSAIL_SSH_KEY`).

Ajusta ramas y secretos según tu organización.

---

## Licencia y contacto

Documentación OpenAPI generada con **drf-yasg**. Contacto indicado en el esquema de la API: `pesbcngo@gmail.com`.

Si mejoras el despliegue, documenta en este README los cambios en URLs públicas o requisitos nuevos para que el equipo las tenga centralizadas.
