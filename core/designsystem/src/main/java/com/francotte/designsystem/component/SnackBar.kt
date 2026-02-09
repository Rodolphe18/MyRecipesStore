package com.francotte.designsystem.component

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun FoodSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        Snackbar(
            snackbarData = data,
            containerColor =Color(0xFFFACF5C), // jaune crème
            contentColor = Color(0xFF000000),   // texte noir
            actionColor = Color(0xFF000000),    // couleur du bouton action si tu en as
        )
    }
}
