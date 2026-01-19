package com.francotte.search.result_mode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.data.repository.SearchRepository
import com.francotte.feature.search.api.SearchMode
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel(assistedFactory = SearchModeViewModel.Factory::class)
class SearchModeViewModel
    @AssistedInject
    constructor(
        repository: SearchRepository,
        @Assisted val searchMode: SearchMode,
    ) : ViewModel() {

        val items =
            when (searchMode) {
                SearchMode.INGREDIENTS -> repository.observeAllIngredients()
                SearchMode.COUNTRY -> repository.observeAllAreas()
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @AssistedFactory
    interface Factory {
        fun create(searchMode: SearchMode):
            SearchModeViewModel
    }
    }
