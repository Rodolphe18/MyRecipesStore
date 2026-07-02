package com.francotte.inapp_update

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun InAppUpdateModal(
    snackBarHostState: SnackbarHostState,
    viewModel: InAppUpdateViewModel = hiltViewModel(),
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        viewModel.onUpdateFlowResult(result.resultCode)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is InAppUpdateEffect.LaunchUpdateFlow -> viewModel.launchUpdate(launcher, effect.type)
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isUpdateDownloaded) {
        if (uiState.isUpdateDownloaded) {
            val res = snackBarHostState.showSnackbar(
                message = "Mise à jour prête à installer",
                actionLabel = "Redémarrer",
            )
            if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.completeUpdate()
            }
        }
    }
}
