// src/main/kotlin/com/ejemplo/ui/TestViewModel.kt

package com.example.bcngo.network
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.model.Itinerary
import io.ktor.client.request.get

@Composable
fun InterestPointViewModel(id: Int): State<InterestPoint?> {
    val interestPoint = remember { mutableStateOf<InterestPoint?>(null) }

    LaunchedEffect(id) {
        Log.d("InterestPointViewModel", "LaunchedEffect triggered for ID: $id")
        interestPoint.value =
            try {
                val point = ApiService.getInterestPoint(id)
                Log.d("InterestPointViewModel", "InterestPoint deserialized successfully: $point")
                point
            } catch (e: Exception) {
                Log.e("InterestPointViewModel", "Error loading InterestPoint: ${e.message}")
                e.printStackTrace()
                null
            }
    }

    return interestPoint
}

@Composable
fun InterestPointsListViewModel(): State<List<InterestPoint>?> {
    // Crear un estado mutable para una lista de puntos de interés
    val interestPoints = remember { mutableStateOf<List<InterestPoint>?>(null) }

    LaunchedEffect(Unit) {
        Log.d(
            "InterestPointListViewModel",
            "LaunchedEffect triggered for loading all Interest Points",
        )
        interestPoints.value =
            try {
                val points = ApiService.getAllInterestPoints()
                Log.d("InterestPointListViewModel", "InterestPoints deserialized successfully: $points")
                points
            } catch (e: Exception) {
                Log.e("InterestPointListViewModel", "Error loading InterestPoints: ${e.message}")
                e.printStackTrace()
                null
            }
    }

    return interestPoints
}