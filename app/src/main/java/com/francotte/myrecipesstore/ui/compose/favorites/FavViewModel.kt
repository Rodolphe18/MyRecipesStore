package com.francotte.myrecipesstore.ui.compose.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.manager.FavoriteManager
import com.francotte.myrecipesstore.network.model.CustomRecipe
import com.francotte.myrecipesstore.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavViewModel @Inject constructor(
    val favoriteManager: FavoriteManager,
    private val favoritesRepository: FavoritesRepository
) :
    ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    val _favoritesRecipesState: MutableStateFlow<FavoriteUiState> =
        MutableStateFlow(FavoriteUiState.Loading)
    val favoritesRecipesState = _favoritesRecipesState.asStateFlow()

    var isReloading by mutableStateOf(false)

    init {
        viewModelScope.launch {
            favoriteManager.initFavorites()
            loadFavoritesData()
        }
    }

    @OptIn(FlowPreview::class)
    fun loadFavoritesData() {
        viewModelScope.launch {
            combine<Result<List<LikeableRecipe>>, Result<List<CustomRecipe>>, String, FavoriteUiState>(
                favoritesRepository
                    .observeFavoritesRecipes(),
                favoritesRepository.observeUserCustomRecipes(), _searchText,
            ) { favorites, customRecipes, searchText ->
                if (searchText.isBlank()) {
                    FavoriteUiState.Success(
                        favoritesRecipes = favorites.getOrDefault(emptyList()),
                        customRecipes = customRecipes.getOrDefault(emptyList())
                    )
                } else {
                    FavoriteUiState.Success(favorites.getOrDefault(emptyList()).filter {
                        it.recipe.strMeal.lowercase().contains(searchText.lowercase())
                    }, customRecipes = customRecipes.getOrDefault(emptyList()))
                }
            }
                .debounce(500)
                .collect { _favoritesRecipesState.value = it }
        }
    }

    fun reload() {
        isReloading = true
        _favoritesRecipesState.value = FavoriteUiState.Loading
        _searchText.value = ""
        loadFavoritesData()
        isReloading = false
    }


    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

}

sealed interface FavoriteUiState {
    data class Success(
        val favoritesRecipes: List<LikeableRecipe>,
        val customRecipes: List<CustomRecipe>
    ) : FavoriteUiState

    data object Error : FavoriteUiState
    data object Loading : FavoriteUiState
}