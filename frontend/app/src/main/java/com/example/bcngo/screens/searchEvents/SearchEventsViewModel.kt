package com.example.bcngo.screens.searchEvents

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.bcngo.model.Event
import com.example.bcngo.network.ApiService
import com.example.bcngo.network.ApiService.doFavouriteEvent
import com.example.bcngo.network.ApiService.doUnfavouriteEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchEventsViewModel : ViewModel() {
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> = _events

    private var allEvents: List<Event> = emptyList()  // Almacenar todos los eventos

    private val previousSelectedMode: MutableLiveData<EventFilterMode> = MutableLiveData(EventFilterMode.All)
    //private val selectedMode: MutableLiveData<EventFilterMode> = MutableLiveData(EventFilterMode.All)

    private val _favoriteStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val favoriteStates: StateFlow<Map<Int, Boolean>> = _favoriteStates


    private val _selectedMode = MutableLiveData(EventFilterMode.All)
    val selectedMode: LiveData<EventFilterMode> = _selectedMode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        filterEvents(newQuery)
    }

    fun onModeSelected(mode: EventFilterMode, context: Context) {
        previousSelectedMode.value = selectedMode.value
        _selectedMode.value = mode
        Log.d("SearchEvents", "Selected Mode viewmodel onmodeselected: $selectedMode")
        updateEvents(context)
    }

    private fun updateEvents(context: Context) {
        Log.d("SearchEvents", "Selected Mode viewmodel update: $selectedMode")
        viewModelScope.launch {
            if (_selectedMode.value != previousSelectedMode.value) {
                // Si el modo ha cambiado, realiza la llamada correspondiente a la API
                fetchFilteredEvents(_selectedMode.value!!, context)
            } else {
                // Si no ha cambiado el modo, aplica el filtro local
                filterEvents(_searchQuery.value ?: "")
            }
        }
    }

    private suspend fun fetchFilteredEvents(mode: EventFilterMode, context: Context) {
        Log.d("SearchEvents", "Selected Mode viewmodel fetchfiltered: $selectedMode")
        _isLoading.value = true // Inicia el estado de carga
        try {
            val fetchedEvents = when (mode) {
                EventFilterMode.All -> ApiService.getAllEvents(context)
                EventFilterMode.Today -> ApiService.getTodayEvents(context)
                EventFilterMode.Day -> null
                EventFilterMode.Month -> ApiService.getMonthEvents(context)
            }
            fetchedEvents?.let {
                allEvents = it
                filterEvents(_searchQuery.value ?: "")
            }
        } finally {
            _isLoading.value = false // Finaliza el estado de carga
        }
    }

    // Filtra los eventos en función del texto de búsqueda
    private fun filterEvents(query: String) {
        val filteredList = if (query.isEmpty()) {
            allEvents
        } else {
            allEvents.filter { it.name.contains(query, ignoreCase = true) }
        }
        _events.value = filteredList
    }

    fun fetchEvents(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true // Inicia el estado de carga
            try {
                val fetchedEvents = ApiService.getAllEvents(context)
                fetchedEvents?.let {
                    allEvents = it
                    filterEvents(_searchQuery.value ?: "")
                    // Initialize favorite states
                    val favoriteMap = it.associate { event -> event.id to event.is_favorite }
                    _favoriteStates.value = favoriteMap
                }
            } finally {
                _isLoading.value = false // Finaliza el estado de carga
            }
        }
    }

    fun fetchFavoriteEvents(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true // Inicia el estado de carga
            try {
                val fetchedEvents = ApiService.getFavouriteEvents(context) // Llamada a la API para obtener solo los favoritos
                fetchedEvents?.let {
                    allEvents = it
                    filterEvents(_searchQuery.value ?: "") // Aplica el filtro después de obtener los eventos favoritos
                    // Initialize favorite states
                    val favoriteMap = it.associate { event -> event.id to event.is_favorite }
                    _favoriteStates.value = favoriteMap
                }
            } finally {
                _isLoading.value = false // Finaliza el estado de carga
            }
        }
    }


    fun onDateSelected(selectedDateMillis: Long, context: Context) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Formato ISO-8601
        val selectedDate = dateFormat.format(Date(selectedDateMillis))

        viewModelScope.launch {
            _isLoading.value = true // Inicia el estado de carga
            try {
                val fetchedEvents = ApiService.getDayEvents(context, date = selectedDate) // Envía la fecha como String
                fetchedEvents?.let {
                    allEvents = it
                    filterEvents(_searchQuery.value ?: "") // Aplica el filtro después de obtener datos
                }
            } finally {
                _isLoading.value = false // Finaliza el estado de carga
            }
        }
    }

    fun toggleFavorite(eventId: Int, context: Context, removeFromList: Boolean = false) {
        viewModelScope.launch {
            val currentFavorites = _favoriteStates.value.toMutableMap()
            val isFavorite = currentFavorites[eventId] ?: false
            currentFavorites[eventId] = !isFavorite
            _favoriteStates.value = currentFavorites

            // Update the favorite state in the backend
            if (isFavorite) {
                doUnfavouriteEvent(context, eventId)
                if (removeFromList) {
                    // Remove the event from the list if it is unfavorited
                    _events.value = _events.value?.filter { it.id != eventId }
                }
            } else {
                doFavouriteEvent(context, eventId)
            }
        }
    }



}

enum class EventFilterMode {
    All,
    Today,
    Day,
    Month,
}
