package com.francotte.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.common.restartableWhileSubscribed
import com.francotte.data.repository.CategoriesRepository
import com.francotte.model.AbstractCategory
import com.francotte.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(repository: CategoriesRepository) : ViewModel() {

    val categories = repository
        .observeAllMealCategories()
        .map(::mapToUiState)
        .stateIn(viewModelScope, restartableWhileSubscribed, CategoriesUiState.Loading)

    private fun mapToUiState(result: Result<List<Category>>): CategoriesUiState {
        return if (result.isSuccess) {
            CategoriesUiState.Success(result.getOrDefault(emptyList()))
        } else {
            CategoriesUiState.Error
        }
    }

    fun reload() {
        viewModelScope.launch {
            restartableWhileSubscribed.restart()
        }
    }
}


sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data object Error : CategoriesUiState
    data class Success(val categories: List<AbstractCategory>) : CategoriesUiState
}