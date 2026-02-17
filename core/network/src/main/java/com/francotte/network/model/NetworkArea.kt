package com.francotte.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkAreas(
    @SerialName("meals") val areas: List<NetworkArea>,
)

@Serializable
data class NetworkArea(
    val strArea: String,
)
