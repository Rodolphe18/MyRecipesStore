package com.francotte.section

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.UserHomeRepository
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

@HiltViewModel(assistedFactory = SectionViewModel.Factory::class)
class SectionViewModel
@AssistedInject
constructor(
    repository: UserHomeRepository,
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
