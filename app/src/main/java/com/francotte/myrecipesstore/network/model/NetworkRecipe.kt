package com.francotte.myrecipesstore.network.model

import com.francotte.myrecipesstore.database.model.FullRecipeEntity
import com.francotte.myrecipesstore.database.model.LightRecipeEntity
import com.francotte.myrecipesstore.datastore.UserData
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
    abstract val strMealThumb: String
    abstract val idMeal: String
}

@Serializable
data class NetworkLightRecipe(
    override val strMeal: String,
    override val strMealThumb: String,
    override val idMeal: String,
) :
    NetworkAbstractRecipe()

@Serializable
data class NetworkRecipe(
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
) : NetworkAbstractRecipe()

@Serializable
data class NetworkRecipeResult(val meals: List<NetworkAbstractRecipe>)

fun NetworkLightRecipe.asEntity(): LightRecipeEntity =
    LightRecipeEntity(
        idMeal = idMeal,
        strMeal = strMeal,
        strMealThumb = strMealThumb
    )

fun NetworkRecipe.asEntity(): FullRecipeEntity =
    FullRecipeEntity(
        idMeal = idMeal,
        strMeal = strMeal,
        strMealThumb = strMealThumb,
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




