package com.francotte.domain

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.scale
import com.francotte.datastore.UserDataRepository
import com.francotte.model.LikeableRecipe
import com.francotte.network.api.FavoriteApi
import com.francotte.network.model.NetworkCustomIngredient
import com.francotte.network.model.NetworkCustomRecipe
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
import okio.IOException
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleCoroutineScope
import com.francotte.ui.FavoritesSyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val SHORTCUT_ID_FAVORITES = "shortcut_favorites"

@Singleton
class FavoriteManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: FavoriteApi,
    authManager: AuthManager,
    private val foodPreferencesDataSource: UserDataRepository
) {


    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val credentials: StateFlow<UserCredentials?> = authManager.credentials

    val isAuthenticated = authManager.isAuthenticated

    val goToLoginScreenEvent = MutableSharedFlow<Unit>()

    val snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val customRecipeHasBeenUpdatedSuccessfully = MutableStateFlow(false)

    init {
        coroutineScope.launch {
            isAuthenticated.collect { isAuthenticated ->
                setupFavoritesShortcut(context, isAuthenticated)
            }
        }
    }

    suspend fun initFavorites() {
        try {
            val result = api.getFavoriteRecipeIds("Bearer ${credentials.value?.token}")
            foodPreferencesDataSource.setFavoritesIds(result.toSet())
        } catch (e: Exception) {
            Log.d("debug_google_error_fav", e.message.toString())
        }
    }


    fun setupFavoritesShortcut(context: Context, enable: Boolean) {
        if (enable) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "myapp://favorites".toUri()
                putExtra("is_shortcut", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID_FAVORITES)
                .setIcon(
                    IconCompat.createWithResource(
                        context,
                        R.drawable.ic_favorite
                    )
                )
                .setShortLabel("favorites")
                .setLongLabel("favorites")
                .setIntent(intent)
                .build()

            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        } else {
            ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(SHORTCUT_ID_FAVORITES))
        }
    }


    suspend fun toggleRecipeFavorite(likeableRecipe: LikeableRecipe) {
        val cred = credentials.firstOrNull()
        val token = cred?.token
        if (token.isNullOrEmpty()) {
            goToLoginScreenEvent.emit(Unit)
            return
        }
        val recipeId = likeableRecipe.recipe.idMeal
        val currentlyFavorite = foodPreferencesDataSource.userData.first().favoriteRecipesIds.contains(recipeId)
        val desiredFavorite = !currentlyFavorite
        foodPreferencesDataSource.setFavoriteId(recipeId, desiredFavorite)
        foodPreferencesDataSource.upsertPendingFavorite(recipeId, desiredFavorite)
        snackBarMessage.tryEmit(
            if (desiredFavorite) "Recipe added to favorites"
            else "Recipe removed from favorites"
        )
        FavoritesSyncScheduler.enqueue(context)
    }

    suspend fun createRecipe(
        title: String,
        ingredients: List<NetworkCustomIngredient>,
        instructions: String,
        image: Uri?
    ) {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val instructionsPart = instructions.toRequestBody("text/plain".toMediaTypeOrNull())
        val ingredientsJson = Json.encodeToString(ingredients)
        val ingredientsBody = ingredientsJson.toRequestBody("text/plain".toMediaType())
        val imagePart = image.toMultiPartBody(context)
        try {
            val response = withContext(Dispatchers.IO) {
                api.addRecipe(
                    "Bearer ${credentials.value?.token}",
                    imagePart,
                    titlePart,
                    instructionsPart,
                    ingredientsBody
                )
            }

            if (response.isSuccessful) {
                snackBarMessage.emit("Your recipe has been created successfully !")
            } else {
                snackBarMessage.emit("An error occurred!")
            }
        } catch (e: IOException) {
            snackBarMessage.emit("Network error. Please check your connection.")
        } catch (e: HttpException) {
            snackBarMessage.emit("Server error. Please try again later.")
        } catch (e: Exception) {
            snackBarMessage.emit("Unexpected error. Please try again.")
        }
    }


    suspend fun updateRecipe(
        recipeId: String,
        title: String,
        ingredients: List<NetworkCustomIngredient>,
        instructions: String,
        image: Uri?
    ) {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val instructionsPart = instructions.toRequestBody("text/plain".toMediaTypeOrNull())
        val ingredientsJson = Json.encodeToString(ingredients)
        val ingredientsBody = ingredientsJson.toRequestBody("text/plain".toMediaType())
        val imagePart = image.toMultiPartBody(context)
        try {
            val response = withContext(Dispatchers.IO) {
                api.updateRecipe(
                    "Bearer ${credentials.value?.token}",
                    recipeId,
                    imagePart,
                    titlePart,
                    instructionsPart,
                    ingredientsBody
                )
            }
            if (response.isSuccessful) {
                customRecipeHasBeenUpdatedSuccessfully.value = true
                snackBarMessage.tryEmit("Your recipe has been updated successfully !")
            } else {
                snackBarMessage.tryEmit("An error occurred!")
            }
        } catch (e: IOException) {
            snackBarMessage.emit("Network error. Please check your connection.")
        } catch (e: HttpException) {
            snackBarMessage.emit("Server error. Please try again later.")
        } catch (e: Exception) {
            snackBarMessage.emit("Unexpected error. Please try again.")
        }
    }


    suspend fun getUserRecipes(): List<NetworkCustomRecipe> {
        return if (credentials.value?.token != null) {
            api.getUserRecipes("Bearer ${credentials.value?.token}")
        } else {
            emptyList()
        }
    }

    suspend fun getUserCustomRecipe(customRecipeId: String): NetworkCustomRecipe {
        return if (credentials.value?.token != null) {
            api.getUserRecipe("Bearer ${credentials.value?.token}", customRecipeId)
        } else {
            throw Exception("ss")
        }
    }

    companion object {
        private const val SHORTCUT_ID = "favorites"
    }

}

fun Uri?.toMultiPartBody(context: Context): MultipartBody.Part? {
    val imagePart = this?.let { uri ->
        val resolver = context.contentResolver

        val inputStream = resolver.openInputStream(uri)
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




