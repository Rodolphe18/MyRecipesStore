package com.francotte.myrecipesstore.ui.compose.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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

    var currentPage by mutableIntStateOf(0)

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.observeLatestRecipes(),
        repository.observeFoodAreaSections(),
        repository.observeEnglishAreaRecipes()
    ) { latestResult, areaSections, englishRecipes ->
        Log.d("debug1", latestResult.toString())
        Log.d("debug2", areaSections.toString())
        Log.d("debug3", englishRecipes.toString())
        if (latestResult.isSuccess || areaSections.isSuccess || englishRecipes.isSuccess) {
            HomeUiState.Success(
                latestRecipes = latestResult.getOrDefault(emptyList()),
                areaSections = areaSections.getOrDefault(emptyMap()),
                englishRecipes = englishRecipes.getOrDefault(emptyList())
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
        val areaSections: Map<String, List<LikeableRecipe>>,
        val englishRecipes: List<LikeableRecipe>
    ) : HomeUiState

    data object Error : HomeUiState
    data object Loading : HomeUiState
}