package com.francotte.myrecipesstore.ui.compose.search.result_recipes

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.SearchRepository
import com.francotte.myrecipesstore.ui.compose.search.SearchMode
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchRecipesViewModel @Inject constructor(
    repository: SearchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val item = savedStateHandle.toRoute<SearchRecipesNavRoute>().item

    private val mode = savedStateHandle.toRoute<SearchRecipesNavRoute>().mode

    val searchRecipesUiState = when (mode) {
        SearchMode.COUNTRY -> repository
            .observeRecipesByArea(item)
            .map { result ->
                if (result.isSuccess) {
                    SearchRecipesUiState.Success(result.getOrDefault(emptyList()))
                } else {
                    SearchRecipesUiState.Error
                }
            }

        SearchMode.INGREDIENTS -> repository
            .observeRecipesByIngredients(listOf(item))
            .map { result ->
                Log.d("debug_result_vm", result.toString())
                if (result.isSuccess) {
                    SearchRecipesUiState.Success(result.getOrDefault(emptyList()))
                } else {
                    SearchRecipesUiState.Error
                }
            }
    }.stateIn(viewModelScope, restartableWhileSubscribed, SearchRecipesUiState.Loading)


}


sealed interface SearchRecipesUiState {
    data object Loading : SearchRecipesUiState
    data object Error : SearchRecipesUiState
    data class Success(val recipes: List<LikeableRecipe>) : SearchRecipesUiState
}