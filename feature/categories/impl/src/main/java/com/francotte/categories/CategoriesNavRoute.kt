package com.francotte.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.api.CategoriesNavKey
import com.francotte.api.navigateToCategory
import com.francotte.common.counters.ScreenCounter
import com.francotte.model.AbstractCategory
import com.francotte.navigation.Navigator
import com.francotte.ui.LocalSnackbarHostState


fun EntryProviderScope<NavKey>.categoriesEntry(
    navigator: Navigator
) {
    entry<CategoriesNavKey> {
        CategoriesRoute(onOpenCategory = { category -> navigator.navigateToCategory(category.strCategory) })
    }
}


@Composable
fun CategoriesRoute(
    viewModel: CategoriesViewModel = hiltViewModel(),
    onOpenCategory: (AbstractCategory) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHostState.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CategoriesEvent.NavigateToCategory -> onOpenCategory(event.category)
                is CategoriesEvent.ShowSnackbar -> snackBarHost.showSnackbar(event.message)
            }
        }
    }
    LaunchedEffect(Unit) {
        ScreenCounter.increment()
    }

    CategoriesScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}
