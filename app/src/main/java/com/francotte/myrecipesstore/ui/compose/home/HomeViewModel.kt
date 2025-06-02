package com.francotte.myrecipesstore.ui.compose.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.model.RecipeResult
import com.francotte.myrecipesstore.repository.RecipesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(repository: RecipesRepository): ViewModel() {

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.getLatestMeals(),
        repository.getRandomMealsSelection()
    ) { latestResult, randomResult ->
        if (latestResult.isFailure || randomResult.isFailure) {
            HomeUiState.Error
        } else {
            HomeUiState.Success(
                latestResult.getOrDefault(RecipeResult.Empty),
                randomResult.getOrDefault(RecipeResult.Empty)
            )
        }
    }
        .onStart { emit(HomeUiState.Loading) }
        .stateIn(viewModelScope, restartableWhileSubscribed, HomeUiState.Loading)
    fun reload() {
        viewModelScope.launch {
            restartableWhileSubscribed.restart()
        }
    }

}


sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Error : HomeUiState
    data class Success(
        val latestRecipes: RecipeResult? = null,
        val randomRecipes: RecipeResult? = null
    ) : HomeUiState
}
