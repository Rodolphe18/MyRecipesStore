package com.francotte.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.CategoriesRepository
import com.francotte.model.AbstractCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(private val repository: CategoriesRepository) :
    ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val snackBarMessage = _snackBarMessage.asSharedFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val categories =
        combine(repository.observeAllMealCategories(), _isRefreshing) { categories, reloading ->
            if (categories.isEmpty()) {
                CategoriesUiState.Empty
            } else {
                CategoriesUiState.Success(categories, reloading)
            }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                CategoriesUiState.Loading
            )


    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val message = repository.refreshAllMealCategories(true)
            message?.let {
                _snackBarMessage.tryEmit(it)
            }
            _isRefreshing.value = false
        }
    }
}


sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data object Empty : CategoriesUiState
    data class Success(val categories: List<AbstractCategory>, val isReloading: Boolean) :
        CategoriesUiState
}
