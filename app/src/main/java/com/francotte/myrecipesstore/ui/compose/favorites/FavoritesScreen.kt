package com.francotte.myrecipesstore.ui.compose.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.francotte.myrecipesstore.R
import com.francotte.myrecipesstore.domain.model.LikeableRecipe
import com.francotte.myrecipesstore.ui.compose.composables.CustomCircularProgressIndicator
import com.francotte.myrecipesstore.ui.compose.composables.ErrorScreen
import com.francotte.myrecipesstore.ui.compose.composables.RecipeItem
import com.francotte.myrecipesstore.ui.compose.composables.SectionTitle
import com.francotte.myrecipesstore.ui.compose.composables.nbFavoritesColumns
import com.francotte.myrecipesstore.ui.compose.user_recipes.UserRecipesScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavViewModel = hiltViewModel<FavViewModel>(),
    windowSizeClass: WindowSizeClass,
    favoriteUiState: FavoriteUiState,
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onToggleFavorite: (LikeableRecipe, Boolean) -> Unit
) {
    val lazyGridState = rememberLazyGridState()
    val focusManager = LocalFocusManager.current
    val pullRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    var showSearchBar by remember { mutableStateOf(true) }
    val lastScrollOffset = remember { mutableIntStateOf(0) }

    LaunchedEffect(searchText) {
        if (searchText.isEmpty()) {
            delay(3000)
            focusManager.clearFocus()
        }
    }
    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.firstVisibleItemScrollOffset }
            .collect { offset ->
                if (offset > lastScrollOffset.intValue) {
                    showSearchBar = false // Scroll vers le bas
                } else if (offset < lastScrollOffset.intValue) {
                    showSearchBar = true // Scroll vers le haut
                }
                lastScrollOffset.intValue = offset
            }
    }
    Column {
        AnimatedVisibility(
            visible = showSearchBar,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
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
                    Text(text = "Favorites Search")
                },
                textStyle = TextStyle(fontSize = 14.sp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors()
            )
        }
        when (favoriteUiState) {
            FavoriteUiState.Loading -> CustomCircularProgressIndicator()
            FavoriteUiState.Error -> ErrorScreen { viewModel.reload() }
            is FavoriteUiState.Success -> {
                LaunchedEffect(favoriteUiState.favoritesRecipes.isEmpty()) {
                    if (favoriteUiState.favoritesRecipes.isEmpty()) {
                        viewModel.favoriteManager.initFavorites()
                    }
                }
                if (favoriteUiState.favoritesRecipes.isEmpty() && favoriteUiState.customRecipes.isEmpty()) {
                    AnimatedCookbookScreen()
                } else {
                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
                        isRefreshing = viewModel.isReloading,
                        onRefresh = {
                            coroutineScope.launch {
                                viewModel.reload()
                                pullRefreshState.animateToHidden()
                            }
                        },
                        state = pullRefreshState
                    ) {
                        LazyVerticalGrid(
                            state = lazyGridState,
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
                                    SectionTitle(
                                        title = "Favorites",
                                        count = likeableRecipes.size,
                                        paddingStart = 4.dp
                                    )
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
    }
}


@Composable
fun AnimatedCookbookScreen() {
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animation vers le haut puis retour avec rebond
        offsetY.animateTo(
            targetValue = -20f, // déplacement vers le haut
            animationSpec = tween(durationMillis = 600)
        )
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.recipe),
            contentDescription = "Cookbook image",
            modifier = Modifier
                .offset(y = offsetY.value.dp)
                .size(200.dp),
            contentScale = ContentScale.None
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.add_favorites_recipes),
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            lineHeight = 35.sp
        )
    }
}

