package com.francotte.model

data class CustomRecipe(val id:String, val title: String,
                               val ingredients: List<CustomIngredient>,
                               val instructions: String,
                               val imageUrl: String?)

data class CustomIngredient(
    val name: String,
    val quantity: String,
    val measureType: String
)