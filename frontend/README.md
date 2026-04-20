# BCN GO

Part of the BCNGo monorepo: this code lives under **`frontend/`**. The file `app/google-services.json` is not committed; in **GitHub Actions**, configure the **`GOOGLE_SERVICES_JSON`** secret with the full JSON content so CI can build correctly.

Android app to discover Barcelona: points of interest, itineraries (manual and automatic), events, mobility (nearby metro, bus, and Bicing stops), and community features through chats and reviews. Includes email/Google authentication, push notifications, and an admin panel.

---

## Main Features

- **Account and security:** sign up, sign in, Google Sign-In, password recovery, editable profile, and locally stored JWT.
- **Itineraries:** manual creation (point selection) or automatic generation (categories and rarity), plus listing, editing, and deletion.
- **Maps and location:** Google Maps with Compose, location permissions, and nearby stop lookup.
- **Events:** calendar, favorites, details, and integration with event chats.
- **Chats:** event-based conversations, messages, and FCM notifications when joining chats.
- **Points of interest and reviews:** browsing, ratings, moderation/reporting, and admin workflows.
- **Passport / gamification:** check and mark visited points.
- **Administration:** user management (roles, blocking), review of reported content (reviews and messages), based on backend permissions.

---

## Tech Stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Networking | Ktor Client, OkHttp, kotlinx.serialization |
| Async | Kotlin Coroutines |
| Maps and location | Maps Compose, Play Services (Maps, Location) |
| Auth / analytics | Firebase (Auth, Analytics, Cloud Messaging) |
| Quality | ktlint, Detekt |
| Build | Gradle (Kotlin DSL), AGP 8.x |

- **minSdk:** 28 · **targetSdk / compileSdk:** 34  
- **Build JDK:** 21 (see `compileOptions` / `kotlinOptions` in the `app` module)

---

## Prerequisites

- A recent [Android Studio](https://developer.android.com/studio) version (recommended: Hedgehog or newer) with Android SDK 34.
- **JDK 21** installed and configured for the project.
- A **Firebase** project with a valid `google-services.json` for package `com.example.bcngo`.
- A **Google Maps API key** for Android (package/signature restrictions recommended). The project uses the *Secrets Gradle* plugin; for production, avoid committing keys in the manifest and load them securely.

---

## Getting Started

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd bcngo-frontend-main
   ```

2. **`local.properties`**  
   Android Studio usually generates it automatically. It should include your SDK path, for example:

   ```properties
   sdk.dir=/path/to/Android/sdk
   ```

3. **Firebase**  
   Place `google-services.json` inside `app/` (same level as the module `build.gradle.kts`), downloaded from the Firebase console for your project.

4. **Backend**  
   The API base URL is centralized in `ApiService` (`BASE_URL`). Update that value to point to your environment (development, staging, or production).

5. **Sync and run**  
   Open the project in Android Studio, sync Gradle, and run the **app** configuration on an emulator or physical device.

### Command Line

```bash
chmod +x ./gradlew
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Style checks (you can configure CI behavior with `continue-on-error` depending on your policy):

```bash
./gradlew ktlintCheck
./gradlew detekt
```

---

## CI/CD (GitHub Actions)

The workflow in `.github/workflows/ci.yml` (branches `main` and `develop`) usually includes:

- Debug build (`assembleDebug`)
- ktlint and Detekt
- Unit tests
- Instrumentation tests on device (`connectedDebugAndroidTest`; on GitHub this usually requires emulator/cloud device setup)

**Firebase App Distribution** and Google Cloud authentication depend on repository secrets (`GOOGLE_CREDENTIALS_JSON`, `FIREBASE_APP_ID`, etc.). Without those secrets, steps after build may fail; in forks/personal environments, you can restrict the workflow to build and tests.

---

## Code Structure (Summary)

```text
app/src/main/java/com/example/bcngo/
├── MainActivity.kt          # Compose entry point + theme
├── navigation/              # Routes and navigation graph
├── network/                 # ApiService, HTTP client
├── model/                   # DTOs and serialization
├── screens/                 # Screens by flow (login, itineraries, chats, admin...)
├── components/              # Reusable UI
├── ui/theme/                # Colors, typography, theme
└── utils/                   # Helper services (e.g., FCM)
```

---

## Security and Best Practices

- Do not upload unrestricted Maps keys, OAuth secrets, or Firebase tokens to public repositories.
- Review `network_security_config` and clear HTTP vs HTTPS usage based on your backend setup.
- Rotate keys immediately if any secret has been exposed in repository history.

---

## License

Specify the project license here (for example MIT, Apache 2.0, or your organization's license).

---

*BCN GO — Android frontend. Requires a backend compatible with the endpoints consumed in `ApiService`.*
