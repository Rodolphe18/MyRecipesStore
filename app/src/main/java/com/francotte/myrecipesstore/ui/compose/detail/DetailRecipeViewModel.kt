package com.francotte.myrecipesstore.ui.compose.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.FullRecipeRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DetailRecipeViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val detailRecipeRepository: FullRecipeRepositoryImpl):ViewModel() {

    private val recipeId = savedStateHandle.toRoute<DetailRecipeRoute>().recipeId.toLong()

    val recipeName = savedStateHandle.toRoute<DetailRecipeRoute>().recipeTitle

    val recipe = detailRecipeRepository
        .observeFullRecipe(recipeId)
        .map { result ->
            if (result.isSuccess) {
                DetailRecipeUiState.Success(result.getOrDefault(null))
            } else {
                DetailRecipeUiState.Error
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetailRecipeUiState.Loading)

}

sealed interface DetailRecipeUiState {
    data object Loading : DetailRecipeUiState
    data object Error : DetailRecipeUiState
    data class Success(val recipe: LikeableRecipe?) :DetailRecipeUiState
}