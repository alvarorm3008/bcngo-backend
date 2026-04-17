package com.example.bcngo.model
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Message (
    val id: Int,
    val message: String,
    val creator_name: String,
    val created_at: LocalDateTime,
)
