package com.francotte.search.result_recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.common.restartableWhileSubscribed
import com.francotte.data.repository.SearchRepository
import com.francotte.model.LikeableRecipe
import com.francotte.search.SearchMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchRecipesViewModel @Inject constructor(
    repository: SearchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val item = savedStateHandle.toRoute<SearchRecipesNavRoute>().item

    private val mode = savedStateHandle.toRoute<SearchRecipesNavRoute>().mode

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchRecipesUiState = when (mode) {
        SearchMode.COUNTRY -> refreshTrigger
            .flatMapLatest { repository.observeRecipesByArea(item) }
            .map { result ->
                if (result.isSuccess) {
                    SearchRecipesUiState.Success(result.getOrDefault(emptyList()))
                } else {
                    SearchRecipesUiState.Error
                }
            }

        SearchMode.INGREDIENTS -> refreshTrigger
            .flatMapLatest { repository.observeRecipesByIngredients(listOf(item)) }
            .map { result ->
                if (result.isSuccess) {
                    SearchRecipesUiState.Success(result.getOrDefault(emptyList()))
                } else {
                    SearchRecipesUiState.Error
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchRecipesUiState.Loading)


    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

}


sealed interface SearchRecipesUiState {
    data object Loading : SearchRecipesUiState
    data object Error : SearchRecipesUiState
    data class Success(val recipes: List<LikeableRecipe>) : SearchRecipesUiState
}