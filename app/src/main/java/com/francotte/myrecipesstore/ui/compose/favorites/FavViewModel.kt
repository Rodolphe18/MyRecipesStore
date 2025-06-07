package com.francotte.myrecipesstore.ui.compose.favorites

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.favorites.FavoriteManager
import com.francotte.myrecipesstore.model.LikeableRecipe
import com.francotte.myrecipesstore.repository.FavoritesRepository
import com.francotte.myrecipesstore.ui.compose.section.SectionUiState
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class FavViewModel @Inject constructor(favoriteManager: FavoriteManager, favoritesRepository: FavoritesRepository) :
    ViewModel() {

    val favoritesRecipesState = favoritesRepository
        .getFavoritesRecipes()
        .map<Result<List<LikeableRecipe>>,FavoriteUiState> { FavoriteUiState.Success(it.getOrDefault(emptyList())) }
        .catch { emit(FavoriteUiState.Error) }
        .stateIn(viewModelScope, restartableWhileSubscribed, FavoriteUiState.Loading)

    init {
        favoriteManager.initFavorites()
    }

}

sealed interface FavoriteUiState {
    data class Success(
        val favoritesRecipes: List<LikeableRecipe>
    ) : FavoriteUiState

    data object Error : FavoriteUiState
    data object Loading : FavoriteUiState
}