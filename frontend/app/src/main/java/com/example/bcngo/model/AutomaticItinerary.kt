package com.example.bcngo.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class AutomaticItinerary(
    val name: String,
    val start_date: LocalDate,
    val end_date: LocalDate,
    val categories: List<String>,
    val rarity: String
)