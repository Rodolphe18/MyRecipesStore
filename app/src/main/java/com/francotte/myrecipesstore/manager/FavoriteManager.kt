package com.francotte.myrecipesstore.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import com.francotte.myrecipesstore.network.api.FavoriteApi
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.network.model.CustomRecipe
import com.francotte.myrecipesstore.network.model.Ingredient
import com.francotte.myrecipesstore.network.model.serializeIngredients
import com.francotte.myrecipesstore.network.model.toPart
import com.francotte.myrecipesstore.network.model.uriToMultipart
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        images: List<Uri>
    ) {
        val titlePart = toPart(title)
        val instructionsPart = toPart(instructions)
        val ingredientsPart = serializeIngredients(ingredients)
        val imageParts = images.mapNotNull { uri ->
            uriToMultipart(uri, context, "images")
        }

        //  api.addRecipe("Bearer ${credentials.value?.token}",imageParts, titlePart, instructionsPart, ingredientsPart)
    }

    suspend fun getUserRecipes(): List<CustomRecipe> {
        return if (credentials.value?.token != null) {
            api.getUserRecipes("Bearer ${credentials.value?.token}")
        } else {
            emptyList()
        }
    }

}




