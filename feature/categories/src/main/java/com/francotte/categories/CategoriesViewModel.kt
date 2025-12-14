package com.francotte.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.CategoriesRepository
import com.francotte.model.AbstractCategory
import com.francotte.model.Category
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
class CategoriesViewModel @Inject constructor(repository: CategoriesRepository) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories = refreshTrigger
        .flatMapLatest { repository.observeAllMealCategories() }
        .map(::mapToUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoriesUiState.Loading)

    private fun mapToUiState(result: Result<List<Category>>): CategoriesUiState {
        return if (result.isSuccess) {
            CategoriesUiState.Success(result.getOrDefault(emptyList()))
        } else {
            CategoriesUiState.Error
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


sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data object Error : CategoriesUiState
    data class Success(val categories: List<AbstractCategory>) : CategoriesUiState
}