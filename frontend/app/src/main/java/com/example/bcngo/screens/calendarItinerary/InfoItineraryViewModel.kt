package com.example.bcngo.screens.calendarItinerary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.datetime.LocalDate

class InfoItineraryViewModel : ViewModel() {
    val itineraryName = MutableLiveData<String>()
    val startDate = MutableLiveData<LocalDate>()
    val endDate = MutableLiveData<LocalDate>()

    fun setItinerary(name: String, start: LocalDate, end: LocalDate) {
        itineraryName.value = name
        startDate.value = start
        endDate.value = end
    }
}
