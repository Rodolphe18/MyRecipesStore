package com.francotte.record_video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.francotte.navigation.Navigator
import com.francotte.record_video.api.RecordVideoNavKey

fun EntryProviderScope<NavKey>.recordVideoEntry(navigator: Navigator) {
    entry<RecordVideoNavKey> {
        RecordVideoRoute(onDone = { navigator.goBack() })
    }
}

@Composable
fun RecordVideoRoute(
    onDone: () -> Unit,
    viewModel: RecordVideoViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.done.collect { onDone() }
    }
    RecordVideoScreen(
        onUse = viewModel::onVideoConfirmed,
        onCancel = onDone,
    )
}
