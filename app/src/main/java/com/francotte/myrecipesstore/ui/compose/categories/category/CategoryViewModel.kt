package com.francotte.myrecipesstore.ui.compose.categories.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.model.RecipeResult
import com.francotte.myrecipesstore.repository.RecipesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(savedStateHandle: SavedStateHandle, repository: RecipesRepository): ViewModel() {

    private val category = savedStateHandle.toRoute<CategoryNavigationRoute>().category

    val categoryUiState = repository
        .getRecipesListByCategory(category)
        .catch { CategoryUiState.Error }
        .filterNotNull()
        .map { CategoryUiState.Success(it) }
        .stateIn(viewModelScope, restartableWhileSubscribed, CategoryUiState.Loading)

    fun reload() {
        viewModelScope.launch {
            restartableWhileSubscribed.restart()
        }
    }
}

sealed interface CategoryUiState {
    data class Success(val recipes: RecipeResult) : CategoryUiState
    data object Error : CategoryUiState
    data object Loading : CategoryUiState
}