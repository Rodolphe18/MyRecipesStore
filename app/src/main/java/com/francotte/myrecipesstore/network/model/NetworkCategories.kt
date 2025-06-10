package com.francotte.myrecipesstore.network.model

import com.francotte.myrecipesstore.database.model.CategoryEntity
import com.francotte.myrecipesstore.database.model.LightRecipeEntity
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class NetworkCategories(val categories:List<NetworkAbstractCategory>)

class PolymorphicCategorySerializer: JsonContentPolymorphicSerializer<NetworkAbstractCategory>(
    NetworkAbstractCategory::class) {

    private val fullCategoryFields = setOf("strCategoryThumb", "strCategoryDescription")

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out NetworkAbstractCategory> {
        val keys = element.jsonObject.keys
        return if (fullCategoryFields.any { it in keys }) {
            NetworkCategory.serializer()
        } else {
            NetworkLightCategory.serializer()
        }
    }

}

@Serializable(with = PolymorphicCategorySerializer::class)
sealed class NetworkAbstractCategory {
   abstract val strCategory: String
}

@Serializable
data class NetworkLightCategory(
    override val strCategory: String
) : NetworkAbstractCategory()

@Serializable
data class NetworkCategory(
    val idCategory: String,
    override val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
) : NetworkAbstractCategory()

fun NetworkCategory.asEntity(): CategoryEntity =
    CategoryEntity(
        idCategory = idCategory,
        strCategory = strCategory,
        strCategoryThumb = strCategoryThumb,
        strCategoryDescription = strCategoryDescription
    )


