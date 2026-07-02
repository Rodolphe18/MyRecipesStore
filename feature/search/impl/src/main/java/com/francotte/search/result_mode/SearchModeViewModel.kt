package com.francotte.search.result_mode

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.IngredientsAndAreasRepository
import com.francotte.feature.search.api.SearchMode
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SearchModeViewModel.Factory::class)
class SearchModeViewModel @AssistedInject constructor(
    private val repository: IngredientsAndAreasRepository,
    @Assisted val searchMode: SearchMode,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    private val itemsFlow = when (searchMode) {
        SearchMode.INGREDIENTS -> repository.observeAllIngredients()
        SearchMode.COUNTRY -> repository.observeAllAreas()
        else -> flowOf(emptyList())
    }

    val state: StateFlow<SearchModeState> =
        combine(itemsFlow, _isRefreshing) { items, refreshing ->
            SearchModeState(title = searchMode.title, items = items, isRefreshing = refreshing)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SearchModeState(title = searchMode.title),
        )

    private val _events = Channel<SearchModeEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SearchModeAction) {
        when (action) {
            SearchModeAction.OnRefresh -> refresh()
            SearchModeAction.OnBackClick -> viewModelScope.launch {
                _events.send(SearchModeEvent.NavigateBack)
            }
            is SearchModeAction.OnItemClick -> viewModelScope.launch {
                _events.send(SearchModeEvent.NavigateToRecipes(action.item, searchMode))
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val message = when (searchMode) {
                SearchMode.INGREDIENTS -> repository.refreshAllIngredients(true)
                SearchMode.COUNTRY -> repository.refreshAllAreas(true)
                SearchMode.CATEGORIES -> null
            }
            if (message != null) {
                _events.send(SearchModeEvent.ShowSnackbar(message))
            }
            _isRefreshing.value = false
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(searchMode: SearchMode): SearchModeViewModel
    }
}

@Immutable
data class SearchModeState(
    val title: String = "",
    val items: List<String> = emptyList(),
    val isRefreshing: Boolean = false,
)

@Immutable
sealed interface SearchModeAction {
    data object OnRefresh : SearchModeAction
    data object OnBackClick : SearchModeAction
    data class OnItemClick(val item: String) : SearchModeAction
}

@Immutable
sealed interface SearchModeEvent {
    data class NavigateToRecipes(val item: String, val mode: SearchMode) : SearchModeEvent
    data object NavigateBack : SearchModeEvent
    data class ShowSnackbar(val message: String) : SearchModeEvent
}
