package com.francotte.search.result_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.common.utils.DataResult
import com.francotte.common.utils.onFailure
import com.francotte.common.utils.onSuccess
import com.francotte.data.interfaces.IngredientsAndAreasRepository
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.feature.search.api.SearchMode
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SearchRecipesViewModel.Factory::class)
class SearchRecipesViewModel @AssistedInject constructor(
    private val ingredientsAndAreasRepository: IngredientsAndAreasRepository,
    private val userHomeRepository: UserHomeRepository,
    private val userDataRepository: UserDataRepository,
    @Assisted val mode: SearchMode,
    @Assisted val item: String,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchRecipesState(title = item))
    val state = _state.asStateFlow()

    private val _events = Channel<SearchRecipesEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadRecipes()
        observeFavorites()
    }

    fun onAction(action: SearchRecipesAction) {
        when (action) {
            SearchRecipesAction.OnReload -> loadRecipes()
            SearchRecipesAction.OnBackClick -> viewModelScope.launch {
                _events.send(SearchRecipesEvent.NavigateBack)
            }
            is SearchRecipesAction.OnRecipeClick -> {
                val recipes = state.value.recipes
                val clicked = recipes.getOrNull(action.index) ?: return
                viewModelScope.launch {
                    _events.send(
                        SearchRecipesEvent.NavigateToRecipe(
                            ids = recipes.map { it.recipe.idMeal },
                            index = action.index,
                            title = clicked.recipe.strMeal,
                        )
                    )
                }
            }
            // Favorite toggling is handled outside the VM (FavoriteManager decoupling).
            is SearchRecipesAction.OnToggleFavorite -> Unit
        }
    }

    /**
     * One-shot network fetch (no DB). `recipes` and the loading/error flags come from the same
     * result, so there is no empty -> error flash.
     */
    private fun loadRecipes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isError = false) }
            fetchRecipes()
                .onSuccess { recipes ->
                    _state.update {
                        it.copy(
                            recipes = recipes,
                            isLoading = false,
                            // Categories treat "empty" as an error; the other modes show an empty screen.
                            isError = recipes.isEmpty() && mode == SearchMode.CATEGORIES,
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, isError = true) }
                }
        }
    }

    private suspend fun fetchRecipes(): DataResult<List<LikeableRecipe>> = when (mode) {
        SearchMode.COUNTRY -> ingredientsAndAreasRepository.getRecipesByArea(item)
        SearchMode.INGREDIENTS -> ingredientsAndAreasRepository.getRecipesByIngredients(listOf(item))
        SearchMode.CATEGORIES -> userHomeRepository.getRecipesByCategory(item)
    }

    /**
     * Network-only data freezes favorite state at fetch time. Observe `userData` and recompute
     * each recipe's favoriteState so the heart icon reacts to toggles, without re-fetching.
     */
    private fun observeFavorites() {
        userDataRepository.userData
            .onEach { userData ->
                _state.update { state ->
                    state.copy(recipes = state.recipes.map { LikeableRecipe(it.recipe, userData) })
                }
            }
            .launchIn(viewModelScope)
    }

    @AssistedFactory
    interface Factory {
        fun create(item: String, mode: SearchMode): SearchRecipesViewModel
    }
}

data class SearchRecipesState(
    val title: String = "",
    val recipes: List<LikeableRecipe> = emptyList(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
)

sealed interface SearchRecipesAction {
    data object OnReload : SearchRecipesAction
    data object OnBackClick : SearchRecipesAction
    data class OnRecipeClick(val index: Int) : SearchRecipesAction
    data class OnToggleFavorite(val recipe: LikeableRecipe) : SearchRecipesAction
}

sealed interface SearchRecipesEvent {
    data class NavigateToRecipe(val ids: List<String>, val index: Int, val title: String) : SearchRecipesEvent
    data object NavigateBack : SearchRecipesEvent
}
