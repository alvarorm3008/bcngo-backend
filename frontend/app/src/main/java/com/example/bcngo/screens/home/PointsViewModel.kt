package com.example.bcngo.screens.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcngo.model.InterestPoint
import com.example.bcngo.network.ApiService
import com.example.bcngo.network.InterestPointsListViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PointsViewModel : ViewModel() {

    private val userLocation = MutableLiveData<LatLng?>()

    private val _nearbyPoints = MutableLiveData<List<InterestPoint>>()
    val nearbyPoints: LiveData<List<InterestPoint>> = _nearbyPoints

    //val interestPoints by InterestPointsListViewModel()

    private val _allPoints = MutableLiveData<List<InterestPoint>?>()


    // Actualizamos la ubicación del usuario y recalculamos los puntos cercanos
    fun updateUserLocation(location: LatLng) {
        userLocation.value = location
        getAllPointsFromApi()
        calculateNearbyPoints() // Calculamos los puntos cercanos cada vez que cambia la ubicación
    }


    // Metodo para obtener todos los puntos de interés desde la API
    fun getAllPointsFromApi() {
        viewModelScope.launch {
            val points = ApiService.getAllInterestPoints()
            if (points != null) {
                _allPoints.value = points
                calculateNearbyPoints() // Volver a calcular los puntos cercanos cuando obtenemos los puntos
                //Log.d("PointsViewModel", "Puntos cargados desde la API: $points")
            } else {
                Log.e("PointsViewModel", "Error al obtener puntos desde la API")
            }
        }
    }

    // Metodo para calcular los puntos cercanos
    fun calculateNearbyPoints() {
        val userLocation = userLocation.value
        val allPoints = _allPoints.value ?: emptyList()
        //Log.d("PointsViewModel", "Cantidad de puntos en allPoints: ${allPoints.size}")

        // Log para ver la ubicación del usuario
        //Log.d("PointsViewModel", "Ubicación del usuario: $userLocation")

        if (userLocation == null) {
            _nearbyPoints.value = emptyList()
            return
        }

        viewModelScope.launch {
            // Filtrar los puntos cercanos en un hilo de fondo
            val nearby = withContext(Dispatchers.Default) {
                allPoints.filter { point ->
                    point.latitude?.let { lat ->
                        point.longitude?.let { lng ->
                            val pointLocation = LatLng(lat.toDouble(), lng.toDouble())
                            val dist = distance(userLocation, pointLocation)
                            // Log para ver la distancia calculada
                            //Log.d("PointsViewModel", "Distancia a ${point.name}: $dist metros")
                            dist <= 500 // Puntos dentro de 500m
                        } ?: false
                    } ?: false
                }
            }
            _nearbyPoints.value = nearby
            // Log para ver los puntos cercanos
            //Log.d("PointsViewModel", "Puntos cercanos: ${nearby.joinToString { it.name ?: "Sin nombre" }}")
        }
    }

    // Metodo para calcular la distancia entre dos puntos
    fun distance(latLng1: LatLng, latLng2: LatLng): Double {
        val earthRadius = 6371000.0 // Radio de la Tierra en metros

        val lat1 = Math.toRadians(latLng1.latitude)
        val lon1 = Math.toRadians(latLng1.longitude)
        val lat2 = Math.toRadians(latLng2.latitude)
        val lon2 = Math.toRadians(latLng2.longitude)

        val dlat = lat2 - lat1
        val dlon = lon2 - lon1

        val a = sin(dlat / 2) * sin(dlat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dlon / 2) *sin(dlon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // Resultado en metros
    }
}
