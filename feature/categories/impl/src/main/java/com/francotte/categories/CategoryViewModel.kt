package com.francotte.categories

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.common.utils.onFailure
import com.francotte.common.utils.onSuccess
import com.francotte.common.utils.userMessage
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.data.interfaces.UserHomeRepository
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

@HiltViewModel(assistedFactory = CategoryViewModel.Factory::class)
class CategoryViewModel @AssistedInject constructor(
    @Assisted val category: String,
    private val repository: UserHomeRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState(title = category))
    val state = _state.asStateFlow()

    private val _events = Channel<CategoryEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadData()
        observeFavorites()
    }

    fun onAction(action: CategoryAction) {
        when (action) {
            CategoryAction.OnReload -> refresh()
            CategoryAction.OnBackClick -> viewModelScope.launch {
                _events.send(CategoryEvent.NavigateBack)
            }
            is CategoryAction.OnRecipeClick -> {
                val recipes = state.value.recipes
                viewModelScope.launch {
                    _events.send(
                        CategoryEvent.NavigateToRecipe(
                            ids = recipes.map { it.recipe.idMeal },
                            index = action.index,
                            title = recipes[action.index].recipe.strMeal,
                        )
                    )
                }
            }
            // Favorite toggling is handled outside the VM (FavoriteManager decoupling);
            // intercepted by CategoryRoute before reaching onAction.
            is CategoryAction.OnToggleFavorite -> Unit
        }
    }

    /**
     * Initial load on screen entry: one-shot network fetch (no DB). On failure we
     * record `error` and, since `recipes` stays empty, the screen shows FullErrorScreen.
     */
    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getRecipesByCategory(category)
                .onSuccess { recipes -> _state.update { it.copy(recipes = recipes, isLoading = false, error = null) } }
                .onFailure { appError -> _state.update { it.copy(isLoading = false, error = appError.userMessage()) } }
        }
    }

    /**
     * Recipe data is fetched one-shot (network-only), so favorite state would be frozen at
     * fetch time. We observe `userData` and recompute each recipe's favoriteState so the
     * heart icon reacts to toggles — without re-fetching or going back to DB caching.
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

    /**
     * User-triggered pull-to-refresh (or retry): drives `isRefreshing`. On failure we
     * keep the current list and notify via a snackbar instead of wiping the screen.
     */
    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            repository.getRecipesByCategory(category)
                .onSuccess { recipes -> _state.update { it.copy(recipes = recipes, error = null) } }
                .onFailure { appError -> _events.send(CategoryEvent.ShowSnackbar(appError.userMessage())) }
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(category: String): CategoryViewModel
    }
}

@Immutable
data class CategoryState(
    val title: String = "",
    val recipes: List<LikeableRecipe> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@Immutable
sealed interface CategoryAction {
    data object OnReload : CategoryAction
    data class OnRecipeClick(val index: Int) : CategoryAction
    data class OnToggleFavorite(val recipe: LikeableRecipe) : CategoryAction
    data object OnBackClick : CategoryAction
}

@Immutable
sealed interface CategoryEvent {
    data class NavigateToRecipe(val ids: List<String>, val index: Int, val title: String) : CategoryEvent
    data object NavigateBack : CategoryEvent
    data class ShowSnackbar(val message: String) : CategoryEvent
}
