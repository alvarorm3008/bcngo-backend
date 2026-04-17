// Importaciones necesarias
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.ktor.client.*
import io.ktor.client.call.* // Para `body` de la respuesta
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.* // Importa el plugin de logging
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Data class para InterestPoint
@Serializable
data class InterestPoint(
    val id: Int,
    val name: String,
    val address_name: String?,
    val street_number: String?,
    val latitude: Float?,
    val longitude: Float?,
    val phone: String?,
    val web_url: String?,
    val category: String,
    val esencial: Boolean,
)

// Configuración básica de Ktor Client
val client =
    HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true // Ignora las claves desconocidas en el JSON
                },
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

@Composable
fun InterestPointScreen() {
    val interestPoint = remember { mutableStateOf<InterestPoint?>(null) }

    // Llamada a la URL al inicializar el Composable
    LaunchedEffect(Unit) {
        Log.d("InterestPointScreen", "LaunchedEffect triggered")

        // Realiza la solicitud GET a la URL y deserializa directamente
        interestPoint.value =
            try {
                // Aquí se deserializa automáticamente a InterestPoint
                val point: InterestPoint = client.get("http://35.180.108.104/puntosdeinteres/7").body()
                Log.d("InterestPointScreen", "InterestPoint deserialized successfully: $point")
                point
            } catch (e: Exception) {
                Log.e("InterestPointScreen", "Error loading InterestPoint: ${e.message}")
                e.printStackTrace()
                null
            }
    }

    // Muestra los datos
    Column(modifier = Modifier.fillMaxSize()) {
        interestPoint.value?.let { point ->
            Log.d("InterestPointScreen", "Displaying InterestPoint data")
            Text("ID: ${point.id}", style = MaterialTheme.typography.titleMedium)
            Text("Name: ${point.name}", style = MaterialTheme.typography.titleMedium)
            Text("Address: ${point.address_name}", style = MaterialTheme.typography.titleMedium)
            Text("Latitude: ${point.latitude}", style = MaterialTheme.typography.titleMedium)
            Text("Longitude: ${point.longitude}", style = MaterialTheme.typography.titleMedium)
            Text("Category: ${point.category}", style = MaterialTheme.typography.titleMedium)
            Text("Esencial: ${point.esencial}", style = MaterialTheme.typography.titleMedium)

        } ?: run {
            Log.d("InterestPointScreen", "InterestPoint is null, displaying loading message")
            Text("Loading...", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInterestPointScreen() {
    InterestPointScreen()
}
