package com.example.bcngo.model

import kotlinx.serialization.Serializable

@Serializable
data class InterestPoint(
    val id: Int,
    val name: String,
    val address_name: String?,
    val street_number: String?,
    val latitude: Float?,
    val longitude: Float?,
    val phone: String?,
    val web_url: String?,
    val category: String,
    val esencial: Boolean,
)
