package com.example.bcngo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusStop(
    @SerialName("NomParada") val nomParada: String,
    @SerialName("CodiParada") val codiParada: Int,
    @SerialName("Longitud") val longitud: Double,
    @SerialName("Latitud") val latitud: Double,
    @SerialName("Linies") val linies: List<String>
)