package com.francotte.myrecipesstore.ui.compose.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.DetailRecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DetailRecipeViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val detailRecipeRepository: DetailRecipeRepository):ViewModel() {

    private val idMeal = savedStateHandle.toRoute<DetailRecipeRoute>().idMeal.toLong()

    val recipe = detailRecipeRepository
        .getMealDetail(idMeal)
        .map { result ->
            if (result.isSuccess) {
                DetailRecipeUiState.Success(result.getOrDefault(emptyList()).firstOrNull())
            } else {
                DetailRecipeUiState.Error
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetailRecipeUiState.Loading)

}

sealed interface DetailRecipeUiState {
    data object Loading : DetailRecipeUiState
    data object Error : DetailRecipeUiState
    data class Success(val likeableRecipe: LikeableRecipe?) :DetailRecipeUiState
}