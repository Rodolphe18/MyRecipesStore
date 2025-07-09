package com.francotte.myrecipesstore.ui.compose.categories.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.domain.model.Category
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.HomeRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(savedStateHandle: SavedStateHandle, repository: HomeRepository): ViewModel() {

    val category = savedStateHandle.toRoute<CategoryNavigationRoute>().category

    val categoryUiState = repository
        .observeRecipesByCategory(category)
        .map(::mapToUiState)
        .stateIn(viewModelScope, restartableWhileSubscribed, CategoryUiState.Loading)


    fun mapToUiState(result: Result<List<LikeableRecipe>>): CategoryUiState {
       return if (result.isSuccess) {
            CategoryUiState.Success(recipes = result.getOrDefault(emptyList()))
        } else {
            CategoryUiState.Error
        }
    }

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