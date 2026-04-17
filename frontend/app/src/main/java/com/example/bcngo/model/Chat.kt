package com.example.bcngo.model
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Chat (
    val id: Int,
    val event: Int,
    val event_name: String,
    val created_at: LocalDateTime,
    val participants: List<Participant>
)