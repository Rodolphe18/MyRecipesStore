package com.francotte.myrecipesstore.network.model

import kotlinx.serialization.Serializable


@Serializable
data class NetworkIngredients(val meals:List<NetworkIngredient>)

@Serializable
data class NetworkIngredient(val idIngredient:String, val strIngredient:String)


