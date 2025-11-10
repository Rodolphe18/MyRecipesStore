package com.francotte.network.model

import com.francotte.model.LightRecipe
import com.francotte.model.Recipe
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject


object RecipePolymorphicSerializer :
    JsonContentPolymorphicSerializer<NetworkAbstractRecipe>(NetworkAbstractRecipe::class) {

    private val fullRecipeFields = setOf("strCategory", "strInstructions")

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out NetworkAbstractRecipe> {
        val keys = element.jsonObject.keys
        return if (fullRecipeFields.any { it in keys }) {
            NetworkRecipe.serializer()
        } else {
            NetworkLightRecipe.serializer()
        }
    }
}

@Serializable(with = RecipePolymorphicSerializer::class)
sealed class NetworkAbstractRecipe {
    abstract val strMeal: String
    abstract val strMealThumb: String?
    abstract val idMeal: String
}

@Serializable
data class NetworkLightRecipe(
    override val strMeal: String,
    override val strMealThumb: String?,
    override val idMeal: String,
) :
    NetworkAbstractRecipe()

@Serializable
data class NetworkRecipe(
    override val idMeal: String,
    override val strMeal: String,
    val strMealAlternate: String? = null,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String? = null,
    override val strMealThumb: String?,
    val strTags: String? = null,
    val strYoutube: String,
    val strIngredient1: String? = null,
    val strIngredient2: String? = null,
    val strIngredient3: String? = null,
    val strIngredient4: String? = null,
    val strIngredient5: String? = null,
    val strIngredient6: String? = null,
    val strIngredient7: String? = null,
    val strIngredient8: String? = null,
    val strIngredient9: String? = null,
    val strIngredient10: String? = null,
    val strIngredient11: String? = null,
    val strIngredient12: String? = null,
    val strIngredient13: String? = null,
    val strIngredient14: String? = null,
    val strIngredient15: String? = null,
    val strIngredient16: String? = null,
    val strIngredient17: String? = null,
    val strIngredient18: String? = null,
    val strIngredient19: String? = null,
    val strIngredient20: String? = null,
    val strMeasure1: String? = null,
    val strMeasure2: String? = null,
    val strMeasure3: String? = null,
    val strMeasure4: String? = null,
    val strMeasure5: String? = null,
    val strMeasure6: String? = null,
    val strMeasure7: String? = null,
    val strMeasure8: String? = null,
    val strMeasure9: String? = null,
    val strMeasure10: String? = null,
    val strMeasure11: String? = null,
    val strMeasure12: String? = null,
    val strMeasure13: String? = null,
    val strMeasure14: String? = null,
    val strMeasure15: String? = null,
    val strMeasure16: String? = null,
    val strMeasure17: String? = null,
    val strMeasure18: String? = null,
    val strMeasure19: String? = null,
    val strMeasure20: String? = null,
    val strSource: String? = null,
    val strImageSource: String? = null,
    val strCreativeCommonsConfirmed: String? = null,
    val dateModified: String? = null,
) : NetworkAbstractRecipe()

@Serializable
data class NetworkRecipeResult(val meals: List<NetworkAbstractRecipe>)


fun NetworkLightRecipe.asExternalModel() : LightRecipe =
    LightRecipe(
        strMeal = strMeal,
        strMealThumb = strMealThumb ?: "",
        idMeal = idMeal
    )

fun NetworkRecipe.asExternalModel() : Recipe =
    Recipe(
        idMeal = idMeal,
        strMeal = strMeal,
        strMealThumb = strMealThumb ?: "",
        strMealAlternate = strMealAlternate,
        strCategory = strCategory,
        strArea = strArea,
        strInstructions = strInstructions,
        strTags = strTags,
        strYoutube = strYoutube,
        strIngredient1 = strIngredient1,
        strIngredient2 = strIngredient2,
        strIngredient3 = strIngredient3,
        strIngredient4 = strIngredient4,
        strIngredient5 = strIngredient5,
        strIngredient6 = strIngredient6,
        strIngredient7 = strIngredient7,
        strIngredient8 = strIngredient8,
        strIngredient9 = strIngredient9,
        strIngredient10 = strIngredient10,
        strIngredient11 = strIngredient11,
        strIngredient12 = strIngredient12,
        strIngredient13 = strIngredient13,
        strIngredient14 = strIngredient14,
        strIngredient15 = strIngredient15,
        strIngredient16 = strIngredient16,
        strIngredient17 = strIngredient17,
        strIngredient18 = strIngredient18,
        strIngredient19 = strIngredient19,
        strIngredient20 = strIngredient20,
        strMeasure1 = strMeasure1,
        strMeasure2 = strMeasure2,
        strMeasure3 = strMeasure3,
        strMeasure4 = strMeasure4,
        strMeasure5 = strMeasure5,
        strMeasure6 = strMeasure6,
        strMeasure7 = strMeasure7,
        strMeasure8 = strMeasure8,
        strMeasure9 = strMeasure9,
        strMeasure10 = strMeasure10,
        strMeasure11 = strMeasure11,
        strMeasure12 = strMeasure12,
        strMeasure13 = strMeasure13,
        strMeasure14 = strMeasure14,
        strMeasure15 = strMeasure15,
        strMeasure16 = strMeasure16,
        strMeasure17 = strMeasure17,
        strMeasure18 = strMeasure18,
        strMeasure19 = strMeasure19,
        strMeasure20 = strMeasure20,
        strSource = strSource,
        strImageSource = strImageSource,
        strCreativeCommonsConfirmed = strCreativeCommonsConfirmed,
        dateModified = dateModified
    )


