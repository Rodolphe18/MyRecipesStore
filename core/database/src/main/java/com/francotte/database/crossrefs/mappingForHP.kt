package com.francotte.database.crossrefs

import androidx.room.Entity
import androidx.room.Index
import java.time.Instant

@Entity(
    tableName = "recipe_area_xref",
    primaryKeys = ["strArea", "idMeal"],
    indices = [Index("idMeal"), Index("savedTimestamp")]
)
data class RecipeAreaCrossRef(
    val strArea: String,
    val idMeal: String,
    val savedTimestamp: Instant,
)

@Entity(
    tableName = "recipe_category_xref",
    primaryKeys = ["strCategory", "idMeal"],
    indices = [Index("idMeal"), Index("savedTimestamp")]
)
data class RecipeCategoryCrossRef(
    val strCategory: String,
    val idMeal: String,
    val savedTimestamp: Instant,
)

@Entity(
    tableName = "recipe_ingredient_xref",
    primaryKeys = ["ingredientName", "idMeal"],
    indices = [Index("idMeal"), Index("savedTimestamp")]
)
data class RecipeIngredientCrossRef(
    val ingredientName: String,
    val idMeal: String,
    val savedTimestamp: Instant
)
