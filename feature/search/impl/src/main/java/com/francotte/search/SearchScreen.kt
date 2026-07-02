package com.francotte.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.francotte.ads.BannerAd
import com.francotte.ads.BannerPlacement
import com.francotte.feature.search.api.SearchMode
import com.francotte.model.LikeableRecipe
import com.francotte.search.result_mode.SelectableChip
import com.francotte.ui.DeviceMode
import com.francotte.ui.LocalBannerProvider
import com.francotte.ui.RecipeItem
import com.francotte.ui.rememberDeviceMode

@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit,
) {
    val localBannerProvider = LocalBannerProvider.current
    val mode = rememberDeviceMode()
    val dimension = remember(mode) { searchModeButtonDimension(mode) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchToolbar(
            searchQuery = state.query,
            onSearchQueryChanged = { onAction(SearchAction.OnQueryChange(it)) },
            onSearchTriggered = {},
        )
        val result = state.result
        if (result is SearchResultUiState.Success) {
            SearchResultBody(
                categories = result.categories,
                areas = result.areas,
                ingredients = result.ingredients,
                likeableRecipes = result.likeableRecipes,
                onSearchTypeClick = { item, searchMode -> onAction(SearchAction.OnSearchTypeClick(item, searchMode)) },
                onRecipeClick = { index -> onAction(SearchAction.OnRecipeClick(index)) },
                onToggleFavorite = { onAction(SearchAction.OnToggleFavorite(it)) }
            )
        } else {
//            BannerAd(
//                placement = BannerPlacement.SEARCH,
//                provider = localBannerProvider,
//                horizontalPadding = 16.dp,
//            )
//            Spacer(Modifier.height(dimension.spacer1))
            Text(
                stringResource(R.string.search_recipes_question),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(32.dp))
            if (mode == DeviceMode.PhoneLandscape) {
                Row {
                    SearchModeButton(
                        stringResource(R.string.by_ingredients),
                        Icons.Default.ThumbUp,
                        dimension
                    ) {
                        onAction(SearchAction.OnSearchModeClick(SearchMode.INGREDIENTS))
                    }
                    Spacer(Modifier.width(60.dp))
                    SearchModeButton(
                        stringResource(R.string.by_country),
                        Icons.Default.Notifications,
                        dimension
                    ) {
                        onAction(SearchAction.OnSearchModeClick(SearchMode.COUNTRY))
                    }
                }
            } else {
                SearchModeButton(
                    stringResource(R.string.by_ingredients),
                    Icons.Default.ThumbUp,
                    dimension
                ) {
                    onAction(SearchAction.OnSearchModeClick(SearchMode.INGREDIENTS))
                }
                Spacer(Modifier.height(dimension.spacer2))
                SearchModeButton(
                    stringResource(R.string.by_country),
                    Icons.Default.Notifications,
                    dimension
                ) {
                    onAction(SearchAction.OnSearchModeClick(SearchMode.COUNTRY))
                }
            }
        }
    }
}


@Composable
private fun SearchResultBody(
    categories: List<String>,
    areas: List<String>,
    ingredients: List<String>,
    likeableRecipes: List<LikeableRecipe>,
    onSearchTypeClick: (String, SearchMode) -> Unit,
    onRecipeClick: (Int) -> Unit,
    onToggleFavorite: (LikeableRecipe) -> Unit
) {
    val state = rememberLazyGridState()
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {

        item(span = { GridItemSpan(3) }) {
            if (categories.isNotEmpty() || areas.isNotEmpty()) {
                SectionTitle("Categories and country")
            }
        }
        item(span = { GridItemSpan(3) }) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4,
                maxLines = 1
            ) {
                val category = categories.firstOrNull()
                category?.let { cat ->
                    SelectableChip(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(40.dp, 60.dp),
                        label = cat,
                        onClick = { onSearchTypeClick(cat, SearchMode.CATEGORIES) }
                    )
                }
                for (area in areas) {
                    SelectableChip(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(40.dp, 60.dp),
                        area,
                        { onSearchTypeClick(area, SearchMode.COUNTRY) })
                }
            }
        }
        item(span = { GridItemSpan(3) }) {
            if (ingredients.isNotEmpty()) {
                SectionTitle("Ingredients")
            }
        }
        item(span = { GridItemSpan(3) }) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 5,
                maxLines = 2
            ) {
                for (ing in ingredients) {
                    SelectableChip(
                        modifier = Modifier.weight(1f).heightIn(40.dp, 60.dp),
                        label = ing,
                        onClick = { onSearchTypeClick(ing, SearchMode.INGREDIENTS) }
                    )
                }
            }
        }
        item(span = { GridItemSpan(3) }) {
            if (likeableRecipes.isNotEmpty()) {
                SectionTitle("Recipes")
            }
        }
        itemsIndexed(likeableRecipes) { index, likeableRecipe ->
            RecipeItem(likeableRecipe, onToggleFavorite, { onRecipeClick(index) }, size = 120.dp)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp),
        color = Color.Black
    )
}

@Composable
fun SearchModeButton(
    label: String,
    icon: ImageVector,
    searchModeButtonDimension: SearchModeButtonDimension,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(searchModeButtonDimension.width)
            .aspectRatio(searchModeButtonDimension.ratio),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
        elevation = ButtonDefaults.buttonElevation(8.dp),
    ) {
        Icon(icon, contentDescription = null, Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}


data class SearchModeButtonDimension(
    val width: Dp,
    val ratio: Float,
    val spacer1: Dp,
    val spacer2: Dp
)

fun searchModeButtonDimension(mode: DeviceMode): SearchModeButtonDimension = when (mode) {
    DeviceMode.PhonePortrait -> SearchModeButtonDimension(
        width = 280.dp,
        ratio = 4f,
        spacer1 = 24.dp,
        spacer2 = 32.dp
    )

    DeviceMode.PhoneLandscape -> SearchModeButtonDimension(
        width = 200.dp,
        ratio = 2.5f,
        spacer1 = 12.dp,
        spacer2 = 8.dp
    )

    DeviceMode.TabletPortrait -> SearchModeButtonDimension(
        width = 200.dp,
        ratio = 3f,
        spacer1 = 32.dp,
        spacer2 = 32.dp
    )

    DeviceMode.TabletLandscape -> SearchModeButtonDimension(
        width = 250.dp,
        ratio = 3f,
        spacer1 = 40.dp,
        spacer2 = 40.dp
    )
}

@Composable
private fun SearchToolbar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp, max = 64.dp),
    ) {
        SearchTextField(
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            searchQuery = searchQuery,
        )
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val onSearchExplicitlyTriggered = {
        keyboardController?.hide()
        onSearchTriggered(searchQuery)
    }

    TextField(
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSearchQueryChanged("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        onValueChange = {
            if ("\n" !in it) onSearchQueryChanged(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.key == Key.Enter) {
                    if (searchQuery.isBlank()) return@onKeyEvent false
                    onSearchExplicitlyTriggered()
                    true
                } else {
                    false
                }
            }
            .testTag("searchTextField"),
        shape = RoundedCornerShape(32.dp),
        value = searchQuery,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (searchQuery.isBlank()) return@KeyboardActions
                onSearchExplicitlyTriggered()
            },
        ),
        maxLines = 1,
        singleLine = true,
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
