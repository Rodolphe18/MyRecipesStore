package com.francotte.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.navigateToDetail
import com.francotte.common.counters.ScreenCounter
import com.francotte.feature.section.api.SectionNavKey
import com.francotte.model.LikeableRecipe
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.sectionEntry(
    navigator: Navigator,
    onToggleFavorite: (LikeableRecipe) -> Unit
) {
    entry<SectionNavKey> { key ->
        SectionRoute(
            sectionViewModel = hiltViewModel<SectionViewModel, SectionViewModel.Factory>(
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
    val state by sectionViewModel.state.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHostState.current

    LaunchedEffect(sectionViewModel) {
        sectionViewModel.events.collect { event ->
            when (event) {
                is SectionEvent.NavigateToRecipe -> onOpenRecipe(event.ids, event.index, event.title)
                SectionEvent.NavigateBack -> onBackClick()
                is SectionEvent.ShowSnackbar -> snackBarHost.showSnackbar(event.message)
            }
        }
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }

    VerticalSectionScreen(
        state = state,
        onAction = { action ->
            when (action) {
                // Favorite toggling stays outside the VM (FavoriteManager decoupling).
                is SectionAction.OnToggleFavorite -> onToggleFavorite(action.recipe)
                else -> sectionViewModel.onAction(action)
            }
        },
    )
}
