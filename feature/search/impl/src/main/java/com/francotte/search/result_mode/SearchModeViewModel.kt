package com.francotte.search.result_mode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.IngredientsAndAreasRepository
import com.francotte.feature.search.api.SearchMode
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@HiltViewModel(assistedFactory = SearchModeViewModel.Factory::class)
class SearchModeViewModel
@AssistedInject
constructor(
    private val repository: IngredientsAndAreasRepository,
    @Assisted val searchMode: SearchMode,
) : ViewModel() {

    private val _snackBarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1,replay = 1)
    val snackBarMessage = _snackBarMessage.asSharedFlow()

    private val _isReloading = MutableStateFlow(false)
    val isReloading = _isReloading.asStateFlow()

    val items =
        when (searchMode) {
            SearchMode.INGREDIENTS -> repository.observeAllIngredients()
            SearchMode.COUNTRY -> repository.observeAllAreas()
            else -> flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun onRefresh() {
        viewModelScope.launch {
            _isReloading.value = true
            val message = when (searchMode) {
                SearchMode.INGREDIENTS -> repository.refreshAllIngredients(true)
                SearchMode.COUNTRY -> repository.refreshAllAreas(true)
                SearchMode.CATEGORIES -> null
            }
            message?.let {
                _snackBarMessage.emit(it)
            }
            _isReloading.value = false
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(searchMode: SearchMode):
            SearchModeViewModel
    }
}
