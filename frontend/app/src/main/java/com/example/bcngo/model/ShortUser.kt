package com.example.bcngo.model

import kotlinx.serialization.Serializable

@Serializable
data class ShortUser(
    val id: Int,
    val username: String,
    val email: String,
    val notifications: Boolean
)