package com.francotte.myrecipesstore.ui.compose.categories.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.RecipesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(savedStateHandle: SavedStateHandle, repository: RecipesRepository): ViewModel() {

    val category = savedStateHandle.toRoute<CategoryNavigationRoute>().category

    val categoryUiState = repository
        .getRecipesListByCategory(category)
        .map { result ->
            if (result.isSuccess) {
                CategoryUiState.Success(result.getOrDefault(emptyList()))
            } else {
                CategoryUiState.Error
            }
        }
        .stateIn(viewModelScope, restartableWhileSubscribed, CategoryUiState.Loading)


    fun reload() {
        viewModelScope.launch {
            restartableWhileSubscribed.restart()
        }
    }
}

sealed interface CategoryUiState {
    data object Loading : CategoryUiState
    data object Error : CategoryUiState
    data class Success(val recipes: List<LikeableRecipe>) : CategoryUiState
}