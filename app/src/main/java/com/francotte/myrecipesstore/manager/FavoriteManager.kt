package com.francotte.myrecipesstore.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.graphics.scale
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.network.api.FavoriteApi
import com.francotte.myrecipesstore.network.model.CustomRecipe
import com.francotte.myrecipesstore.network.model.Ingredient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FavoriteManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: FavoriteApi,
    authManager: AuthManager,
    private val foodPreferencesDataSource: UserDataSource
) {

    private val credentials: StateFlow<UserCredentials?> = authManager.credentials

    val goToLoginScreenEvent = MutableSharedFlow<Unit>()

    val snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val customRecipehasBeenUpdatedSuccessfully = MutableStateFlow(false)

    suspend fun initFavorites() {
        try {
            val result = api.getFavoriteRecipeIds("Bearer ${credentials.value?.token}")
            foodPreferencesDataSource.setFavoritesIds(result.toSet())
        } catch (e: Exception) {
            Log.d("debug_google_error_fav", e.message.toString())
        }
    }

    suspend fun toggleRecipeFavorite(
        likeableRecipe: LikeableRecipe
    ) {
        val cred = credentials.firstOrNull()
        if (cred == null) {
            goToLoginScreenEvent.emit(Unit)
        } else {
            val recipeId = likeableRecipe.recipe.idMeal
            val isFavorite = api.getRecipeFavoriteStatus(
                recipeId,
                "Bearer ${cred.token}"
            )
            val willBeFavorite = !isFavorite
            withContext(NonCancellable) {
                if (willBeFavorite) {
                    api.addFavorite(
                        recipeId,
                        "Bearer ${cred.token}"
                    )
                    foodPreferencesDataSource.setFavoriteId(recipeId, true)
                    snackBarMessage.tryEmit("Recipe added to favorites")
                } else {
                    api.removeFavorite(
                        recipeId,
                        "Bearer ${cred.token}"
                    )
                    foodPreferencesDataSource.setFavoriteId(recipeId, false)
                    snackBarMessage.tryEmit("Recipe deleted from favorites")
                }
            }
        }
    }

    suspend fun createRecipe(
        title: String,
        ingredients: List<Ingredient>,
        instructions: String,
        image: Uri?
    ) {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val instructionsPart = instructions.toRequestBody("text/plain".toMediaTypeOrNull())
        val ingredientsJson = Json.encodeToString(ingredients)
        val ingredientsBody = ingredientsJson.toRequestBody("text/plain".toMediaType())
        val imagePart = image.toMultiPartBody(context)
        api.addRecipe(
            "Bearer ${credentials.value?.token}",
            imagePart,
            titlePart,
            instructionsPart,
            ingredientsBody
        )
        withContext(Dispatchers.Main) {
            snackBarMessage.emit("Your recipe has been created successfully !")
        }
    }


    suspend fun updateRecipe(
        recipeId: String,
        title: String,
        ingredients: List<Ingredient>,
        instructions: String,
        image: Uri?
    ) {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val instructionsPart = instructions.toRequestBody("text/plain".toMediaTypeOrNull())
        val ingredientsJson = Json.encodeToString(ingredients)
        val ingredientsBody = ingredientsJson.toRequestBody("text/plain".toMediaType())
        val imagePart = image.toMultiPartBody(context)
        val response = api.updateRecipe(
            "Bearer ${credentials.value?.token}",
            recipeId,
            imagePart,
            titlePart,
            instructionsPart,
            ingredientsBody
        )
        if (response.isSuccessful) {
            withContext(Dispatchers.Main) {
                customRecipehasBeenUpdatedSuccessfully.value = true
                snackBarMessage.emit("Your recipe has been updated successfully !")
            }
        } else {
            withContext(Dispatchers.Main) {
                snackBarMessage.emit("An error occurred!")
            }
        }

    }


    suspend fun getUserRecipes(): List<CustomRecipe> {
        return if (credentials.value?.token != null) {
            Log.d("debug_user_recipes", api.getUserRecipes("Bearer ${credentials.value?.token}").toString())
            api.getUserRecipes("Bearer ${credentials.value?.token}")
        } else {
            emptyList()
        }
    }

    suspend fun getUserCustomRecipe(customRecipeId:String): CustomRecipe? {
        return if (credentials.value?.token != null) {
            api.getUserRecipe("Bearer ${credentials.value?.token}", customRecipeId)
        } else {
            null
        }
    }

}

fun Uri?.toMultiPartBody(context: Context): MultipartBody.Part? {
    val imagePart = this?.let { uri ->
        val resolver = context.contentResolver

        val inputStream = resolver.openInputStream(uri)
        Log.d("debug_upload", "inputStream is null: ${inputStream == null}")
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val resizedBitmap = originalBitmap?.let {
            val maxSize = 800
            val width = it.width
            val height = it.height
            val scale = minOf(maxSize / width.toFloat(), maxSize / height.toFloat(), 1f)
            it.scale((width * scale).toInt(), (height * scale).toInt())
        }

        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(file)
        resizedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Qualité 80%
        outputStream.flush()
        outputStream.close()

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("image", file.name, requestFile)
    }
    return imagePart
}




