package com.example.bcngo.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewModelSerializer(
    val id: Int,
    val user_email: String,
    val comment: String?,
    val reports_count: Int
)