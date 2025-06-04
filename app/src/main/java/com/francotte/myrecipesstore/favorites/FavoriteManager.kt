package com.francotte.myrecipesstore.favorites

import android.util.Log
import com.francotte.myrecipesstore.auth.AuthManager
import com.francotte.myrecipesstore.auth.UserCredentials
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.RecipeResult
import com.francotte.myrecipesstore.network.api.FavoriteApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FavoriteManager @Inject constructor(
    private val api: FavoriteApi,
    authManager: AuthManager
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val canAccessFavorites: StateFlow<Boolean> = authManager.isAuthenticated

    private val credentials: StateFlow<UserCredentials?> = authManager.credentials

    val goToLoginScreenEvent = MutableSharedFlow<Unit>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val favoritesRecipes: StateFlow<RecipeResult> = combine(
        credentials,
        canAccessFavorites
    ) { creds, canAccess ->
        if (creds != null && canAccess) {
            creds
        } else {
            null
        }
    }.flatMapLatest { creds ->
        if (creds != null) {
            flow<RecipeResult> {
                try {
                    val result = api.getFavoriteLightRecipes(creds.id, creds.token, 1)
                    emit(result)
                } catch (e: Exception) {
                    emit(RecipeResult.Empty)
                }
            }
        } else {
            flowOf(RecipeResult.Empty)
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeResult.Empty
    )


    suspend fun onToggleFavoriteSuccess() = Unit


    suspend fun toggleRecipeFavorite(
        recipe: AbstractRecipe
    ) {
        credentials.collectLatest { cred ->
            if (cred == null) {
                Log.d("debug_login1", "ssss")
                goToLoginScreenEvent.emit(Unit)
            } else {
                Log.d("debug_login2", "ssss")
                val isFavorite = api.getRecipeFavoriteStatus(
                    cred.id,
                    recipe.idMeal,
                    cred.token
                )
                val willBeFavorite = !isFavorite
                withContext(NonCancellable) {
                    if (willBeFavorite) {
                        api.addFavorite(
                            cred.id,
                            recipe.idMeal,
                            cred.token
                        )
                    } else {
                        api.removeFavorite(
                            cred.id,
                            recipe.idMeal,
                            cred.token
                        )
                    }
                    onToggleFavoriteSuccess()
                }

            }
        }
    }

}




