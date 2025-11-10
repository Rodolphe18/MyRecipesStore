package com.francotte.section

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.common.restartableWhileSubscribed
import com.francotte.data.repository.HomeRepository
import com.francotte.model.LikeableRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: HomeRepository
) : ViewModel() {

    private val sectionName = savedStateHandle.toRoute<SectionRoute>().sectionName

    val section = MutableStateFlow(sectionName).asStateFlow()

    val sectionUiState = repository
        .observeFoodAreaSection(sectionName)
        .map { result ->
            if (result.isSuccess) {
                SectionUiState.Success(result.getOrDefault(emptyList()))
            } else {
                SectionUiState.Error
            }
        }
        .stateIn(viewModelScope, restartableWhileSubscribed, SectionUiState.Loading)

}

sealed interface SectionUiState {
    data class Success(val recipes: List<LikeableRecipe>) : SectionUiState
    data object Error : SectionUiState
    data object Loading : SectionUiState
}