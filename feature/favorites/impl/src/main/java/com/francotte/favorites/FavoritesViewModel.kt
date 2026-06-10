package com.francotte.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.model.CustomRecipe
import com.francotte.model.LikeableRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val searchTextFlow = MutableStateFlow("")
    private val isReloadingFlow = MutableStateFlow(false)

    private val content: Flow<FavoriteUiState> =
        combine(
            favoritesRepository.observeFavoritesRecipes(),
            favoritesRepository.observeUserCustomRecipes(),
            searchTextFlow,
        ) { favorites, customRecipes, searchText ->
            val favoriteRecipes = favorites.getOrDefault(emptyList())
            FavoriteUiState.Success(
                favoritesRecipes = if (searchText.isBlank()) {
                    favoriteRecipes
                } else {
                    favoriteRecipes.filter { it.recipe.strMeal.lowercase().contains(searchText.lowercase()) }
                },
                customRecipes = customRecipes.getOrDefault(emptyList()),
            )
        }.debounce(500)

    val state: StateFlow<FavoritesState> =
        combine(content, searchTextFlow, isReloadingFlow) { content, searchText, isReloading ->
            FavoritesState(searchText = searchText, isReloading = isReloading, content = content)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesState())

    private val _events = Channel<FavoritesEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: FavoritesAction) {
        when (action) {
            is FavoritesAction.OnSearchChange -> searchTextFlow.value = action.text
            FavoritesAction.OnReload -> reload()
            is FavoritesAction.OnRecipeClick -> {
                val recipes = (state.value.content as? FavoriteUiState.Success)?.favoritesRecipes ?: return
                val clicked = recipes.getOrNull(action.index) ?: return
                viewModelScope.launch {
                    _events.send(
                        FavoritesEvent.NavigateToRecipe(
                            ids = recipes.map { it.recipe.idMeal },
                            index = action.index,
                            title = clicked.recipe.strMeal,
                        )
                    )
                }
            }
            is FavoritesAction.OnCustomRecipeClick -> viewModelScope.launch {
                _events.send(FavoritesEvent.NavigateToCustomRecipe(action.recipeId))
            }
            // Favorite toggling is handled outside the VM (FavoriteManager decoupling).
            is FavoritesAction.OnToggleFavorite -> Unit
        }
    }

    private fun reload() {
        viewModelScope.launch {
            isReloadingFlow.value = true
            searchTextFlow.value = ""
            favoritesRepository.refreshFavoritesRecipes()
            isReloadingFlow.value = false
        }
    }
}

data class FavoritesState(
    val searchText: String = "",
    val isReloading: Boolean = false,
    val content: FavoriteUiState = FavoriteUiState.Loading,
)

sealed interface FavoriteUiState {
    data class Success(
        val favoritesRecipes: List<LikeableRecipe>,
        val customRecipes: List<CustomRecipe>,
    ) : FavoriteUiState

    data object Error : FavoriteUiState

    data object Loading : FavoriteUiState
}

sealed interface FavoritesAction {
    data class OnSearchChange(val text: String) : FavoritesAction
    data object OnReload : FavoritesAction
    data class OnRecipeClick(val index: Int) : FavoritesAction
    data class OnCustomRecipeClick(val recipeId: String) : FavoritesAction
    data class OnToggleFavorite(val recipe: LikeableRecipe) : FavoritesAction
}

sealed interface FavoritesEvent {
    data class NavigateToRecipe(val ids: List<String>, val index: Int, val title: String) : FavoritesEvent
    data class NavigateToCustomRecipe(val recipeId: String) : FavoritesEvent
}
