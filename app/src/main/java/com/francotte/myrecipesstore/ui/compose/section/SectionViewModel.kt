package com.francotte.myrecipesstore.ui.compose.section

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.model.RecipeResult
import com.francotte.myrecipesstore.repository.RecipesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: RecipesRepository
) : ViewModel() {

    private val sectionType = savedStateHandle.toRoute<SectionRoute>().sectionType

    val section = MutableStateFlow(sectionType).asStateFlow()

    val sectionUiState = when (sectionType) {
        SectionType.LATEST_RECIPES -> repository
            .getLatestMeals()
            .map { SectionUiState.Success(it.getOrDefault(RecipeResult.Empty)) }
            .catch { SectionUiState.Error }
            .stateIn(viewModelScope, restartableWhileSubscribed, SectionUiState.Loading)

        SectionType.TOP_RECIPES -> repository
            .getRandomMealsSelection()
            .map { SectionUiState.Success(it.getOrDefault(RecipeResult.Empty)) }
            .catch { SectionUiState.Error }
            .stateIn(viewModelScope, restartableWhileSubscribed, SectionUiState.Loading)
    }


    fun reload() {
        viewModelScope.launch {
            restartableWhileSubscribed.restart()
        }
    }
}

sealed interface SectionUiState {
    data class Success(val recipes: RecipeResult) : SectionUiState
    data object Error : SectionUiState
    data object Loading : SectionUiState
}