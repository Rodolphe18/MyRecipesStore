package com.francotte.network.model

import android.content.Context
import android.net.Uri
import com.francotte.model.CustomIngredient
import com.francotte.model.CustomRecipe
import com.francotte.model.LightRecipe
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.String

@Serializable
data class NetworkCustomIngredient(
    val name: String,
    val quantity: String,
    val measureType: String
)

@Serializable
data class NetworkCustomRecipe(
    val id: String, val title: String,
    val ingredients: List<NetworkCustomIngredient>,
    val instructions: String,
    val imageUrl: String?
)

fun NetworkCustomRecipe.asExternalModel(): CustomRecipe =
    CustomRecipe(
        id = id,
        title = title,
        ingredients = ingredients.map { it.asExternalModel() },
        instructions = instructions,
        imageUrl = imageUrl
    )

fun NetworkCustomIngredient.asExternalModel(): CustomIngredient = CustomIngredient(name,quantity,measureType)

fun CustomRecipe.asDto(): NetworkCustomRecipe =
    NetworkCustomRecipe(
        id = id,
        title = title,
        ingredients = ingredients.map { it.asDto() },
        instructions = instructions,
        imageUrl = imageUrl
    )

fun CustomIngredient.asDto(): NetworkCustomIngredient = NetworkCustomIngredient(name,quantity,measureType)


fun uriToMultipart(uri: Uri?, context: Context, partName: String = "images"): MultipartBody.Part? {
    val contentResolver = context.contentResolver
    val inputStream = uri?.let { contentResolver.openInputStream(it) ?: return null }
    val fileName = uri?.lastPathSegment ?: "image.jpg"
    val file = File(context.cacheDir, fileName)

    file.outputStream().use { fileOut -> inputStream?.copyTo(fileOut) }

    val mediaType = "image/*".toMediaType()
    val requestBody = file.asRequestBody(mediaType)
    return MultipartBody.Part.createFormData(partName, file.name, requestBody)
}

