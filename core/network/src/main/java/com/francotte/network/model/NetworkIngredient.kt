package com.francotte.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkIngredients(
    val ingredients: List<NetworkIngredient>,
)

@Serializable
data class NetworkIngredient(
    val idIngredient: String,
    val strIngredient: String,
)
