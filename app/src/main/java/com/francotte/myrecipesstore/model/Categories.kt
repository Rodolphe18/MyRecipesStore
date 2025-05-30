package com.francotte.myrecipesstore.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class Categories(val data:List<AbstractCategory>)

class PolymorphicCategorySerializer: JsonContentPolymorphicSerializer<AbstractCategory>(AbstractCategory::class) {

    private val fullCategoryFields = setOf("strCategoryThumb", "strCategoryDescription")

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out AbstractCategory> {
        val keys = element.jsonObject.keys
        return if (fullCategoryFields.any { it in keys }) {
            Category.serializer()
        } else {
            LightCategory.serializer()
        }
    }

}

@Serializable(with = PolymorphicCategorySerializer::class)
sealed class AbstractCategory {
   abstract val strCategory: String
}

@Serializable
data class LightCategory(
    override val strCategory: String
) : AbstractCategory()

@Serializable
data class Category(
    val idCategory: String,
    override val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
) : AbstractCategory()