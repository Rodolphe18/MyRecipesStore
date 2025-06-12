package com.francotte.myrecipesstore.ui.compose.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.repository.FavoritesRepository
import com.francotte.myrecipesstore.util.restartableWhileSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class FavViewModel @Inject constructor(favoriteManager: FavoriteManager, favoritesRepository: FavoritesRepository) :
    ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    val favoritesRecipesState = favoritesRepository
        .observeFavoritesRecipes().combine<Result<List<LikeableRecipe>>, String, FavoriteUiState>(_searchText) { favorites, searchText ->
            if (searchText.isBlank()) {
                FavoriteUiState.Success(favorites.getOrDefault(emptyList()))
            } else {
                FavoriteUiState.Success(favorites.getOrDefault(emptyList()).filter {
                    it.recipe.strMeal.lowercase().contains(searchText.lowercase())
                })
            }
        }
        .debounce(500)
        .catch { emit(FavoriteUiState.Error) }
        .stateIn(viewModelScope, restartableWhileSubscribed, FavoriteUiState.Loading)

    init {
        favoriteManager.initFavorites()
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

}

sealed interface FavoriteUiState {
    data class Success(
        val favoritesRecipes: List<LikeableRecipe>
    ) : FavoriteUiState
    data object Error : FavoriteUiState
    data object Loading : FavoriteUiState
}