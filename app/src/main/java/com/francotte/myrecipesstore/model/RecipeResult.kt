package com.francotte.myrecipesstore.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

@Serializable(with = MealResultSerializer::class)
sealed class RecipeResult {
    data class Single(val meal: AbstractRecipe) : RecipeResult()
    data class Multiple(val meals: List<AbstractRecipe>) : RecipeResult()
    data object Empty : RecipeResult()
}

object MealResultSerializer : KSerializer<RecipeResult> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MealResult")

    override fun deserialize(decoder: Decoder): RecipeResult {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("Expected JsonDecoder")
        val jsonElement = input.decodeJsonElement()

        val jsonObject = jsonElement.jsonObject
        val mealsElement = jsonObject["meals"]

        return when (mealsElement) {
            null -> RecipeResult.Empty
            is JsonArray -> when (mealsElement.size) {
                0 -> RecipeResult.Empty
                1 -> {
                    val meal = Json.decodeFromJsonElement(AbstractRecipe.serializer(), mealsElement[0])
                    RecipeResult.Single(meal)
                }
                else -> {
                    val meals = mealsElement.map {
                        Json.decodeFromJsonElement(AbstractRecipe.serializer(), it)
                    }
                    RecipeResult.Multiple(meals)
                }
            }

            else -> throw SerializationException("Invalid 'meals' format")
        }
    }

    override fun serialize(encoder: Encoder, value: RecipeResult) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("Expected JsonEncoder")

        val mealsJson = when (value) {
            is RecipeResult.Single -> JsonArray(listOf(Json.encodeToJsonElement(AbstractRecipe.serializer(), value.meal)))
            is RecipeResult.Multiple -> JsonArray(value.meals.map { Json.encodeToJsonElement(AbstractRecipe.serializer(), it) })
            is RecipeResult.Empty -> JsonArray(emptyList())
        }

        val obj = buildJsonObject {
            put("meals", mealsJson)
        }

        output.encodeJsonElement(obj)
    }
}

fun RecipeResult.toMealList(): List<AbstractRecipe> = when (this) {
    is RecipeResult.Single -> listOf(meal)
    is RecipeResult.Multiple -> meals
    is RecipeResult.Empty -> emptyList()
}
