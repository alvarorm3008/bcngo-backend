package com.example.bcngo.screens.manualItinerary
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class SelectedPointsViewModel : ViewModel() {
    val selectedPoints = mutableStateListOf<Int>()

    fun addPoint(pointId: Int) {
        if (!selectedPoints.contains(pointId)) {
            selectedPoints.add(pointId)
        }
    }

    fun removePoint(pointId: Int) {
        selectedPoints.remove(pointId)
    }

    fun clearPoints() {
        selectedPoints.clear()
    }
}
