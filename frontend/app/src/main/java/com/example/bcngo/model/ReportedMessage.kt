package com.example.bcngo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportedMessage(
    val id: Int,
    val message: String,
    @SerialName("creator_name") val creatorName: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("reports_count") val reportsCount: Int
)