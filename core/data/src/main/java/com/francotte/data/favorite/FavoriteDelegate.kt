package com.francotte.data.favorite

import com.francotte.data.interfaces.FavoriteHelper
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Side effects a favorite toggle can produce, surfaced to the feature so it can bridge them
 * into its own event stream (snackbar / navigation).
 */
sealed interface FavoriteEvent {
    data class ShowMessage(val message: String) : FavoriteEvent
    data object NavigateToLogin : FavoriteEvent
}

/**
 * Encapsulates favorite toggling for feature ViewModels: performs the toggle via [FavoriteHelper]
 * and maps the [ToggleFavoriteResult] to a [FavoriteEvent] once, so each feature only has to bridge
 * [favoriteEvents] into its own event channel instead of duplicating the result handling.
 *
 * Composed by delegation in a ViewModel: `class XViewModel(... favoriteDelegate) : FavoriteDelegate by favoriteDelegate`.
 */
interface FavoriteDelegate {
    val favoriteEvents: Flow<FavoriteEvent>
    fun toggleFavorite(scope: CoroutineScope, recipe: LikeableRecipe)
}

class FavoriteDelegateImpl @Inject constructor(
    private val favoriteHelper: FavoriteHelper,
) : FavoriteDelegate {

    private val _favoriteEvents = Channel<FavoriteEvent>()
    override val favoriteEvents: Flow<FavoriteEvent> = _favoriteEvents.receiveAsFlow()

    override fun toggleFavorite(scope: CoroutineScope, recipe: LikeableRecipe) {
        scope.launch {
            val event = when (val result = favoriteHelper.toggleRecipeFavorite(recipe)) {
                is ToggleFavoriteResult.Success -> FavoriteEvent.ShowMessage(
                    if (result.added) "Recipe added to favorites" else "Recipe removed from favorites"
                )
                ToggleFavoriteResult.Offline -> FavoriteEvent.ShowMessage(
                    "Offline: favorites will sync when back online"
                )
                ToggleFavoriteResult.Unauthenticated -> FavoriteEvent.NavigateToLogin
            }
            _favoriteEvents.send(event)
        }
    }
}
