package com.francotte.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.repository.FavoritesRepository
import com.francotte.model.CustomRecipe
import com.francotte.model.LikeableRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    var isReloading by mutableStateOf(false)


    val favoritesRecipesState =
        combine<Result<List<LikeableRecipe>>, Result<List<CustomRecipe>>, String, FavoriteUiState>(
            favoritesRepository
                .observeFavoritesRecipes(),
            favoritesRepository.observeUserCustomRecipes(),
            _searchText,
        ) { favorites, customRecipes, searchText ->
            if (searchText.isBlank()) {
                FavoriteUiState.Success(
                    favoritesRecipes = favorites.getOrDefault(emptyList()),
                    customRecipes = customRecipes.getOrDefault(emptyList()),
                )
            } else {
                FavoriteUiState.Success(
                    favorites.getOrDefault(emptyList()).filter {
                        it.recipe.strMeal
                            .lowercase()
                            .contains(searchText.lowercase())
                    },
                    customRecipes = customRecipes.getOrDefault(emptyList()),
                )
            }
        }.debounce(500)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoriteUiState.Loading)


    fun reload() {
        viewModelScope.launch {
            isReloading = true
            _searchText.value = ""
            favoritesRepository.refreshFavoritesRecipes()
            isReloading = false
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}

sealed interface FavoriteUiState {
    data class Success(
        val favoritesRecipes: List<LikeableRecipe>,
        val customRecipes: List<CustomRecipe>,
    ) : FavoriteUiState

    data object Error : FavoriteUiState

    data object Loading : FavoriteUiState
}
