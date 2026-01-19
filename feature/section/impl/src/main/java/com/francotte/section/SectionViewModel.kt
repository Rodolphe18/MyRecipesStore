package com.francotte.section

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.data.repository.HomeRepository
import com.francotte.feature.section.api.SectionNavKey
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel(assistedFactory = SectionViewModel.Factory::class)
class SectionViewModel
@AssistedInject
constructor(
    repository: HomeRepository,
    @Assisted val sectionName: String,
) : ViewModel() {


    val section = MutableStateFlow(sectionName).asStateFlow()

    val sectionUiState =
        repository
            .observeFoodAreaSection(sectionName)
            .map { result ->
                if (result.isSuccess) {
                    SectionUiState.Success(result.getOrDefault(emptyList()))
                } else {
                    SectionUiState.Error
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SectionUiState.Loading)

    @AssistedFactory
    interface Factory {
        fun create(title: String): SectionViewModel
    }

}

sealed interface SectionUiState {
    data class Success(
        val recipes: List<LikeableRecipe>,
    ) : SectionUiState

    data object Error : SectionUiState

    data object Loading : SectionUiState
}
