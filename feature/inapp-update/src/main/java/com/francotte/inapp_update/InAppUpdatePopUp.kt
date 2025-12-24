package com.francotte.inapp_update

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import kotlinx.coroutines.flow.collectLatest

@Composable
fun InAppUpdateHost(
    appUpdateManager: AppUpdateManager,
    snackbarHostState: SnackbarHostState,
    viewModel: InAppUpdateViewModel = hiltViewModel()
) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onUpdateFlowResult(result.resultCode)
    }

    LaunchedEffect(Unit) {
        viewModel.startObserving()
    }

    // 3) Réagit aux effects: lancement du flow via Activity Result API
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is InAppUpdateEffect.LaunchUpdateFlow -> {
                    // API Activity Result ajoutée dans la lib 2.1.0
                    appUpdateManager.startUpdateFlowForResult(
                        effect.info,
                        launcher,
                        AppUpdateOptions.defaultOptions(effect.type)
                    )
                }
            }
        }
    }

    // 4) Quand c’est téléchargé (flexible), on propose “Redémarrer”
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isUpdateDownloaded) {
        if (uiState.isUpdateDownloaded) {
            val res = snackbarHostState.showSnackbar(
                message = "Mise à jour prête à installer",
                actionLabel = "Redémarrer"
            )
            if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.completeUpdate()
            }
        }
    }
}