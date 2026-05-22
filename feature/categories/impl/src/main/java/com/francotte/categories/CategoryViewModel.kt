package com.francotte.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.UserHomeRepository
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CategoryViewModel.Factory::class)
class CategoryViewModel @AssistedInject constructor(
    @Assisted val category: String,
    private val repository: UserHomeRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val categoryUiState =
        repository.observeRecipesByCategory(category)
            .map(::mapToUiState)
            .debounce(250)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState.Loading)

    fun mapToUiState(recipes: List<LikeableRecipe>?): CategoryUiState =
        if (!recipes.isNullOrEmpty()) {
            CategoryUiState.Success(recipes = recipes)
        } else {
            CategoryUiState.Error
        }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshRecipesByCategory(category,true)
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
