package com.francotte.myrecipesstore.ui.compose.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.model.RecipeResult
import com.francotte.myrecipesstore.repository.RecipesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(repository: RecipesRepository): ViewModel() {

    private val latestMeals = repository.getLatestMeals().stateIn(viewModelScope, restartableWhileSubscribed, null)
    private val randomMeals = repository.getLatestMeals().stateIn(viewModelScope, restartableWhileSubscribed, null)

    val homeUiState: StateFlow<HomeUiState> = combine(
        latestMeals,
        randomMeals
    ) { latestMeals, randomMeals ->
        when {
            latestMeals == null && randomMeals == null -> HomeUiState.Error
            else -> HomeUiState.Success(latestMeals, randomMeals)
        }
    }.stateIn(viewModelScope, restartableWhileSubscribed, HomeUiState.Loading)

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
