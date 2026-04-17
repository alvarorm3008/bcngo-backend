FROM python:3.9-slim

ENV PYTHONUNBUFFERED=1

# Instalar cron para automatizacion de tareas
RUN apt-get update && apt-get install -y cron

WORKDIR /app

COPY requirements.txt .

RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# Copiar el código de tu aplicación
COPY . .

# Copiar el archivo cron al contenedor
COPY ./config/mycron /etc/cron.d/sync_events_cron

# Dar permisos al archivo cron
RUN chmod 0644 /etc/cron.d/sync_events_cron

# Aplicar el cronjob
RUN crontab /etc/cron.d/sync_events_cron

# Para que cron ejecute tareas y el servidor Django funcione
CMD cron && python manage.py runserver 0.0.0.0:8000

