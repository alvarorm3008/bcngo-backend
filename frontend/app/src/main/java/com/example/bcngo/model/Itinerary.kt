package com.example.bcngo.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime


@Serializable
data class Itinerary(
    val id: Int,
    val name: String,
    val user: Int,
    val start_date: LocalDate?,
    val end_date: LocalDate?,
    val created_at: LocalDateTime,
    val updated_at: LocalDateTime,
    val favourite: Boolean,
    val manual: List<Int>,
    val items: List<Item>,
    val days_of_stay: Int,
)
