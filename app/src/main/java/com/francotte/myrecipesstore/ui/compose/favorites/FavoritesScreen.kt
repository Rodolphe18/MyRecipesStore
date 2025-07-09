package com.francotte.myrecipesstore.ui.compose.favorites

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.RecipeItem
import com.francotte.myrecipesstore.ui.compose.composables.SectionTitle
import com.francotte.myrecipesstore.ui.compose.composables.nbCategoriesColumns
import com.francotte.myrecipesstore.ui.compose.composables.nbFavoritesColumns
import com.francotte.myrecipesstore.ui.compose.user_recipes.UserRecipesScreen

@Composable
fun FavoritesScreen(
    viewModel: FavViewModel= hiltViewModel<FavViewModel>(),
    windowSizeClass: WindowSizeClass,
    favoriteUiState: FavoriteUiState,
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onReload: () -> Unit,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit
) {
    Column {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            value = searchText,
            onValueChange = onSearchTextChanged,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "leading icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            placeholder = {
                Text(
                    text = "Favorites Search"
                )
            },
            textStyle = TextStyle(fontSize = 14.sp),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors()
        )
        when (favoriteUiState) {
            FavoriteUiState.Loading -> CustomCircularProgressIndicator()
            FavoriteUiState.Error -> ErrorScreen { onReload() }
            is FavoriteUiState.Success -> {
                LaunchedEffect(favoriteUiState.favoritesRecipes.isEmpty()) {
                    if (favoriteUiState.favoritesRecipes.isEmpty()) {
                        viewModel.favoriteManager.initFavorites()
                    }
                }
                LazyVerticalGrid(
                    state = rememberLazyGridState(),
                    columns = GridCells.Fixed(windowSizeClass.widthSizeClass.nbFavoritesColumns),
                    reverseLayout = false,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    contentPadding = PaddingValues(all = 16.dp)
                ) {
                    val likeableRecipes = favoriteUiState.favoritesRecipes
                    val customRecipes = favoriteUiState.customRecipes
                    if (customRecipes.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            UserRecipesScreen(customRecipes)
                        }
                        item(span = { GridItemSpan(2) }) {
                            SectionTitle(title = "Favorites", count =  likeableRecipes.size, paddingStart = 4.dp)
                        }
                    }
                    itemsIndexed(
                        items = likeableRecipes,
                        key = { index, likeableRecipe -> likeableRecipe.recipe.idMeal + index }
                    ) { index, likeableRecipe ->
                        RecipeItem(
                            likeableRecipe = likeableRecipe,
                            onToggleFavorite = onToggleFavorite,
                            onOpenRecipe = {
                                onOpenRecipe(
                                    likeableRecipes.map { it.recipe.idMeal },
                                    index,
                                    likeableRecipe.recipe.strMeal
                                )
                            })
                    }
                }
            }
        }
    }
}
