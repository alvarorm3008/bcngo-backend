# BCNGo — Backend API

REST API for **BCNGo**, a platform focused on culture and leisure in Barcelona (events, points of interest, itineraries, cultural passport, and users).

This code lives in the monorepo under **`backend/`**. From the repository root, enter this directory before running Docker or `manage.py`.

Main stack: **Django 4.2**, **Django REST Framework**, **PostgreSQL**, **JWT** authentication, **Firebase** notifications, and **OpenAPI documentation (Swagger / ReDoc)**.

---

## Contents

- [Requirements](#requirements)
- [Docker Setup](#docker-setup)
- [Local Setup (Without Docker)](#local-setup-without-docker)
- [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Useful Management Commands](#useful-management-commands)
- [Tests and Code Quality](#tests-and-code-quality)
- [CI/CD](#cicd)

---

## Requirements

| Environment | Recommended Version |
|-------------|---------------------|
| Python | 3.9+ (aligned with the `Dockerfile`) |
| PostgreSQL | Compatible with `psycopg2-binary` in `requirements.txt` |
| Docker / Docker Compose | For the containerized workflow described below |

---

## Docker Setup

1. Clone the monorepo and enter the backend folder:

   ```bash
   cd backend
   ```

2. Ensure the Firebase credentials file exists at the expected location (default: `./config/firebase-key.json`, or the path defined in `FIREBASE_KEY_PATH`).

3. Start services:

   ```bash
   docker compose up --build
   ```

   The app will be available at **http://localhost:8000** (mapped port from `docker-compose.yml`).

4. In another terminal, apply migrations (first setup or after model changes):

   ```bash
   docker exec bcngo-container python manage.py migrate
   ```

5. To stop services (from `backend/`):

   ```bash
   docker compose down
   ```

> **Note:** The Docker image configures **cron** to run daily event synchronization (`sync_events`). Check `config/mycron` if you need a different schedule or command.

---

## Local Setup (Without Docker)

1. From monorepo root, enter `backend/`, create and activate a virtual environment:

   ```bash
   cd backend
   python3 -m venv venv
   source venv/bin/activate   # Linux / macOS
   # .\venv\Scripts\activate  # Windows
   ```

2. Install dependencies:

   ```bash
   pip install -r requirements.txt
   ```

3. Configure PostgreSQL and adjust `DATABASES` in `bcngo_backend/settings.py` (or migrate this config to environment variables for deployment). User, password, and database must match your local instance.

4. Export required environment variables (next section), including the Firebase JSON path.

5. Apply migrations and start the development server:

   ```bash
   python manage.py migrate
   python manage.py runserver
   ```

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `FIREBASE_KEY_PATH` | Path to Firebase Admin credentials JSON. Default: `./config/firebase-key.json`. |
| `EMAIL_HOST_USER` | SMTP username (for features like password recovery). |
| `EMAIL_HOST_PASSWORD` | Email password or app password. |

These values are read with `python-decouple` where applicable; you can use a `.env` file inside **`backend/`** (do not commit it if it contains secrets).

**Best practice:** never commit real database credentials, `SECRET_KEY`, or Firebase JSON files. Add `firebase-key.json` and `.env` to `.gitignore` in real environments if not already ignored.

---

## API Documentation

With the server running:

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8000/swagger/ |
| **ReDoc** | http://localhost:8000/redoc/ |
| **Django Admin** | http://localhost:8000/admin/ |

Main API prefixes (see `bcngo_backend/urls.py`):

| Prefix | Module |
|--------|--------|
| `/puntosdeinteres/` | Points of interest and reviews |
| `/itinerario/` | Itineraries |
| `/users/` | Users and authentication |
| `/pasaporte/` | Cultural passport |
| `/eventos/` | Events and related features |
| `/` (via `servicio`) | Integration with external YAML service |

---

## Project Structure

| Path | Role |
|------|------|
| `bcngo_backend/` | Settings, root URLs, templates (e.g., password reset) |
| `config/` | Cron and configuration resources (e.g., Firebase) |
| `users/` | Custom user model, JWT, auth views |
| `puntosdeinteres/` | POIs and external API synchronization |
| `itinerario/` | Itineraries |
| `pasaporte/` | Cultural passport |
| `eventos/` | Events, group chats, favorites, synchronization |
| `servicio/` | Service endpoints / YAML contract |

---

## Useful Management Commands

```bash
# Sync events from external source
python manage.py sync_events

# Sync points of interest
python manage.py sync_points_of_interest

# Point maintenance (see command help)
python manage.py delete_puntos
```

---

## Tests and Code Quality

```bash
# Django tests
python manage.py test --noinput

# Pytest (requires `pytest-django`; `DJANGO_SETTINGS_MODULE` in pytest.ini)
pytest
```

Static analysis with Pylint (also executed by CI in containerized flow):

```bash
pylint . --exit-zero
```

---

## CI/CD

In `.github/workflows/ci-django.yml` (monorepo root):

1. Runs only when files under **`backend/`** (or the workflow file itself) change.
2. Builds and starts the project using **Docker Compose** from `backend/`.
3. Waits for PostgreSQL, then runs **Pylint**, **migrations**, and **`manage.py test`**.
4. After a **push** to `main` / `develop`, a **deploy** job can update the remote instance via SSH (secrets such as `LIGHTSAIL_SSH_KEY`). On the server, repository layout must include the `backend/` subfolder with `docker-compose.yml`.

Adjust branches, remote paths, and secrets based on your organization needs.

---

## License and Contact

OpenAPI docs are generated with **drf-yasg**. Contact listed in the API schema: `pesbcngo@gmail.com`.

If you improve deployment, update this README with public URLs or new requirements so the team has centralized documentation.
