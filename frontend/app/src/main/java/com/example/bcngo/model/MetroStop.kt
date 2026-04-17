package com.example.bcngo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetroStop(
    @SerialName("id") val id: Int,
    @SerialName("latitud") val latitud: Double,
    @SerialName("longitud") val longitud: Double,
    @SerialName("nom") val nom: String,
    @SerialName("liniesMetro") val liniesMetro: List<String>
)
