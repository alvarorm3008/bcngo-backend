# BCNGo

Monorepo del proyecto **BCNGo**: cultura y ocio en Barcelona. Incluye la **API REST (Django)** y la **app Android (Kotlin)** en un solo repositorio.

| Parte | Ruta | Descripción |
|--------|------|-------------|
| **Backend** | [`backend/`](backend/) | Django, DRF, PostgreSQL, JWT, Firebase Admin, Swagger |
| **Frontend** | [`frontend/`](frontend/) | App Android con Gradle |

## Requisitos rápidos

- **Backend:** Python 3.9+, Docker (opcional), PostgreSQL.
- **Frontend:** Android Studio / JDK (ver [`frontend/README.md`](frontend/README.md)).

## Puesta en marcha

### API (desde la raíz del repo)

```bash
cd backend
docker compose up --build
```

Migraciones (primera vez):

```bash
docker exec bcngo-container python manage.py migrate
```

Swagger: http://localhost:8000/swagger/

Más detalle: [**backend/README.md**](backend/README.md).

### App Android

```bash
cd frontend
./gradlew assembleDebug
```

El archivo `frontend/app/google-services.json` no se versiona (contiene claves). Cópialo en local según [**frontend/README.md**](frontend/README.md). En GitHub Actions, define el secreto **`GOOGLE_SERVICES_JSON`** con el contenido completo del JSON para que el CI pueda compilar.

## CI/CD

Los workflows viven en [`.github/workflows/`](.github/workflows/):

- **`ci-django.yml`** — Docker Compose, Pylint, migraciones y tests del backend (solo si cambian archivos bajo `backend/` o el propio workflow).
- **`ci-android.yml`** — Compilación, ktlint, detekt, tests y despliegue a Firebase Distribution cuando aplica (solo si cambian archivos bajo `frontend/` o el propio workflow).

## Despliegue del backend en servidor

Si despliegas con el job SSH del workflow Django, en la máquina remota el código debe quedar con esta estructura (por ejemplo tras `git pull`):

```text
.../bcngo-backend/    # o el nombre de tu carpeta
  backend/
    docker-compose.yml
    ...
```

Los comandos remotos usan el subdirectorio **`backend/`** para `docker compose`. Si tu servidor tenía el proyecto en la raíz del repo antiguo, mueve o reclona para alinear rutas.

## Estructura del repositorio

```text
.
├── README.md                 # Este archivo
├── backend/                  # Proyecto Django (API)
├── frontend/                 # Proyecto Android
└── .github/workflows/        # CI backend y frontend
```

---

Contacto API (documentación OpenAPI): `pesbcngo@gmail.com`.
