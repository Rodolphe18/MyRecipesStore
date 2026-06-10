package com.francotte.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.CategoriesRepository
import com.francotte.model.AbstractCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: CategoriesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state = _state.asStateFlow()

    private val _events = Channel<CategoriesEvent>()
    val events = _events.receiveAsFlow()

    init {
        repository.observeAllMealCategories()
            .onEach { categories ->
                _state.update { it.copy(categories = categories, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CategoriesAction) {
        when (action) {
            CategoriesAction.OnRefresh -> refresh()
            is CategoriesAction.OnCategoryClick -> viewModelScope.launch {
                _events.send(CategoriesEvent.NavigateToCategory(action.category))
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val message = repository.refreshAllMealCategories(true)
            message?.let { _events.send(CategoriesEvent.ShowSnackbar(it)) }
            _state.update { it.copy(isRefreshing = false) }
        }
    }
}

data class CategoriesState(
    val categories: List<AbstractCategory> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && categories.isEmpty()
}

sealed interface CategoriesAction {
    data object OnRefresh : CategoriesAction
    data class OnCategoryClick(val category: AbstractCategory) : CategoriesAction
}

sealed interface CategoriesEvent {
    data class NavigateToCategory(val category: AbstractCategory) : CategoriesEvent
    data class ShowSnackbar(val message: String) : CategoriesEvent
}
