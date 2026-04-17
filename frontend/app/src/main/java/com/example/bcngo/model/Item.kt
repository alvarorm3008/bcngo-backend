package com.example.bcngo.model

import kotlinx.serialization.Serializable

// Clase para los elementos (items) del itinerario
@Serializable
data class Item(
    val id: Int,
    val day: Int,
    var points: List<String>,
)
