package com.francotte.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.HomeRepository
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CategoryViewModel.Factory::class)
class CategoryViewModel @AssistedInject constructor(
    @Assisted val category: String,
    repository: HomeRepository,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryUiState =
        refreshTrigger
            .flatMapLatest { repository.observeRecipesByCategory(category) }
            .map(::mapToUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState.Loading)

    fun mapToUiState(result: Result<List<LikeableRecipe>>): CategoryUiState =
        if (result.isSuccess) {
            CategoryUiState.Success(recipes = result.getOrDefault(emptyList()))
        } else {
            CategoryUiState.Error
        }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(category: String): CategoryViewModel
    }
}

sealed interface CategoryUiState {
    data object Loading : CategoryUiState

    data object Error : CategoryUiState

    data class Success(
        val recipes: List<LikeableRecipe>,
    ) : CategoryUiState
}
