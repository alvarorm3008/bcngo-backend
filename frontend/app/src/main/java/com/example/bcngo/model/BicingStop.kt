package com.example.bcngo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BicingStop(
    @SerialName("id") val id: Int,
    @SerialName("latitud") val latitud: Double,
    @SerialName("longitud") val longitud: Double,
    @SerialName("nom") val nom: String,
    @SerialName("adreca") val adreca: String,
    @SerialName("capacitat") val capacitat: Int,
    @SerialName("physicalConf") val physicalConf: String,
    @SerialName("lastUpdated") val lastUpdated: Long,
    @SerialName("nDisponibles") val nDisponibles: Int,
    @SerialName("nDocksDisponibles") val nDocksDisponibles: Int,
    @SerialName("nMecaniquesDisponibles") val nMecaniquesDisponibles: Int,
    @SerialName("nElectriquesDisponibles") val nElectriquesDisponibles: Int
)