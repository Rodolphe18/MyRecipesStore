package com.francotte.myrecipesstore.manager

import android.content.Context
import android.net.Uri
import com.francotte.myrecipesstore.network.api.FavoriteApi
import com.francotte.myrecipesstore.datastore.UserDataSource
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class FavoriteManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: FavoriteApi,
    authManager: AuthManager,
    private val foodPreferencesDataSource: UserDataSource
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val credentials: StateFlow<UserCredentials?> = authManager.credentials

    val goToLoginScreenEvent = MutableSharedFlow<Unit>()

    val snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)

    fun initFavorites() {
        coroutineScope.launch {
            try {
                val result = api.getFavoriteRecipeIds("Bearer ${credentials.value?.token}")
                foodPreferencesDataSource.setFavoritesIds(result.toSet())
            } catch (e: Exception) {
                TODO()
            }
        }
    }

    suspend fun toggleRecipeFavorite(
        likeableRecipe: LikeableRecipe
    ) {
        credentials.collectLatest { cred ->
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
                        snackbarMessage.tryEmit("Recette ajoutée aux favoris")
                    } else {
                        api.removeFavorite(
                            recipeId,
                            "Bearer ${cred.token}"
                        )
                        foodPreferencesDataSource.setFavoriteId(recipeId, false)
                        snackbarMessage.tryEmit("Recette retirée des favoris")
                    }
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

   fun getUserRecipes() = flow {
      if (credentials.value?.token != null) {
          emit(api.getUserRecipes("Bearer ${credentials.value?.token}"))
      } else {
          emit(null)
      }
   }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), emptyList())

}




