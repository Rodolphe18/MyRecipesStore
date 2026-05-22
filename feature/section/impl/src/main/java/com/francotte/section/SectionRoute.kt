package com.francotte.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.section.api.SectionNavKey
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator


fun EntryProviderScope<NavKey>.sectionEntry(
    navigator: Navigator,
    onToggleFavorite: (LikeableRecipe) -> Unit
) {
    entry<SectionNavKey> {key ->
        SectionRoute(
            sectionViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<SectionViewModel, SectionViewModel.Factory>(
                key = key.sectionName,
            ) { factory ->
                factory.create(key.sectionName)
            },
            onToggleFavorite = onToggleFavorite,
            onOpenRecipe = navigator::navigateToDetail,
            onBackClick = navigator::goBack,
        )
    }
}


@Composable
fun SectionRoute(
    sectionViewModel: SectionViewModel = hiltViewModel(),
    onToggleFavorite: (LikeableRecipe) -> Unit,
    onOpenRecipe: (List<String>, Int, String) -> Unit,
    onBackClick: () -> Unit,
) {
    val uiState by sectionViewModel.sectionUiState.collectAsStateWithLifecycle()
    VerticalSectionScreen(uiState, sectionViewModel.sectionName, { action ->
        when (action) {
            SectionAction.BackClick -> { onBackClick() }
            is SectionAction.RecipeClick -> {
                onOpenRecipe(
                    action.recipeIds,
                    action.index,
                    action.title,
                )
            }
            is SectionAction.ToggleFavorite -> { onToggleFavorite(action.recipe) }
            SectionAction.Reload -> { sectionViewModel.onAction(action) }
        }
    })
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }
}
