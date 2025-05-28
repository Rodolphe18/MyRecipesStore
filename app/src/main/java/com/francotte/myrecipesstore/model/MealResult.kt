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
sealed class MealResult {
    data class Single(val meal: AbstractMeal) : MealResult()
    data class Multiple(val meals: List<AbstractMeal>) : MealResult()
    data object Empty : MealResult()
}

object MealResultSerializer : KSerializer<MealResult> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MealResult")

    override fun deserialize(decoder: Decoder): MealResult {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("Expected JsonDecoder")
        val jsonElement = input.decodeJsonElement()

        val jsonObject = jsonElement.jsonObject
        val mealsElement = jsonObject["meals"]

        return when (mealsElement) {
            null -> MealResult.Empty
            is JsonArray -> when (mealsElement.size) {
                0 -> MealResult.Empty
                1 -> {
                    val meal = Json.decodeFromJsonElement(AbstractMeal.serializer(), mealsElement[0])
                    MealResult.Single(meal)
                }
                else -> {
                    val meals = mealsElement.map {
                        Json.decodeFromJsonElement(AbstractMeal.serializer(), it)
                    }
                    MealResult.Multiple(meals)
                }
            }

            else -> throw SerializationException("Invalid 'meals' format")
        }
    }

    override fun serialize(encoder: Encoder, value: MealResult) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("Expected JsonEncoder")

        val mealsJson = when (value) {
            is MealResult.Single -> JsonArray(listOf(Json.encodeToJsonElement(AbstractMeal.serializer(), value.meal)))
            is MealResult.Multiple -> JsonArray(value.meals.map { Json.encodeToJsonElement(AbstractMeal.serializer(), it) })
            is MealResult.Empty -> JsonArray(emptyList())
        }

        val obj = buildJsonObject {
            put("meals", mealsJson)
        }

        output.encodeJsonElement(obj)
    }
}

fun MealResult.toMealList(): List<AbstractMeal> = when (this) {
    is MealResult.Single -> listOf(meal)
    is MealResult.Multiple -> meals
    is MealResult.Empty -> emptyList()
}
