package com.example.bcngo.network

import android.content.Context
import android.util.Log
import com.example.bcngo.R
import com.example.bcngo.exceptions.*
import com.example.bcngo.model.AutomaticItinerary
import com.example.bcngo.model.Chat
import com.example.bcngo.model.EmailResponse
import com.example.bcngo.model.Event
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.model.Itinerary
import com.example.bcngo.model.NearbyStopsResponse
import com.example.bcngo.model.Message
import com.example.bcngo.model.PassportPoints
import com.example.bcngo.model.ReportedMessage
import com.example.bcngo.model.Review
import com.example.bcngo.model.ShortItinerary
import com.example.bcngo.model.ShortUser
import com.example.bcngo.model.User
import com.example.bcngo.screens.review.ReviewData
import com.google.firebase.messaging.FirebaseMessaging
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object ApiService {

    private const val BASE_URL = "http://35.180.108.104"

    val client =
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true // Ignora claves desconocidas en el JSON
                        isLenient = true
                        useArrayPolymorphism = true
                    },
                )
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }



    // Obtener un punto de interés por ID
    suspend fun getInterestPoint(id: Int): InterestPoint? {
        return try {
            val response = client.get("$BASE_URL/puntosdeinteres/$id/")
            if (response.status.isSuccess()) { // Maneja códigos 2xx como éxito
                response.body()
            } else {
                Log.d("ApiService", "Error get: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al obtener punto de interés: ${e.message}")
            null
        }
    }

    // Obtener todos los puntos de interés
    suspend fun getAllInterestPoints(): List<InterestPoint>? {
        return try {
            val response = client.get("$BASE_URL/puntosdeinteres/")
            if (response.status.isSuccess()) {
                response.body()
            } else {
                Log.d("ApiService", "Error getAll: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al obtener puntos de interés: ${e.message}")
            null
        }
    }

    // Función para enviar el token al backend usando Ktor
    /*suspend fun sendTokenUserToBackend(idToken: String) {
        // Preparar el cuerpo de la solicitud con el token
        val requestBody = mapOf("id_token" to idToken)
        Log.d("ApiService", "Request Body: $requestBody")

        try {
            // Hacemos la solicitud POST al backend
            val response: HttpResponse = client.post("$BASE_URL/users/") {
                contentType(ContentType.Application.Json) // Definimos el tipo de contenido como JSON
                setBody(requestBody) // Usamos setBody para enviar el cuerpo de la solicitud
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")
        } catch (e: Exception) {
            // Manejo de errores
            Log.e("ApiService", "Error al enviar el token: ${e.message}")
        }
    }*/

    suspend fun sendUserCredentials(
        context: Context,
        username: String,
        email: String,
        password: String,
    ) {
        val user = User(username = username, email = email, password = password)

        // Convertir el objeto a JSON
        val jsonBody = Json.encodeToString(user)

        // Cliente HTTP
        val client = HttpClient()

        try {
            // Enviar el JSON a un servidor
            val response: HttpResponse =
                client.post("$BASE_URL/users/register") {
                    contentType(ContentType.Application.Json)
                    setBody(jsonBody)
                }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            // Comprobamos si la respuesta fue exitosa (status 200 o 201)
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                // Usuario registrado correctamente
                Log.d("ApiService", "Usuario registrado con éxito.")
                val responseBody = response.bodyAsText()
                val token = Json.decodeFromString<Map<String, String>>(responseBody)["jwt"]
                if (token != null) {
                    saveTokenToSharedPreferences(context, token) // Guardar el token
                    Log.d("ApiService", "Token guardado en SharedPreferences: $token")
                } else {
                    Log.e("ApiService", "No se encontró el token en la respuesta.")
                }
            } else {
                // Verificamos si es un error 400 relacionado con el correo o el nombre de usuario
                if (response.status == HttpStatusCode.BadRequest) {
                    val responseBody = response.bodyAsText()

                    // Verificamos si el correo es inválido
                    if (responseBody.contains("Enter a valid email address")) {
                        throw InvalidEmailException(context.getString(R.string.invalid_email))
                    }

                    // Verificamos si el correo ya está registrado
                    if (responseBody.contains("User with this email already exists")) {
                        throw EmailAlreadyExistsException(
                            context.getString(R.string.email_already_exists)
                        )
                    }

                    // Verificamos si el nombre de usuario ya está registrado
                    if (responseBody.contains("User with this username already exists")) {
                        throw UsernameAlreadyExistsException(
                            context.getString(R.string.username_already_exists)
                        )
                    }
                }

                // Si no es un error conocido, lanzamos una excepción genérica
                throw Exception(
                    context.getString(R.string.server_error),
                )
            }
        } catch (e: Exception) {
            // Aquí lanzamos la excepción nuevamente para que pueda ser capturada en la función de signUp
            throw e
        } finally {
            client.close()
        }
    }

    suspend fun sendUserCredentialsLogIn(
        context: Context,
        email: String,
        password: String,
    ) {
        val loginData = mapOf(
            "email" to email,
            "password" to password,
        )

        val jsonBody = Json.encodeToString(loginData)
        val client = HttpClient()

        try {
            val response: HttpResponse = client.post("$BASE_URL/users/login") {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            // Verificamos si la respuesta es exitosa (200 OK)
            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                val token = Json.decodeFromString<Map<String, String>>(responseBody)["jwt"]
                if (token != null) {
                    saveTokenToSharedPreferences(context, token) // Guardar el token
                    Log.d("ApiService", "Token guardado en SharedPreferences: $token")
                } else {
                    Log.e("ApiService", "No se encontró el token en la respuesta.")
                }
            } else {
                // Si el usuario no es encontrado (401 Unauthorized) o si hay un error en el login, lanzar una excepción
                if (response.status == HttpStatusCode.Unauthorized) {
                    val responseBody = response.bodyAsText()
                    if (responseBody.contains("La cuenta está bloqueada")) {
                        throw AccountBlockedException(context.getString(R.string.account_locked))
                    } else {
                        throw UserNotFoundException(context.getString(R.string.user_not_found))
                    }
                } else {
                    throw Exception(context.getString(R.string.server_error))
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e // Volver a lanzar la excepción para que logIn pueda manejarla
        } finally {
            client.close()
        }
    }

    suspend fun sendUserCredentialsGoogle(
        context: Context,
        tokenGoogle: String
    ) {

        val jsonBody = Json.encodeToString(mapOf("token" to tokenGoogle))
        val client = HttpClient()

        Log.d("ApiService", "Request Body: $jsonBody")

        try {
            val response: HttpResponse =
                client.post("$BASE_URL/users/logingoogle") {
                    contentType(ContentType.Application.Json)
                    setBody(jsonBody)
                }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            // Verificamos si la respuesta es exitosa (200 OK)
            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                val token = Json.decodeFromString<Map<String, String>>(responseBody)["jwt"]
                if (token != null) {
                    saveTokenToSharedPreferences(context, token) // Guardar el token
                    Log.d("ApiService", "Token guardado en SharedPreferences: $token")
                } else {
                    Log.e("ApiService", "No se encontró el token en la respuesta.")
                }
            } else {
                // Si el usuario no es encontrado (401 Unauthorized) o si hay un error en el login, lanzar una excepción
                if (response.status == HttpStatusCode.Unauthorized) {
                    throw UserNotFoundException(context.getString(R.string.user_not_found))
                } else {
                    throw Exception(context.getString(R.string.server_error))
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e // Volver a lanzar la excepción para que logIn pueda manejarla
        } finally {
            client.close()
        }
    }

    suspend fun isAdminApi(context: Context): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiServiceAdmin", "No se encontró el token en SharedPreferences.")
            return false // Si no hay token, no puede ser admin
        }

        val client = HttpClient()

        try {
            val response: HttpResponse = client.get("$BASE_URL/users/is_admin") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d("ApiServiceAdmin", "Response: ${response.status.value}: ${response.bodyAsText()}")

            return when (response.status) {
                HttpStatusCode.OK -> {
                    // El usuario es administrador
                    true
                }
                HttpStatusCode.Forbidden -> {
                    // El usuario no es administrador
                    false
                }
                else -> {
                    // En caso de otros errores, lanzamos una excepción
                    throw Exception(context.getString(R.string.server_error))
                }
            }
        } catch (e: Exception) {
            Log.e("ApiServiceAdmin", "Exception: ${e.localizedMessage}")
            throw e
        } finally {
            client.close()
        }
    }




    suspend fun sendItineraryToBackend(
        context: Context,
        name: String,
        start_date: LocalDate,
        end_date: LocalDate,
        selectedPoints: List<Int>
    ): String? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        val itineraryRequest = ShortItinerary(name, start_date, end_date, selectedPoints)
        val jsonBody = Json.encodeToString(itineraryRequest)
        val client = HttpClient()

        Log.d("ApiService", "Request Body: $jsonBody")

        try {
            val response: HttpResponse = client.post("$BASE_URL/itinerario/api/create/") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(jsonBody)
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.Created) {
                Log.d("ApiService", "Itinerario creado con éxito.")
                return response.bodyAsText() // Retorna el contenido del cuerpo de la respuesta
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e
        } finally {
            client.close()
        }
    }

    // Función para enviar el itinerario automático al backend
    suspend fun sendAutomaticItineraryToBackend(
        context: Context,
        name: String,
        start_date: LocalDate,
        end_date: LocalDate,
        selectedCategories: List<String>,
        rarity: String,
    ): String? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        // Crear el objeto de la solicitud
        val itineraryRequest = AutomaticItinerary(
            name = name,
            start_date = start_date,
            end_date = end_date,
            categories = selectedCategories,
            rarity = rarity
        )

        // Convertir el objeto de la solicitud a JSON
        val jsonBody = Json.encodeToString(itineraryRequest)
        val client = HttpClient()

        Log.d("ApiService", "Request Body: $jsonBody")

        try {
            val response: HttpResponse = client.post("$BASE_URL/itinerario/api/create/") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(jsonBody) // Establecer el cuerpo de la solicitud
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.Created) {
                Log.d("ApiService", "Itinerario creado con éxito.")
                return response.bodyAsText() // Retorna el contenido del cuerpo de la respuesta
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e
        } finally {
            client.close()
        }
    }

    suspend fun deleteItineraryApi(
        context: Context,
        itineraryId: Int,
    ) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }

        val client = HttpClient()

        try {
            val response: HttpResponse = client.delete(
                "$BASE_URL/itinerario/api/$itineraryId/delete/",
            ) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                // Consideramos éxito tanto para 204 como para 200
                Log.d("ApiService", "Itinerario eliminado con éxito.")
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e
        } finally {
            client.close()
        }
    }

    // Esta función actualiza todos los Items del itinerario
    suspend fun updateAllItemsOfItineraryApi(
        context: Context,
        itinerary: Itinerary, // El itinerario completo con todos los Items
    ) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }

        val client = HttpClient()

        try {
            // Serializamos directamente la lista de items en el formato JSON esperado
            val itemsJson = Json.encodeToString(
                mapOf("items" to itinerary.items),
            )

            val response: HttpResponse = client.put(
                "$BASE_URL/itinerario/api/${itinerary.id}/update-multiple-days/",
            ) {
                headers {
                    append("Authorization", "Bearer $token")
                    append(
                        "Content-Type",
                        "application/json",
                    ) // Aseguramos que enviamos los datos en formato JSON
                }
                // Enviamos el JSON de los items como cuerpo de la solicitud
                setBody(itemsJson)
            }
            Log.d("ApiService", "Itinerario Request Body: $itemsJson")

            Log.d("ApiService", "Itinerario Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) { // Normalmente PUT devuelve 200 OK si se actualizó correctamente
                Log.d("ApiService", "Items del itinerario actualizados con éxito.")
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e
        } finally {
            client.close()
        }
    }

    fun saveTokenToSharedPreferences(
        context: Context,
        token: String,
    ) {
        val sharedPref = context.getSharedPreferences(
            "com.example.bcngo.PREFERENCIAS",
            Context.MODE_PRIVATE,
        )
        with(sharedPref.edit()) {
            putString("AUTH_TOKEN", token)
            apply() // Guarda el token de forma asíncrona
        }
    }

    fun getTokenFromSharedPreferences(context: Context): String? {
        val sharedPref = context.getSharedPreferences(
            "com.example.bcngo.PREFERENCIAS",
            Context.MODE_PRIVATE,
        )
        return sharedPref.getString("AUTH_TOKEN", null) // Devuelve el token o null si no existe
    }

    fun removeTokenFromSharedPreferences(context: Context) {
        val sharedPref = context.getSharedPreferences(
            "com.example.bcngo.PREFERENCIAS",
            Context.MODE_PRIVATE,
        )
        with(sharedPref.edit()) {
            remove("AUTH_TOKEN") // Elimina el token almacenado
            apply() // Aplica los cambios de forma asíncrona
        }
    }

    suspend fun getAllItineraries(context: Context): List<Itinerary>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        val client = HttpClient()

        return try {
            val response: HttpResponse = client.get("$BASE_URL/itinerario/api/list/") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val itineraries = Json.decodeFromString<List<Itinerary>>(cleanedData)
                Log.d("ApiService", "Itinerarios obtenidos con éxito.")
                itineraries
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    suspend fun createPointOfInterest(pointOfInterest: InterestPoint, context: Context): InterestPoint? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }
        return try {
            val response = client.post("$BASE_URL/puntosdeinteres/create/") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(pointOfInterest)
            }
            if (response.status.isSuccess()) {
                response.body()
            } else {
                Log.d("ApiService", "Error post: ${response.status.value} - ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.message}")
            null
        }
    }
    suspend fun deletePointOfInterest(id: Int, context: Context): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }
        return try {
            val response = client.delete("$BASE_URL/puntosdeinteres/delete/$id/") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            when (response.status) {
                HttpStatusCode.NoContent -> {
                    Log.d("ApiService", "Punto de interés eliminado con éxito.")
                    true
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e("ApiService", "Autenticación requerida para acceder a este endpoint.")
                    false
                }
                HttpStatusCode.Forbidden -> {
                    Log.e("ApiService", "No tienes permisos para realizar esta acción.")
                    false
                }
                HttpStatusCode.NotFound -> {
                    Log.e("ApiService", "Punto de interés no encontrado.")
                    false
                }
                else -> {
                    Log.e("ApiService", "Error delete: ${response.status.value} - ${response.bodyAsText()}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.message}")
            false
        }
    }

    suspend fun getProfile(context: Context): ShortUser? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/users/profile") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")
            response.body() // Asegúrate de que ShortUser sea el tipo correcto
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }



    suspend fun updateUsername(context: Context, username: String): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }

        return try {
            val response: HttpResponse = client.post("$BASE_URL/users/profile/edit") {
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(mapOf("username" to username))
            }

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            false
        }
    }

    suspend fun updateProfile(context: Context, username: String, notifications: Boolean): Boolean {
        Log.d("ApiService", "Notifications: $notifications")
        Log.d("ApiService", "Username: $username")
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }

        return try {
            val requestBody = ShortUser(id = 0, username = username, email = "", notifications = notifications)
            val response: HttpResponse = client.patch("$BASE_URL/users/profile/edit") {
                headers {
                    append("Authorization", "Bearer $token")
                    append("Content-Type", "application/json")
                }
                setBody(Json.encodeToString(requestBody))
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            false
        }
    }



    suspend fun updatePassword(context: Context, oldPassword: String, newPassword: String): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }

        return try {
            val response: HttpResponse = client.post("$BASE_URL/users/profile/change-password") {
                headers {
                    append("Authorization", "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(mapOf("current_password" to oldPassword, "new_password" to newPassword))
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            false
        }
    }

    suspend fun forgotPassword(email: String, context: Context) {
        try {
            val response: HttpResponse = client.post("$BASE_URL/users/password-reset") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email))
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")
            when (response.status) {
                HttpStatusCode.BadRequest -> {
                    val errorMessage = context.getString(R.string.restart_password_google)
                    throw Exception(errorMessage)
                }
                HttpStatusCode.NotFound -> {
                    val errorMessage = context.getString(R.string.user_not_found)
                    throw Exception(errorMessage)
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e
        }
    }

    suspend fun isGoogleUser(context: Context): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/users/is-google") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val jsonResponse = Json.decodeFromString<JsonObject>(response.bodyAsText())
                jsonResponse["is_google"]?.jsonPrimitive?.booleanOrNull ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            false
        }
    }



    suspend fun deleteProfile(context: Context) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }

        try {
            val response: HttpResponse = client.delete("$BASE_URL/users/profile/delete") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            false
        }
    }

    //Eventos
    suspend fun getAllEvents(context: Context): List<Event>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/eventos/all") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                // Usa el deserializador configurado con ignoreUnknownKeys
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val events = Json.decodeFromString<List<Event>>(cleanedData)

                //val events = response.body<List<Event>>()
                Log.d("ApiService", "Todos los eventos obtenidos con éxito.")
                events
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun getEventById(context: Context, eventId: Int): Event? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/eventos/$eventId") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                // Usa el deserializador configurado con ignoreUnknownKeys
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val event = Json.decodeFromString<Event>(cleanedData)

                Log.d("ApiService", "Todos los eventos obtenidos con éxito.")
                event
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun getTodayEvents(context: Context): List<Event>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/eventos/current") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val events = Json.decodeFromString<List<Event>>(cleanedData)

                //val events = response.body<List<Event>>()
                Log.d("ApiService", "Eventos de hoy obtenidos con éxito.")
                events
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun getDayEvents(
        context: Context,
        date: String,
    ): List<Event>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/eventos/day/$date") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val events = Json.decodeFromString<List<Event>>(cleanedData)

                //val events = response.body<List<Event>>()
                Log.d("ApiService", "Eventos por dia obtenidos con éxito.")
                events
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun getMonthEvents(
        context: Context,
    ): List<Event>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/eventos/month") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val events = Json.decodeFromString<List<Event>>(cleanedData)

                //val events = response.body<List<Event>>()
                Log.d("ApiService", "Eventos por mes obtenidos con éxito.")
                events
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    //Favourites Events
    suspend fun doFavouriteEvent(
        context: Context,
        id: Int
    ) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }

        try {
            val response: HttpResponse = client.post("$BASE_URL/eventos/favorites/$id") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d("ApiService", "Response: ${response.status.value}")

            if (response.status == HttpStatusCode.Created) {
                Log.d("ApiService", "Evento marcado como favorito exitosamente.")
            } else {
                Log.e("ApiService", "Error en la respuesta del servidor , ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
        }
    }

    suspend fun doUnfavouriteEvent(
        context: Context,
        id: Int,
    ) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }
        Log.d("ApiService", "Token: $token")

        val client = HttpClient()

        try {
            val response: HttpResponse = client.delete(
                "$BASE_URL/eventos/favorites/$id",
            ) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                // Consideramos éxito tanto para 204 como para 200
                Log.d("ApiService", "Desfavorito evento hecho con éxito.")
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e
        } finally {
            client.close()
        }
    }

    suspend fun getFavouriteEvents(
        context: Context,
    ): List<Event>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/eventos/favorites") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val events = Json.decodeFromString<List<Event>>(cleanedData)

                //val events = response.body<List<Event>>()
                Log.d("ApiService", "Eventos favoritos obtenidos con éxito.")
                events
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun getMyChats(
        context: Context,
    ): List<Chat>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/eventos/chats") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val chats = Json.decodeFromString<List<Chat>>(cleanedData)

                //val events = response.body<List<Event>>()
                Log.d("ApiService", "Chats obtenidos con éxito.")
                chats
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun accederChat(tokenDisp: String, eventId: Int, context: Context) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }
        try {
            val response = client.post("$BASE_URL/eventos/chats/$eventId/join") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(mapOf("fcm_token" to tokenDisp))
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status.isSuccess()) {
                response.body<Unit>() // Specify the expected type explicitly
                Log.d("ApiService", "Acceder con éxito.")

            } else {
                Log.d("ApiService", "Error post: ${response.status.value} - ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.message}")
        }
    }

    suspend fun leaveChat(chatid: Int, context: Context) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }
        try {
            val response = client.post("$BASE_URL/eventos/chats/$chatid/leave") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status.isSuccess()) {
                response.body<Unit>() // Specify the expected type explicitly
                Log.d("ApiService", "Salir del chat con éxito.")

            } else {
                Log.d("ApiService", "Error post: ${response.status.value} - ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.message}")
        }
    }

    suspend fun getDeviceToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    continuation.resumeWithException(task.exception ?: Exception("Unknown error"))
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d("FCM", "FCM Registration Token: $token")
                continuation.resume(token)
            }
    }

    suspend fun getChatMessages(
        context: Context,
        chatId: Int
    ): List<Message>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/eventos/chats/$chatId/messages") {
                headers {
                    append("Authorization", "Bearer $token")
                }
                url {
                    parameters.append("page_size", "100")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val cleanedData = response.bodyAsText().replace(Regex("""\.\d{3,}Z"""), "")
                val jsonResponse = Json.decodeFromString<JsonResponse>(cleanedData)

                Log.d("ApiService", "Mensajes obtenidos con éxito.")
                jsonResponse.results
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun addMessage(chatId: Int, message: String, context: Context) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }
        try {
            val response = client.post("$BASE_URL/eventos/chats/$chatId/messages/create") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(mapOf("message" to message))
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status.isSuccess()) {
                response.body<Unit>() // Specify the expected type explicitly
                Log.d("ApiService", "Mensaje enviado con éxito.")
            } else {
                Log.d("ApiService", "Error post: ${response.status.value} - ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.message}")
        }
    }

    suspend fun deleteMessage(messageId: Int, context: Context) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }
        try {
            val response = client.delete("$BASE_URL/eventos/chats/messages/$messageId") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status.isSuccess()) {
                response.body<Unit>() // Specify the expected type explicitly
                Log.d("ApiService", "Mensaje eliminado con éxito.")
            } else {
                Log.d("ApiService", "Error post: ${response.status.value} - ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.message}")
        }
    }

    suspend fun reportMessage(messageId: Int, context: Context) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return
        }
        try {
            val response = client.post("$BASE_URL/eventos/chats/messages/$messageId/report") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status.isSuccess()) {
                response.body<Unit>() // Specify the expected type explicitly
                Log.d("ApiService", "Mensaje reportado con éxito.")
            } else {
                Log.d("ApiService", "Error post: ${response.status.value} - ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.message}")
        }
    }

    suspend fun getNearbyStops(context: Context, latitude: Double, longitude: Double, distance: Int): NearbyStopsResponse? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiServiceAdmin", "No se encontró el token en SharedPreferences.")
            return null
        }
        val client = HttpClient()

        val endpoint = "$BASE_URL/nearby-stops?latitud=$latitude&longitud=$longitude&distancia=$distance\""
        return try {
            // Perform the GET request with headers
            val response: HttpResponse = client.get(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            // Check if the response is successful
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                return Json.decodeFromString<NearbyStopsResponse>(responseBody)
            } else {
                Log.e("ApiService", "Error fetching nearby stops: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception fetching nearby stops: ${e.localizedMessage}")
            null
        }
    }
    suspend fun createReview(
        context: Context,
        pointOfInterestId: Int,
        comment: String,
        rating: Int
    ): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }
        val userProfile = getProfile(context)
        val userEmail = userProfile?.email ?: return false
        val username = userProfile.username ?: return false

        val reviewData = ReviewData(
            pointOfInterest = pointOfInterestId,
            userEmail = userEmail,
            username = username,
            comment = comment,
            rating = rating
        )
        val jsonBody = Json.encodeToString(reviewData)
        Log.d("ApiService", "Request Body: $jsonBody") // Log the request body
        val client = HttpClient()

        return try {
            val response: HttpResponse = client.post("$BASE_URL/puntosdeinteres/reviews/create/") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(jsonBody)
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}") // Log the response
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) { // Normalmente PUT devuelve 200 OK si se actualizó correctamente
                Log.d("ApiService", "Reseña creada con exito")
                return true
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            false
        } finally {
            client.close()
        }
    }

    suspend fun getAllReviews(context: Context, id: Int): List<Review>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No token found in SharedPreferences.")
            return null
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL/puntosdeinteres/reviews/$id/") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val rawBody = response.bodyAsText()
                Log.d("ApiService", "Raw Response Body: $rawBody")

                // Clean JSON date-time strings (if required by backend)
                val cleanedData = rawBody.replace(Regex("""\.\d{3,}Z"""), "")
                Log.d("ApiService", "Cleaned Data: $cleanedData")

                // Parse the cleaned JSON response
                val reviews = Json.decodeFromString<List<Review>>(cleanedData)
                Log.d("ApiService", "Reviews fetched successfully: $reviews")

                reviews
            } else {
                Log.e("ApiService", "Failed with status code: ${response.status}")
                null
            }
        } catch (e: SerializationException) {
            Log.e("ApiService", "Serialization Error: ${e.localizedMessage}")
            null
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            null
        }
    }

    suspend fun deleteReview(context: Context, reviewId: Int) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No token found in SharedPreferences.")
            return // No token, no action
        }
        try {
            val response = client.delete("$BASE_URL/puntosdeinteres/reviews/delete/$reviewId/"){
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Log.d("ApiService", "Review deleted successfully.")
            } else {
                throw Exception(context.getString(R.string.failed_delete_review))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Error deleting review: ${e.localizedMessage}")
        }
    }

    suspend fun reportReview(context: Context, reviewId: Int) {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No token found in SharedPreferences.")
            return // No token, no action
        }

        try {
            val response = client.post("$BASE_URL/puntosdeinteres/reviews/report/$reviewId/"){
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                Log.d("ApiService", "Review reported successfully.")
            } else {
                throw Exception(context.getString(R.string.failed_report_review))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Error reporting review: ${e.localizedMessage}")
        }
    }

    suspend fun getReviewById(reviewId: Int): Review? {
        return try {
            val response = client.get("$BASE_URL/reviews/$reviewId/")
            if (response.status.isSuccess()) {
                response.body()
            } else {
                Log.e("ApiService", "Failed to fetch review: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Error fetching review: ${e.localizedMessage}")
            null
        }
    }


    suspend fun getPassportPoints(context: Context): PassportPoints? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }
        Log.d("ApiService", "Token obtenido: $token")
        val client = HttpClient()

        val endpoint = "$BASE_URL/pasaporte/consultar/"
        return try {
            Log.d("ApiService", "Iniciando solicitud GET a $endpoint con token $token")
            // Realizar la solicitud GET con encabezados
            val response: HttpResponse = client.get(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Respuesta recibida: ${response.status.value}")
            // Verificar si la respuesta es exitosa
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                Log.d("ApiService", "Cuerpo de la respuesta: $responseBody")
                val passportPoints = Json.decodeFromString<PassportPoints>(responseBody)
                return passportPoints
            } else {
                Log.e("ApiService", "Error al obtener puntos del pasaporte: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al obtener puntos del pasaporte: ${e.localizedMessage}")
            null
        }
    }

    suspend fun markPointAsVisited(context: Context, pointId: Int): String? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        // Crear el objeto del cuerpo de la solicitud
        val pointRequest = mapOf("point_id" to pointId)

        // Convertir el cuerpo de la solicitud a JSON
        val jsonBody = Json.encodeToString(pointRequest)
        val client = HttpClient()

        Log.d("ApiService", "Request Body: $jsonBody")

        try {
            val response: HttpResponse = client.post("$BASE_URL/pasaporte/marcar/") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(jsonBody) // Establecer el cuerpo de la solicitud
            }

            Log.d("ApiService", "Response: ${response.status.value}: ${response.bodyAsText()}")

            if (response.status.isSuccess()) {
                Log.d("ApiService", "Punto marcado como visitado con éxito.")
                return response.bodyAsText() // Retorna el contenido de la respuesta si fue exitosa
            } else {
                throw Exception(context.getString(R.string.server_error))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Exception: ${e.localizedMessage}")
            throw e
        } finally {
            client.close()
        }
    }

    suspend fun getUserEmails(context: Context): List<String>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }
        Log.d("ApiService", "Token obtenido: $token")
        val client = HttpClient()

        val endpoint = "$BASE_URL/users/list"
        return try {
            Log.d("ApiService", "Iniciando solicitud GET a $endpoint con token $token")
            // Realizar la solicitud GET con encabezados
            val response: HttpResponse = client.get(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Respuesta recibida: ${response.status.value}")
            // Verificar si la respuesta es exitosa
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                Log.d("ApiService", "Cuerpo de la respuesta: $responseBody")
                // Decodificar la respuesta en un objeto de datos
                val emailResponse = Json.decodeFromString<EmailResponse>(responseBody)
                return emailResponse.emails
            } else {
                Log.e("ApiService", "Error al obtener correos: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al obtener correos: ${e.localizedMessage}")
            null
        }
    }

    suspend fun grantUserAdmin(context: Context, email: String): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }
        Log.d("ApiService", "Token obtenido: $token")
        val client = HttpClient()

        val endpoint = "$BASE_URL/users/make-admin"
        return try {
            Log.d("ApiService", "Iniciando solicitud POST a $endpoint con token $token")
            // Realizar la solicitud POST con encabezados y cuerpo
            val response: HttpResponse = client.post(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                    append("Content-Type", "application/json")
                }
                setBody(
                    """{
                    "email": "$email"
                }"""
                )
            }
            Log.d("ApiService", "Respuesta recibida: ${response.status.value}")
            // Verificar si la respuesta es exitosa
            if (response.status.isSuccess()) {
                Log.d("ApiService", "Usuario convertido en administrador con éxito.")
                true
            } else {
                Log.e("ApiService", "Error al convertir en administrador: ${response.status.value}")
                false
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al realizar la solicitud: ${e.localizedMessage}")
            false
        }
    }

    suspend fun revokeUserAdmin(context: Context, email: String): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }

        val client = HttpClient()
        val endpoint = "$BASE_URL/users/remove-admin"

        return try {
            Log.d("ApiService", "Iniciando solicitud POST para revocar permisos de administrador: $email")
            val response: HttpResponse = client.post(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody(
                    """{
                    "email": "$email"
                }"""
                )
            }

            Log.d("ApiService", "Respuesta recibida: ${response.status}")
            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                Log.d("ApiService", "Cuerpo de la respuesta: $responseBody")
                true
            } else {
                Log.e(
                    "ApiService",
                    "Error al revocar permisos de administrador: ${response.status.value}"
                )
                false
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al revocar permisos de administrador: ${e.localizedMessage}")
            false
        } finally {
            client.close()
        }
    }

    suspend fun blockUser(context: Context, email: String): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }
        Log.d("ApiService", "Token obtenido: $token")
        val client = HttpClient()

        val endpoint = "$BASE_URL/users/block-user"
        return try {
            Log.d("ApiService", "Iniciando solicitud POST a $endpoint con token $token")
            // Realizar la solicitud POST con encabezados y cuerpo
            val response: HttpResponse = client.post(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                    append("Content-Type", "application/json")
                }
                setBody(
                    """{
                    "email": "$email"
                }"""
                )
            }
            Log.d("ApiService", "Respuesta recibida: ${response.status.value}")
            // Verificar si la respuesta es exitosa
            if (response.status.isSuccess()) {
                Log.d("ApiService", "Usuario bloqueado con éxito: $email")
                true
            } else {
                Log.e("ApiService", "Error al bloquear usuario: ${response.status.value}")
                false
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al bloquear usuario: ${e.localizedMessage}")
            false
        }
    }

    suspend fun unblockUser(context: Context, email: String): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }

        val client = HttpClient()
        val endpoint = "$BASE_URL/users/unblock-user"

        return try {
            Log.d("ApiService", "Iniciando solicitud POST para desbloquear usuario: $email")
            val response: HttpResponse = client.post(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody(
                    """{
                    "email": "$email"
                }"""
                )
            }

            Log.d("ApiService", "Respuesta recibida: ${response.status}")
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e("ApiService", "Error al desbloquear usuario: ${e.localizedMessage}")
            false
        } finally {
            client.close()
        }
    }

    suspend fun isUserBlocked(context: Context, email: String): Boolean? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        val client = HttpClient()
        val endpoint = "$BASE_URL/users/check-user-blocked-by-email"

        return try {
            Log.d("ApiService", "Iniciando solicitud POST para verificar si el usuario está bloqueado: $email")

            val response: HttpResponse = client.post(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody("""{"email": "$email"}""")
            }

            Log.d("ApiService", "Respuesta recibida: ${response.status}")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val responseBody = response.bodyAsText()
                    Log.d("ApiService", "Cuerpo de la respuesta: $responseBody")

                    // Parsear el JSON para obtener el valor de "is_blocked"
                    val jsonElement = Json.parseToJsonElement(responseBody)
                    jsonElement.jsonObject["is_blocked"]?.jsonPrimitive?.booleanOrNull
                }
                HttpStatusCode.BadRequest -> {
                    Log.e("ApiService", "Error 400: El campo 'email' es obligatorio o el usuario no existe.")
                    null
                }
                HttpStatusCode.Forbidden -> {
                    Log.e("ApiService", "Error 403: Permisos insuficientes para realizar esta acción.")
                    null
                }
                else -> {
                    Log.e("ApiService", "Error inesperado: ${response.status.value}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al verificar usuario bloqueado: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }

    suspend fun isUserAdmin(context: Context, email: String): Boolean? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }

        val client = HttpClient()
        val endpoint = "$BASE_URL/users/check-if-user-is-admin"

        return try {
            Log.d("ApiService", "Iniciando solicitud POST para verificar si el usuario es administrador: $email")

            val response: HttpResponse = client.post(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody("""{"email": "$email"}""")
            }

            Log.d("ApiService", "Respuesta recibida: ${response.status}")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val responseBody = response.bodyAsText()
                    Log.d("ApiService", "Cuerpo de la respuesta: $responseBody")

                    // Parsear el JSON para obtener el valor de "is_admin"
                    val jsonElement = Json.parseToJsonElement(responseBody)
                    jsonElement.jsonObject["is_admin"]?.jsonPrimitive?.booleanOrNull
                }
                HttpStatusCode.BadRequest -> {
                    Log.e("ApiService", "Error 400: El campo 'email' es obligatorio o el usuario no existe.")
                    null
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e("ApiService", "Error 401: Autenticación requerida para realizar esta acción.")
                    null
                }
                else -> {
                    Log.e("ApiService", "Error inesperado: ${response.status.value}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al verificar si el usuario es administrador: ${e.localizedMessage}")
            null
        } finally {
            client.close()
        }
    }


    suspend fun getReportedReviews(context: Context): List<Review>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }
        Log.d("ApiService", "Token obtenido: $token")
        val client = HttpClient()

        val endpoint = "$BASE_URL/puntosdeinteres/reviews/reported/"
        return try {
            Log.d("ApiService", "Iniciando solicitud GET a $endpoint con token $token")
            val response: HttpResponse = client.get(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Respuesta recibida: ${response.status.value}")
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                Log.d("ApiService", "Cuerpo de la respuesta: $responseBody")
                val reviews = Json.decodeFromString<List<Review>>(responseBody)
                reviews
            } else {
                Log.e("ApiService", "Error al obtener reseñas reportadas: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al obtener reseñas reportadas: ${e.localizedMessage}")
            null
        }
    }

    suspend fun getReportedMessages(context: Context): List<ReportedMessage>? {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return null
        }
        Log.d("ApiService", "Token obtenido: $token")
        val client = HttpClient()

        val endpoint = "$BASE_URL/eventos/messages/reported"
        return try {
            Log.d("ApiService", "Iniciando solicitud GET a $endpoint con token $token")
            val response: HttpResponse = client.get(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Respuesta recibida: ${response.status.value}")
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                Log.d("ApiService", "Cuerpo de la respuesta: $responseBody")
                val messages = Json.decodeFromString<List<ReportedMessage>>(responseBody)
                messages
            } else {
                Log.e("ApiService", "Error al obtener mensajes reportados: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al obtener mensajes reportados: ${e.localizedMessage}")
            null
        }
    }



    suspend fun deleteMessage(context: Context, messageId: Int): Boolean {
        val token = getTokenFromSharedPreferences(context)
        if (token == null) {
            Log.e("ApiService", "No se encontró el token en SharedPreferences.")
            return false
        }
        Log.d("ApiService", "Token obtenido: $token")
        val client = HttpClient()

        val endpoint = "$BASE_URL/eventos/messages/$messageId/delete-admin"
        return try {
            Log.d("ApiService", "Iniciando solicitud DELETE a $endpoint con token $token")
            val response: HttpResponse = client.delete(endpoint) {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
            Log.d("ApiService", "Respuesta recibida: ${response.status.value}")
            when {
                response.status == HttpStatusCode.OK -> {
                    Log.d("ApiService", "Mensaje eliminado exitosamente.")
                    true
                }
                response.status == HttpStatusCode.NotFound -> {
                    Log.e("ApiService", "Mensaje no encontrado.")
                    false
                }
                response.status == HttpStatusCode.Forbidden -> {
                    Log.e("ApiService", "No tienes permiso para realizar esta acción.")
                    false
                }
                else -> {
                    Log.e("ApiService", "Error inesperado: ${response.status.value}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Excepción al eliminar mensaje reportado: ${e.localizedMessage}")
            false
        }
    }


}


@Serializable
data class JsonResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Message>
)

