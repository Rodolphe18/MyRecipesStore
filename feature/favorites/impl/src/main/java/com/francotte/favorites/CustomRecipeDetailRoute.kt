package com.francotte.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.CustomRecipeNavKey
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.customRecipeEntry(navigator: Navigator) {
    entry<CustomRecipeNavKey> { key ->
        CustomRecipeDetailRoute(
            viewModel = hiltViewModel<CustomRecipeDetailViewModel, CustomRecipeDetailViewModel.Factory>(
                key = key.recipeId.orEmpty(),
            ) { factory ->
                factory.create(key.recipeId)
            },
            onBackClick = navigator::goBack,
        )
    }
}


@Composable
internal fun CustomRecipeDetailRoute(
    viewModel: CustomRecipeDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CustomRecipeDetailEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    CustomRecipeDetailScreen(
        state = state,
        onAction = { action ->
            when (action) {
                CustomRecipeDetailAction.OnBackClick -> onBackClick()
                else -> viewModel.onAction(action)
            }
        },
    )
}
