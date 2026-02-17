package com.francotte.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkIngredients(
    @SerialName("meals") val ingredients: List<NetworkIngredient>,
)

@Serializable
data class NetworkIngredient(
    val idIngredient: String?=null,
    val strIngredient: String?=null,
    val strDescription:String?=null,
    val strThumb:String?=null
)
