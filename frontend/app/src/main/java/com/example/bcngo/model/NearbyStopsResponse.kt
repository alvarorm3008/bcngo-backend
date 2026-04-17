package com.example.bcngo.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class NearbyStopsResponse(
    @SerialName("ParadesBus") val paradesBus: List<BusStop>,
    @SerialName("ParadesMetro") val paradesMetro: List<MetroStop>,
    @SerialName("ParadesBicing") val paradesBicing: List<BicingStop>
)