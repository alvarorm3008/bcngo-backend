package com.example.bcngo.model

import kotlinx.serialization.Serializable

@Serializable
data class PassportPoints(
    val message: String,
    val data: List<PassportPointWrapper>
)

@Serializable
data class PassportPointWrapper(
    val point_of_interest: PassportPoint,
    val is_marked: Boolean
)

@Serializable
data class PassportPoint(
    val id: Int,
    var name: String,
    val latitude: Double?,  // Cambiado a Double
    val longitude: Double?, // Cambiado a Double
    val category: String,
    var imageResId: Int? = null // Nuevo campo para almacenar la imagen
)