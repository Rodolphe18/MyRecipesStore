package com.francotte.search.result_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.CategoriesRepository
import com.francotte.data.repository.IngredientsAndAreasRepository
import com.francotte.data.repository.UserHomeRepository
import com.francotte.feature.search.api.SearchMode
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SearchRecipesViewModel.Factory::class)
class SearchRecipesViewModel @AssistedInject constructor(
    private val ingredientsAndAreasRepository: IngredientsAndAreasRepository,
    private val userHomeRepository: UserHomeRepository,
    @Assisted val mode: SearchMode,
    @Assisted val item: String
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchRecipesUiState =
        when (mode) {
            SearchMode.COUNTRY -> ingredientsAndAreasRepository.observeRecipesByArea(item)
                .map(::mapToSearchRecipesUiState)

            SearchMode.INGREDIENTS -> ingredientsAndAreasRepository.observeRecipesByIngredients(
                listOf(item)
            )
                .map(::mapToSearchRecipesUiState)

            SearchMode.CATEGORIES -> userHomeRepository.observeRecipesByCategory(item)
                .map { recipes ->
                    if (recipes.isNotEmpty()) SearchRecipesUiState.Success(recipes) else
                        SearchRecipesUiState.Error
                }
        }.debounce(300)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SearchRecipesUiState.Loading
            )

    fun refresh() {
        viewModelScope.launch {
            when (mode) {
                SearchMode.COUNTRY -> ingredientsAndAreasRepository.refreshRecipesByArea(item, true)
                SearchMode.INGREDIENTS -> ingredientsAndAreasRepository.refreshRecipesByIngredients(
                    listOf(item),
                    true
                )

                SearchMode.CATEGORIES -> userHomeRepository.refreshRecipesByCategory(item, true)
            }
        }
    }

    init {
        refresh()
    }

    private fun mapToSearchRecipesUiState(result: Result<List<LikeableRecipe>>): SearchRecipesUiState {
        return result.fold(
            onSuccess = { SearchRecipesUiState.Success(it) },
            onFailure = { SearchRecipesUiState.Error })
    }

    @AssistedFactory
    interface Factory {
        fun create(item: String, mode: SearchMode): SearchRecipesViewModel
    }
}

sealed interface SearchRecipesUiState {
    data object Loading : SearchRecipesUiState

    data object Error : SearchRecipesUiState

    data class Success(
        val recipes: List<LikeableRecipe>,
    ) : SearchRecipesUiState
}
