package com.example.bcngo.model
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Event (
    val id: Int,
    val name: String,
    val longitude: Double?,
    val latitude: Double?,
    val start_date: LocalDateTime,
    val end_date: LocalDateTime,
    val road_name: String?,
    val district: String,
    val street_number: String?,
    val is_favorite: Boolean
)