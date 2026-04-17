package com.example.bcngo.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: Int,
    val point_of_interest: Int,
    val user_email: String,
    val username: String,
    val comment: String?,
    val rating: Int,
    val reports_count: Int,
    val created_at: LocalDateTime,
    val updated_at: LocalDateTime,
)
