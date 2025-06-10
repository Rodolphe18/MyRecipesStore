package com.francotte.myrecipesstore.ui.compose.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.LikeableLightRecipesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: LikeableLightRecipesRepository
) : ViewModel() {


    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.observeLatestRecipes(),
        repository.observeFoodAreaSections()
    ) { latestResult, areaSections ->
        if (latestResult.isSuccess || areaSections.isSuccess) {
            HomeUiState.Success(
                latestRecipes = latestResult.getOrDefault(emptyList()),
                areaSections = areaSections.getOrDefault(mapOf())
            )
        } else {
            HomeUiState.Error
        }
    }
        .catch { emit(HomeUiState.Error) }
        .stateIn(viewModelScope, restartableWhileSubscribed, HomeUiState.Loading)


    fun reload() {

    }

}

sealed interface HomeUiState {
    data class Success(
        val latestRecipes: List<LikeableRecipe>,
        val areaSections: Map<String, List<LikeableRecipe>>
    ) : HomeUiState

    data object Error : HomeUiState
    data object Loading : HomeUiState
}