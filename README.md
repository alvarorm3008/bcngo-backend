# BCNGo

Monorepo for **BCNGo**, a culture and leisure project focused on Barcelona. It includes both the **REST API (Django)** and the **Android app (Kotlin)** in one repository.

| Part | Path | Description |
|------|------|-------------|
| **Backend** | [`backend/`](backend/) | Django, DRF, PostgreSQL, JWT, Firebase Admin, Swagger |
| **Frontend** | [`frontend/`](frontend/) | Android app with Gradle |

## Quick Requirements

- **Backend:** Python 3.9+, Docker (optional), PostgreSQL.
- **Frontend:** Android Studio / JDK (see [`frontend/README.md`](frontend/README.md)).

## Getting Started

### API (from repository root)

```bash
cd backend
docker compose up --build
```

Migrations (first setup):

```bash
docker exec bcngo-container python manage.py migrate
```

Swagger: http://localhost:8000/swagger/

More details: [**backend/README.md**](backend/README.md).

### Android App

```bash
cd frontend
./gradlew assembleDebug
```

The file `frontend/app/google-services.json` is not versioned (it contains credentials). Copy it locally as described in [**frontend/README.md**](frontend/README.md). In GitHub Actions, define the **`GOOGLE_SERVICES_JSON`** secret with the full JSON content so CI can build the app.

## CI/CD

Workflows are located in [`.github/workflows/`](.github/workflows/):

- **`ci-django.yml`** — Docker Compose, Pylint, migrations, and backend tests (only when files under `backend/` or the workflow itself change).
- **`ci-android.yml`** — Build, ktlint, detekt, tests, and Firebase Distribution deployment when applicable (only when files under `frontend/` or the workflow itself change).

## Backend Deployment on Server

If you deploy using the Django workflow SSH job, the remote machine should keep this structure (for example after `git pull`):

```text
.../bcngo-backend/    # or your folder name
  backend/
    docker-compose.yml
    ...
```

Remote commands use the **`backend/`** subdirectory for `docker compose`. If your server had the old repo layout with files at root level, move or re-clone the project to align paths.

## Repository Structure

```text
.
├── README.md                 # This file
├── backend/                  # Django project (API)
├── frontend/                 # Android project
└── .github/workflows/        # Backend and frontend CI
```

---

API contact (OpenAPI documentation): `pesbcngo@gmail.com`.
