package com.francotte.detail

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.defaultDragHandleSemantics
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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

    // Largeur par défaut : liste 1/3, détail 2/3 ; redimensionnable via la poignée (anchors).
    val expansionState = rememberPaneExpansionState(
        anchors = listOf(
            PaneExpansionAnchor.Proportion(1f / 3f),
            PaneExpansionAnchor.Proportion(1f / 2f),
            PaneExpansionAnchor.Proportion(2f / 3f),
        ),
        initialAnchoredIndex = 0,
    )

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        paneExpansionState = expansionState,
        paneExpansionDragHandle = { paneState ->
            val interactionSource = remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier = Modifier.paneExpansionDraggable(
                    state = paneState,
                    minTouchTargetSize = LocalMinimumInteractiveComponentSize.current,
                    interactionSource = interactionSource,
                    semanticsProperties = paneState.defaultDragHandleSemantics(),
                ),
                interactionSource = interactionSource,
            )
        },
        listPane = {
            AnimatedPane {
                RecipeListPane(
                    recipes = state.recipes,
                    count = state.pageCount,
                    selectedIndex = state.selectedIndex,
                    onSelect = { onAction(DetailAction.OnRecipeSelected(it)) },
                    topPadding = contentPadding.calculateTopPadding() + 12.dp
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
