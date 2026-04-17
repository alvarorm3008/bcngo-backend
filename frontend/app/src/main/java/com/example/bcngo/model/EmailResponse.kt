package com.example.bcngo.model

import kotlinx.serialization.Serializable

@Serializable
data class EmailResponse(
    val emails: List<String>
)