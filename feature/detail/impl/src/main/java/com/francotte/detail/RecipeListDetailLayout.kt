package com.francotte.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.francotte.model.LikeableRecipe

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun RecipeListDetailLayout(
    state: DetailState,
    contentPadding: PaddingValues,
    onAction: (DetailAction) -> Unit,
) {
    val onToggleFavorite: (LikeableRecipe) -> Unit = {
        onAction(DetailAction.OnToggleFavorite(it))
    }

    // On force 2 volets même en largeur MEDIUM (téléphone en paysage).
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
        .copy(maxHorizontalPartitions = 2)
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>(scaffoldDirective = directive)

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                RecipeListPane(
                    recipes = state.recipes,
                    count = state.pageCount,
                    selectedIndex = state.selectedIndex,
                    onSelect = { onAction(DetailAction.OnRecipeSelected(it)) },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                state.recipes[state.selectedIndex]?.let { recipe ->
                    RecipeContent(
                        likeableRecipe = recipe,
                        onToggleFavorite = onToggleFavorite,
                        topPadding = contentPadding.calculateTopPadding() + 12.dp,
                    )
                }
            }
        },
    )
}
