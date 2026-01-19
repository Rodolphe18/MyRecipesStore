package com.francotte.search.result_recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.data.repository.SearchRepository
import com.francotte.feature.search.api.SearchMode
import com.francotte.model.LikeableRecipe
import com.francotte.search.result_mode.SearchModeViewModel
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
import javax.inject.Inject

@HiltViewModel(assistedFactory = SearchRecipesViewModel.Factory::class)
class SearchRecipesViewModel
@AssistedInject
constructor(
    repository: SearchRepository,
    @Assisted mode: SearchMode,
    @Assisted val item:String

) : ViewModel() {
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)


    @OptIn(ExperimentalCoroutinesApi::class)
    val searchRecipesUiState =
        when (mode) {
            SearchMode.COUNTRY ->
                refreshTrigger
                    .flatMapLatest { repository.observeRecipesByArea(item) }
                    .map { result ->
                        if (result.isSuccess) {
                            SearchRecipesUiState.Success(result.getOrDefault(emptyList()))
                        } else {
                            SearchRecipesUiState.Error
                        }
                    }

            SearchMode.INGREDIENTS ->
                refreshTrigger
                    .flatMapLatest { repository.observeRecipesByIngredients(listOf(item)) }
                    .map { result ->
                        if (result.isSuccess) {
                            SearchRecipesUiState.Success(result.getOrDefault(emptyList()))
                        } else {
                            SearchRecipesUiState.Error
                        }
                    }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SearchRecipesUiState.Loading
        )

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
        fun create(item: String, mode: SearchMode): SearchRecipesViewModel
    }
}

sealed interface SearchRecipesUiState {
    data object Loading : SearchRecipesUiState

    data object Error : SearchRecipesUiState

    data class Success(
        val recipes: List<LikeableRecipe>,
    ) : SearchRecipesUiState
}
