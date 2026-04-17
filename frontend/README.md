# BCN GO

Aplicación Android para descubrir Barcelona: puntos de interés, itinerarios (manuales y automáticos), eventos, movilidad (paradas cercanas: metro, bus, Bicing) y comunidad mediante chats y reseñas. Incluye autenticación con correo y Google, notificaciones push y panel de administración.

---

## Características principales

- **Cuenta y seguridad**: registro, inicio de sesión, Google Sign-In, recuperación de contraseña, perfil editable y JWT almacenado de forma local.
- **Itinerarios**: creación manual (selección de puntos) o automática (categorías y rareza), listado, edición y eliminación.
- **Mapas y ubicación**: Google Maps en Compose, permisos de ubicación y consulta de paradas cercanas al usuario.
- **Eventos**: calendario, favoritos y detalle; integración con chats de evento.
- **Chats**: conversaciones por evento, mensajes, notificaciones FCM al unirse a un chat.
- **Puntos de interés y reseñas**: exploración, valoraciones, moderación (reporte) y flujos de administración.
- **Pasaporte / gamificación**: consulta y marcado de puntos visitados.
- **Administración**: gestión de usuarios (roles, bloqueo), revisión de contenido reportado (reseñas y mensajes), según permisos del backend.

---

## Stack tecnológico

| Área | Tecnología |
|------|------------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navegación | Navigation Compose |
| Red | Ktor Client, OkHttp, kotlinx.serialization |
| Async | Kotlin Coroutines |
| Mapas y ubicación | Maps Compose, Play Services (Maps, Location) |
| Autenticación / analítica | Firebase (Auth, Analytics, Cloud Messaging) |
| Calidad | ktlint, Detekt |
| Build | Gradle (Kotlin DSL), AGP 8.x |

- **minSdk**: 28 · **targetSdk / compileSdk**: 34  
- **JDK de compilación**: 21 (ver `compileOptions` / `kotlinOptions` en el módulo `app`)

---

## Requisitos previos

- [Android Studio](https://developer.android.com/studio) reciente (recomendado: Hedgehog o superior) con Android SDK 34.
- **JDK 21** instalado y configurado para el proyecto.
- Cuenta de **Firebase** con `google-services.json` válido para el paquete `com.example.bcngo`.
- **Clave de API de Google Maps** para Android (restricciones recomendadas por firma y paquete). El proyecto usa el plugin *Secrets Gradle*; en producción conviene no commitear claves en el manifiesto y cargarlas de forma segura.

---

## Puesta en marcha

1. **Clonar el repositorio**

   ```bash
   git clone <url-del-repositorio>
   cd bcngo-frontend-main
   ```

2. **`local.properties`**  
   Android Studio suele generarlo automáticamente. Debe incluir la ruta del SDK, por ejemplo:

   ```properties
   sdk.dir=/ruta/a/Android/sdk
   ```

3. **Firebase**  
   Coloca el archivo `google-services.json` en el directorio `app/` (el mismo nivel que `build.gradle.kts` del módulo app), descargado desde la consola de Firebase para tu proyecto.

4. **Backend**  
   La URL base del API está centralizada en `ApiService` (`BASE_URL`). Ajusta ese valor para apuntar a tu entorno (desarrollo, staging o producción).

5. **Sincronizar y ejecutar**  
   Abre el proyecto en Android Studio, sincroniza Gradle y ejecuta la configuración **app** en un emulador o dispositivo físico.

### Línea de comandos

```bash
chmod +x ./gradlew
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Para comprobaciones de estilo (pueden configurarse en CI con `continue-on-error` según tu política):

```bash
./gradlew ktlintCheck
./gradlew detekt
```

---

## CI/CD (GitHub Actions)

El workflow en `.github/workflows/ci.yml` (ramas `main` y `develop`) suele incluir:

- Compilación debug (`assembleDebug`).
- ktlint y Detekt.
- Tests unitarios.
- Tests instrumentados en dispositivo (`connectedDebugAndroidTest`; en GitHub suele requerir emulador o dispositivo en la nube).

La **distribución en Firebase App Distribution** y la autenticación con Google Cloud dependen de secretos del repositorio (`GOOGLE_CREDENTIALS_JSON`, `FIREBASE_APP_ID`, etc.). Sin esos secretos, el job puede fallar en los pasos posteriores a la compilación; puedes limitar el workflow a build y tests en forks o entornos personales.

---

## Estructura del código (resumen)

```
app/src/main/java/com/example/bcngo/
├── MainActivity.kt          # Entrada Compose + tema
├── navigation/              # Rutas y grafo de navegación
├── network/                 # ApiService, cliente HTTP
├── model/                   # DTOs y serialización
├── screens/                 # Pantallas por flujo (login, itinerarios, chats, admin…)
├── components/              # UI reutilizable
├── ui/theme/                # Colores, tipografía, tema
└── utils/                   # Servicios auxiliares (p. ej. FCM)
```

---

## Seguridad y buenas prácticas

- No subas a un repositorio público claves de Maps, OAuth ni tokens de Firebase sin restricciones.
- Revisa `network_security_config` y el uso de HTTP claro vs HTTPS según tu backend.
- Rota las claves si alguna ha sido expuesta en el historial del repo.

---

## Licencia

Especifica aquí la licencia del proyecto (por ejemplo MIT, Apache 2.0 o la que corresponda a tu organización).

---

*BCN GO — frontend Android. Requiere un backend compatible con los endpoints consumidos en `ApiService`.*
