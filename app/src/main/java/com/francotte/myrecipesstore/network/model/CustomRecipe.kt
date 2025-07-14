package com.francotte.myrecipesstore.network.model

import android.content.Context
import android.net.Uri
import com.francotte.myrecipesstore.domain.model.LightRecipe
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@Serializable
data class Ingredient(
    val name: String,
    val quantity: String
)

@Serializable
data class CustomRecipe(val id:String, val title: String,
                        val ingredients: List<Ingredient>,
                        val instructions: String,
                        val imageUrl: String?)


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

