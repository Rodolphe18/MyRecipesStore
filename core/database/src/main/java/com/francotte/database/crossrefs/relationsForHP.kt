package com.francotte.database.crossrefs

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.francotte.database.model.AreaEntity
import com.francotte.database.model.IngredientEntity
import com.francotte.database.model.LatestListEntity
import com.francotte.database.model.LightCategoryEntity
import com.francotte.database.model.LightRecipeEntity

data class AreaWithRecipes(
    @Embedded val area: AreaEntity,

    @Relation(
        parentColumn = "strArea",
        entityColumn = "idMeal",
        associateBy = Junction(
            value = RecipeAreaCrossRef::class,
            parentColumn = "strArea",
            entityColumn = "idMeal"
        )
    )
    val recipes: List<LightRecipeEntity>
)

data class CategoryWithRecipes(
    @Embedded val category: LightCategoryEntity,
    @Relation(
        parentColumn = "strCategory",
        entityColumn = "idMeal",
        associateBy = Junction(
            value = RecipeCategoryCrossRef::class,
            parentColumn = "strCategory",
            entityColumn = "idMeal"
        )
    )
    val recipes: List<LightRecipeEntity>
)


data class IngredientWithRecipes(
    @Embedded val ingredient: IngredientEntity,

    @Relation(
        parentColumn = "name",
        entityColumn = "idMeal",
        associateBy = Junction(
            value = RecipeIngredientCrossRef::class,
            parentColumn = "ingredientName",
            entityColumn = "idMeal"
        )
    )
    val recipes: List<LightRecipeEntity>
)
