package com.example.bcngo.model

import androidx.compose.runtime.MutableState
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val email: String,
    val password: String,
)
