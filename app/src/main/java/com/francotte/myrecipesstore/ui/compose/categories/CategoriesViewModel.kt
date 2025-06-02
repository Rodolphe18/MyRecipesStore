package com.francotte.myrecipesstore.ui.compose.categories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.model.Categories
import com.francotte.myrecipesstore.model.RecipeResult
import com.francotte.myrecipesstore.repository.CategoriesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(repository: CategoriesRepository) : ViewModel() {

    val categories = repository
        .getAllMealCategories()
        .map { result ->
            if (result.isSuccess) {
                CategoriesUiState.Success(result.getOrDefault(Categories(emptyList())))
            } else {
                CategoriesUiState.Error
            }
        }.stateIn(viewModelScope, restartableWhileSubscribed, CategoriesUiState.Loading)


    fun reload() {
        viewModelScope.launch {
            restartableWhileSubscribed.restart()
        }
    }
}


sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data object Error : CategoriesUiState
    data class Success(val categories: Categories) : CategoriesUiState
}