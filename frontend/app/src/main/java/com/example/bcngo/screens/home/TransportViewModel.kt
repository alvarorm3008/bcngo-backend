package com.example.bcngo.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcngo.network.ApiService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bcngo.model.BicingStop
import com.example.bcngo.model.BusStop
import com.example.bcngo.model.MetroStop


class TransportViewModel : ViewModel() {

    // Estado para las paradas de transporte
    private val _busStops = MutableLiveData<List<BusStop>>(emptyList())
    val busStops: LiveData<List<BusStop>> = _busStops

    private val _metroStops = MutableLiveData<List<MetroStop>>(emptyList())
    val metroStops: LiveData<List<MetroStop>> = _metroStops

    private val _bikeStations = MutableLiveData<List<BicingStop>>(emptyList())
    val bikeStations: LiveData<List<BicingStop>> = _bikeStations

    private val _selectedTransport = MutableLiveData<Any?>(null) // Puede ser BusStop, MetroStop o BicingStop
    val selectedTransport: LiveData<Any?> = _selectedTransport

    private val _isTransportCardVisible = MutableLiveData(false)
    val isTransportCardVisible: LiveData<Boolean> = _isTransportCardVisible

    fun setTransportCardVisible(visible: Boolean) {
        _isTransportCardVisible.value = visible
    }

    // Funciones para manejar el estado de visibilidad y transporte seleccionado
    fun showTransportCard(transport: Any?) {
        _selectedTransport.value = transport
        _isTransportCardVisible.value = true
    }

    fun hideTransportCard() {
        _selectedTransport.value = null
        _isTransportCardVisible.value = false
    }

    fun setSelectedTransport(transport: Any?) {
        _selectedTransport.value = transport
    }

    // Función para obtener las paradas de transporte cercanas
    fun fetchTransportPoints(context: Context, userLocation: LatLng) {
        viewModelScope.launch {
            try {
                val response = ApiService.getNearbyStops(context, userLocation.latitude, userLocation.longitude, distance = 500)

                // Asignamos las paradas de bus, metro y bicicletas a sus respectivos estados
                if (response != null) {
                    _busStops.value = response.paradesBus
                    _metroStops.value = response.paradesMetro
                    _bikeStations.value = response.paradesBicing

                }


            } catch (e: Exception) {
                // Manejo de errores, por ejemplo, mostrando un mensaje de error
            }
        }
    }
}
