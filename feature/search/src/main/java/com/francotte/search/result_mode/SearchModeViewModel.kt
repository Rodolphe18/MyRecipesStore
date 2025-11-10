package com.francotte.search.result_mode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.data.repository.SearchRepository
import com.francotte.search.SearchMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchModeViewModel @Inject constructor(
    repository: SearchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val searchMode = savedStateHandle.toRoute<SearchModeNavRoute>().searchMode

    val items = when (searchMode) {
        SearchMode.INGREDIENTS -> repository.observeAllIngredients()
        SearchMode.COUNTRY -> repository.observeAllAreas()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}


