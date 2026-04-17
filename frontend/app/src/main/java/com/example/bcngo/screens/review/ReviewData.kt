package com.example.bcngo.screens.review

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewData(
    @SerialName("point_of_interest") val pointOfInterest: Int,
    @SerialName("user_email") val userEmail: String,
    val comment: String,
    val rating: Int,
    val username: String
)