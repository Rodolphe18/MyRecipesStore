package com.francotte.data.manager

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.francotte.common.extension.ApplicationScope
import com.francotte.data.R
import com.francotte.data.sync.SyncScheduler
import com.francotte.data.util.NetworkMonitor
import com.francotte.datastore.UserDataRepository
import com.francotte.model.LikeableRecipe
import com.francotte.network.api.FavoriteApi
import com.francotte.network.model.NetworkCustomIngredient
import com.francotte.network.model.NetworkCustomRecipe
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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

sealed interface ToggleFavoriteResult {
    data class Success(val added: Boolean) : ToggleFavoriteResult
    data object Offline : ToggleFavoriteResult
    data object Unauthenticated : ToggleFavoriteResult
}

const val SHORTCUT_ID_FAVORITES = "shortcut_favorites"

@Singleton
class FavoriteManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: FavoriteApi,
    private val networkMonitor: NetworkMonitor,
    authManager: AuthManager,
    private val foodPreferencesDataSource: UserDataRepository,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val syncScheduler: SyncScheduler,
) {
    private val credentials: StateFlow<UserCredentials?> = authManager.credentials

    val isAuthenticated = authManager.isAuthenticated

    val customRecipeHasBeenUpdatedSuccessfully = MutableStateFlow(false)

    init {
        coroutineScope.launch {
            isAuthenticated.collect { isAuthenticated ->
                setupFavoritesShortcut(context, isAuthenticated)
            }
        }
    }

    private fun setupFavoritesShortcut(context: Context, enable: Boolean) {
        if (enable) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "myapp://favorites".toUri()
                putExtra("is_shortcut", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID_FAVORITES)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_favorite))
                .setShortLabel("favorites")
                .setLongLabel("favorites")
                .setIntent(intent)
                .build()
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        } else {
            ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(SHORTCUT_ID_FAVORITES))
        }
    }

    suspend fun toggleRecipeFavorite(likeableRecipe: LikeableRecipe): ToggleFavoriteResult {
        val token = credentials.firstOrNull()?.token
        if (token.isNullOrEmpty()) return ToggleFavoriteResult.Unauthenticated

        val recipeId = likeableRecipe.recipe.idMeal
        val currentlyFavorite = foodPreferencesDataSource.userData.first().favoriteRecipesIds.contains(recipeId)
        val desiredFavorite = !currentlyFavorite
        foodPreferencesDataSource.setFavoriteId(recipeId, desiredFavorite)
        foodPreferencesDataSource.upsertPendingFavorite(recipeId, desiredFavorite)
        syncScheduler.enqueueForToggle(context)

        val online = networkMonitor.isOnline.first()
        return if (online) ToggleFavoriteResult.Success(added = desiredFavorite)
        else ToggleFavoriteResult.Offline
    }

    suspend fun createRecipe(
        title: String,
        ingredients: List<NetworkCustomIngredient>,
        instructions: String,
        image: Uri?,
    ): Result<Unit> {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val instructionsPart = instructions.toRequestBody("text/plain".toMediaTypeOrNull())
        val ingredientsJson = Json.encodeToString(ingredients)
        val ingredientsBody = ingredientsJson.toRequestBody("text/plain".toMediaType())
        val imagePart = image.toMultiPartBody(context)
        return try {
            val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                api.addRecipe("Bearer ${credentials.value?.token}", imagePart, titlePart, instructionsPart, ingredientsBody)
            }
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Server error ${response.code()}"))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(
        recipeId: String,
        title: String,
        ingredients: List<NetworkCustomIngredient>,
        instructions: String,
        image: Uri?,
    ): Result<Unit> {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val instructionsPart = instructions.toRequestBody("text/plain".toMediaTypeOrNull())
        val ingredientsJson = Json.encodeToString(ingredients)
        val ingredientsBody = ingredientsJson.toRequestBody("text/plain".toMediaType())
        val imagePart = image.toMultiPartBody(context)
        return try {
            val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                api.updateRecipe("Bearer ${credentials.value?.token}", recipeId, imagePart, titlePart, instructionsPart, ingredientsBody)
            }
            if (response.isSuccessful) {
                customRecipeHasBeenUpdatedSuccessfully.value = true
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server error ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRecipes(): List<NetworkCustomRecipe> =
        if (credentials.value?.token != null)
            api.getUserRecipes("Bearer ${credentials.value?.token}")
        else emptyList()

    suspend fun getUserCustomRecipe(customRecipeId: String): NetworkCustomRecipe =
        if (credentials.value?.token != null)
            api.getUserRecipe("Bearer ${credentials.value?.token}", customRecipeId)
        else throw Exception("Not authenticated")

}

fun Uri?.toMultiPartBody(context: Context): MultipartBody.Part? =
    this?.let { uri ->
        val resolver = context.contentResolver
        val inputStream = resolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        val resizedBitmap = originalBitmap?.let {
            val maxSize = 800
            val scale = minOf(maxSize / it.width.toFloat(), maxSize / it.height.toFloat(), 1f)
            it.scale((it.width * scale).toInt(), (it.height * scale).toInt())
        }
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        FileOutputStream(file).use { out ->
            resizedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("image", file.name, requestFile)
    }
