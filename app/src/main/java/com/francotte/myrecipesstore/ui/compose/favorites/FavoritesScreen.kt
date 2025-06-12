package com.francotte.myrecipesstore.ui.compose.favorites

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.RecipeItem
import com.francotte.myrecipesstore.ui.navigation.TopAppBar
import com.francotte.myrecipesstore.ui.theme.Orange

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteUiState: FavoriteUiState,
    searchText:String,
    onSearchTextChanged:(String)->Unit,
    onReload: () -> Unit,
    onOpenRecipe: (List<String>, Int,String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit
) {
    val topAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())


    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                titleRes = R.string.favorites,
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
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

                    LazyVerticalGrid(
                        state = rememberLazyGridState(),
                        columns = GridCells.Fixed(2),
                        reverseLayout = false,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        flingBehavior = ScrollableDefaults.flingBehavior(),
                        contentPadding = PaddingValues(all = 16.dp)
                    ) {
                        val likeableRecipes = favoriteUiState.favoritesRecipes
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
}