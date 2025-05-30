package com.francotte.myrecipesstore.ui.compose.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.model.Categories
import com.francotte.myrecipesstore.repository.CategoriesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(repository: CategoriesRepository): ViewModel() {

    val categories = repository
        .getAllMealCategories()
        .catch { CategoriesUiState.Error }
        .filterNotNull()
        .map { CategoriesUiState.Success(it) }
        .stateIn(viewModelScope, restartableWhileSubscribed, CategoriesUiState.Loading)

    fun reload() {
        viewModelScope.launch {
            restartableWhileSubscribed.restart()
        }
    }

}


sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data object Error : CategoriesUiState
    data class Success(
        val categories: Categories?
    ) : CategoriesUiState
}