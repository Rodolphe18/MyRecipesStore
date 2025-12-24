package com.francotte.inapp_update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestUpdateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InAppUpdateViewModel @Inject constructor(
    private val appUpdateManager: AppUpdateManager,
    private val versionCodeProvider: CodeVersionProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(InAppUpdateUiState())
    val uiState: StateFlow<InAppUpdateUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<InAppUpdateEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<InAppUpdateEffect> = _effects.asSharedFlow()

    /**
     * Optionnel : si tu veux un “force update” en dessous d’une version minimale.
     */
    var minimumVersionCode: Int? = null

    fun startObserving() {
        val currentVersionCode = versionCodeProvider.currentVersionCode()
        viewModelScope.launch {
            appUpdateManager.requestUpdateFlow()
                .distinctUntilChanged()
                .onStart { _uiState.update { it.copy(state = InAppUpdateState.CHECKING, lastError = null) } }
                .catch { e ->
                    _uiState.update { it.copy(state = InAppUpdateState.FAILED, lastError = e) }
                }
                .collect { result ->
                    when (result) {
                        is AppUpdateResult.NotAvailable -> {
                            _uiState.update { it.copy(state = InAppUpdateState.IDLE, isUpdateDownloaded = false) }
                        }

                        is AppUpdateResult.InProgress -> {
                            _uiState.update { it.copy(state = InAppUpdateState.DOWNLOADING) }
                        }

                        is AppUpdateResult.Downloaded -> {
                            _uiState.update { it.copy(state = InAppUpdateState.DOWNLOADED, isUpdateDownloaded = true) }
                        }

                        is AppUpdateResult.Available -> {
                            // Décide du type (immediate si "blocking" sinon flexible)
                            val info = result.updateInfo
                            val available = info.availableVersionCode()
                            if (available <= currentVersionCode) return@collect

                            val min = minimumVersionCode
                            val forceImmediate = (min != null && currentVersionCode < min && available >= min)

                            val type = if (forceImmediate) IMMEDIATE else FLEXIBLE
                            val allowed = if (type == IMMEDIATE) info.isImmediateUpdateAllowed else info.isFlexibleUpdateAllowed
                            if (!allowed) return@collect

                            // évite de spammer si déjà en ASKING/DOWNLOADING
                            val current = _uiState.value.state
                            if (current == InAppUpdateState.ASKING || current == InAppUpdateState.DOWNLOADING) return@collect

                            _uiState.update { it.copy(state = InAppUpdateState.ASKING) }
                            _effects.tryEmit(InAppUpdateEffect.LaunchUpdateFlow(info, type))
                        }
                    }
                }
        }
    }

    fun onUpdateFlowResult(resultCode: Int) {
        // RESULT_OK => téléchargement va démarrer (InProgress arrivera)
        // RESULT_CANCELED => user a refusé
        // RESULT_IN_APP_UPDATE_FAILED => parfois
        if (resultCode != android.app.Activity.RESULT_OK) {
            _uiState.update { it.copy(state = InAppUpdateState.IDLE) }
        }
    }

    fun completeUpdate() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(state = InAppUpdateState.INSTALLING) }
                appUpdateManager.completeUpdate()
                _uiState.update { it.copy(state = InAppUpdateState.IDLE, isUpdateDownloaded = false) }
            } catch (e: Throwable) {
                _uiState.update { it.copy(state = InAppUpdateState.FAILED, lastError = e) }
            }
        }
    }
}