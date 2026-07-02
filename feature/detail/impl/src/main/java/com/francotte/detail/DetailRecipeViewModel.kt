package com.francotte.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.UserFullRecipeRepository
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailRecipeViewModel.Factory::class)
class DetailRecipeViewModel @AssistedInject constructor(
    private val detailRecipeRepository: UserFullRecipeRepository,
    @Assisted val ids: List<String>?,
    @Assisted val index: Int?,
    @Assisted val recipeTitle: String?,
) : ViewModel() {

    private val longIds = ids?.map { it.toLong() } ?: emptyList()

    private val _state = MutableStateFlow(
        DetailState(
            title = recipeTitle ?: "",
            pageCount = longIds.size,
            initialPage = index ?: 0,
            selectedIndex = index ?: 0,
        )
    )
    val state = _state.asStateFlow()

    private var currentPage = index ?: 0

    /** Pages with an active observer — avoids re-subscribing the same id twice. */
    private val observedPages = mutableSetOf<Int>()

    init {
        // Eager-load : on souscrit à toutes les recettes du lot pour alimenter le volet liste.
        longIds.indices.forEach { loadPage(it) }
    }

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.OnPageChanged -> selectRecipe(action.page)
            is DetailAction.OnRecipeSelected -> selectRecipe(action.index)
            // Favorite toggling is handled outside the VM (FavoriteManager decoupling).
            else -> Unit
        }
    }

    private fun selectRecipe(page: Int) {
        currentPage = page
        _state.update { state ->
            state.copy(
                selectedIndex = page,
                title = state.recipes[page]?.recipe?.strMeal ?: state.title,
            )
        }
    }

    private fun loadPage(page: Int) {
        if (page in observedPages || page !in longIds.indices) return
        observedPages += page
        viewModelScope.launch {
            detailRecipeRepository.observeFullRecipe(longIds[page]).collectLatest { result ->
                val recipe = result.getOrNull() ?: return@collectLatest
                _state.update { state ->
                    state.copy(
                        recipes = state.recipes + (page to recipe),
                        title = if (page == currentPage) recipe.recipe.strMeal else state.title,
                    )
                }
            }
        }
    }

    /** Deep-link entry: loads a single recipe by id. */
    fun loadDeeplink(id: String) {
        viewModelScope.launch {
            detailRecipeRepository.observeFullRecipe(id.toLong()).collectLatest { result ->
                val recipe = result.getOrNull() ?: return@collectLatest
                _state.update { it.copy(deeplinkRecipe = recipe, title = recipe.recipe.strMeal) }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(ids: List<String>?, index: Int?, recipeTitle: String?): DetailRecipeViewModel
    }
}

@Immutable
data class DetailState(
    val title: String = "",
    val pageCount: Int = 0,
    val initialPage: Int = 0,
    val selectedIndex: Int = 0,
    val recipes: Map<Int, LikeableRecipe> = emptyMap(),
    val deeplinkRecipe: LikeableRecipe? = null,
)

@Immutable
sealed interface DetailAction {
    data class OnPageChanged(val page: Int) : DetailAction
    data class OnRecipeSelected(val index: Int) : DetailAction
    data class OnToggleFavorite(val recipe: LikeableRecipe) : DetailAction
    data object OnBackClick : DetailAction
}

