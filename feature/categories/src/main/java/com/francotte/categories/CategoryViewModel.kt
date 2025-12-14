package com.francotte.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.common.restartableWhileSubscribed
import com.francotte.data.repository.HomeRepository
import com.francotte.model.LikeableRecipe
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
class CategoryViewModel @Inject constructor(savedStateHandle: SavedStateHandle, repository: HomeRepository): ViewModel() {

    val category = savedStateHandle.toRoute<CategoryNavigationRoute>().category

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)


    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryUiState = refreshTrigger
        .flatMapLatest { repository.observeRecipesByCategory(category) }
        .map(::mapToUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState.Loading)


    fun mapToUiState(result: Result<List<LikeableRecipe>>): CategoryUiState {
       return if (result.isSuccess) {
            CategoryUiState.Success(recipes = result.getOrDefault(emptyList()))
        } else {
            CategoryUiState.Error
        }
    }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }
}

sealed interface CategoryUiState {
    data object Loading : CategoryUiState
    data object Error : CategoryUiState
    data class Success(val recipes: List<LikeableRecipe>) : CategoryUiState
}