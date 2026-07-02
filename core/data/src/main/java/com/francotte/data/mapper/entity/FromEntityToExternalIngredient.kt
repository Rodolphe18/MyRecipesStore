package com.francotte.data.mapper.entity

import com.francotte.database.model.IngredientEntity
import com.francotte.model.Ingredient

fun IngredientEntity.asExternalModel() =
    Ingredient(
        name = name,
        description = description,
        imageUrl = imageUrl
    )
