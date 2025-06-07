package com.francotte.myrecipesstore.favorites

import android.util.Log
import com.francotte.myrecipesstore.auth.AuthManager
import com.francotte.myrecipesstore.auth.UserCredentials
import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.network.api.FavoriteApi
import com.francotte.myrecipesstore.user.FoodPreferencesDataSource
import com.francotte.myrecipesstore.user.UserDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FavoriteManager @Inject constructor(private val api: FavoriteApi, authManager: AuthManager, private val foodPreferencesDataSource: UserDataSource) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val credentials: StateFlow<UserCredentials?> = authManager.credentials

    val goToLoginScreenEvent = MutableSharedFlow<Unit>()

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
                    } else {
                        api.removeFavorite(
                            recipeId,
                            "Bearer ${cred.token}"
                        )
                        foodPreferencesDataSource.setFavoriteId(recipeId, false)
                    }
                }
            }
        }
    }

}




