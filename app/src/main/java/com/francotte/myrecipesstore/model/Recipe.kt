package com.francotte.myrecipesstore.model

import com.francotte.myrecipesstore.user.UserData
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject


object RecipePolymorphicSerializer :
    JsonContentPolymorphicSerializer<AbstractRecipe>(AbstractRecipe::class) {

    private val fullRecipeFields = setOf("strCategory", "strInstructions")

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out AbstractRecipe> {
        val keys = element.jsonObject.keys
        return if (fullRecipeFields.any { it in keys }) {
            Recipe.serializer()
        } else {
            LightRecipe.serializer()
        }
    }
}

@Serializable(with = RecipePolymorphicSerializer::class)
sealed class AbstractRecipe {
    abstract val strMeal: String
    abstract val strMealThumb: String
    abstract val idMeal: String
}

@Serializable
data class LightRecipe(
    override val strMeal: String,
    override val strMealThumb: String,
    override val idMeal: String,
) :
    AbstractRecipe()

@Serializable
data class Recipe(
    override val idMeal: String,
    override val strMeal: String,
    val strMealAlternate: String?,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String?,
    override val strMealThumb: String,
    val strTags: String?,
    val strYoutube: String,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strIngredient16: String?,
    val strIngredient17: String?,
    val strIngredient18: String?,
    val strIngredient19: String?,
    val strIngredient20: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?,
    val strMeasure16: String?,
    val strMeasure17: String?,
    val strMeasure18: String?,
    val strMeasure19: String?,
    val strMeasure20: String?,
    val strSource: String?,
    val strImageSource: String?,
    val strCreativeCommonsConfirmed: String?,
    val dateModified: String?,
) : AbstractRecipe()

@Serializable
data class RecipeResult(val meals: List<AbstractRecipe>)

data class LikeableRecipe(
    val recipe: AbstractRecipe,
    val isFavorite: Boolean
) {
    constructor(recipe: AbstractRecipe, userData: UserData) : this(
        recipe = recipe,
        isFavorite = recipe.idMeal in userData.favoriteRecipesIds
    )
}

fun List<AbstractRecipe>.mapToLikeableRecipes(userData: UserData): List<LikeableRecipe> =
    mapNotNull {
        when (it) {
            is Recipe -> LikeableRecipe(it, userData)
            is LightRecipe -> LikeableRecipe(it, userData)
        }
    }