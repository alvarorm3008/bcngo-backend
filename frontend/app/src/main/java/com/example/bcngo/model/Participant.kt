package com.example.bcngo.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Participant (
    val username: String,
    val fcm_token: String,
    val joined_at: LocalDateTime,
)