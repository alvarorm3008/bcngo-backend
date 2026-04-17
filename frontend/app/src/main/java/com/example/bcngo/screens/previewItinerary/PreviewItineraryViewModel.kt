package com.example.bcngo.screens.previewItinerary

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bcngo.model.Itinerary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

class PreviewItineraryViewModel : ViewModel() {
    // Ahora mantenemos el Itinerary deserializado
    val itinerary = mutableStateOf<Itinerary?>(null)
    private val _isItinerarySaved = MutableStateFlow(false)
    val isItinerarySaved: StateFlow<Boolean> = _isItinerarySaved

    // Función para deserializar el JSON y guardar el Itinerary
    fun setItinerary(data: String) {
        try {
            // Limita la precisión de las fechas a milisegundos
            val cleanedData = data.replace(Regex("""\.\d{3,}Z"""), "")
            Log.d("PreviewItineraryViewModel", "Cleaned data: $cleanedData")

            // Deserializa el JSON modificado
            val parsedItinerary = Json.decodeFromString<Itinerary>(cleanedData)
            itinerary.value = parsedItinerary
            Log.d("PreviewItineraryViewModel", "Parsed Itinerary: $parsedItinerary")
        } catch (e: Exception) {
            e.printStackTrace()
            itinerary.value = null
            Log.e("PreviewItineraryViewModel", "Error deserializing data", e)
        }
    }

    // Función para eliminar un punto de un día específico
    fun removePointFromDay(dayIndex: Int, point: String) {
        // Verifica si el itinerario y los días existen
        val currentItinerary = itinerary.value
        currentItinerary?.let {
            // Obtén la lista mutable de los días
            val updatedItems = it.items.toMutableList()

            // Asegúrate de que el índice del día sea válido
            val day = updatedItems.getOrNull(dayIndex)

            day?.let {
                // Elimina el punto de la lista de puntos de ese día
                val updatedPoints = it.points.filter { p -> p.toString() != point }
                // Actualiza los puntos de ese día
                it.points = updatedPoints
            }

            // Actualiza el itinerario con los días modificados
            itinerary.value = it.copy(items = updatedItems)
        }
    }

    fun markItinerarySaved(saved: Boolean) {
        _isItinerarySaved.value = saved
    }
}
