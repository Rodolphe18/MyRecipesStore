package com.francotte.inapp_update

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: InAppUpdateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InAppUpdateUiState())
    val uiState: StateFlow<InAppUpdateUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<InAppUpdateEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<InAppUpdateEffect> = _effects.asSharedFlow()

    init {
        startObserving()
    }

    private fun startObserving() {
        viewModelScope.launch {
            repository.observeUpdates()
                .distinctUntilChanged()
                .onStart { _uiState.update { it.copy(state = InAppUpdateState.CHECKING, lastError = null) } }
                .catch { e -> _uiState.update { it.copy(state = InAppUpdateState.FAILED, lastError = e) } }
                .collect { domainState ->
                    when (domainState) {
                        InAppUpdateDomainState.NotAvailable -> _uiState.update {
                            it.copy(state = InAppUpdateState.IDLE, isUpdateDownloaded = false)
                        }
                        InAppUpdateDomainState.InProgress -> _uiState.update {
                            it.copy(state = InAppUpdateState.DOWNLOADING)
                        }
                        InAppUpdateDomainState.Downloaded -> _uiState.update {
                            it.copy(state = InAppUpdateState.DOWNLOADED, isUpdateDownloaded = true)
                        }
                        is InAppUpdateDomainState.Available -> {
                            val current = _uiState.value.state
                            if (current == InAppUpdateState.ASKING || current == InAppUpdateState.DOWNLOADING) return@collect
                            _uiState.update { it.copy(state = InAppUpdateState.ASKING) }
                            _effects.tryEmit(InAppUpdateEffect.LaunchUpdateFlow(domainState.type))
                        }
                    }
                }
        }
    }

    fun launchUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>, type: UpdateType) {
        repository.launchUpdate(launcher, type)
    }

    fun onUpdateFlowResult(resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) {
            _uiState.update { it.copy(state = InAppUpdateState.IDLE) }
        }
    }

    fun completeUpdate() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(state = InAppUpdateState.INSTALLING) }
                repository.completeUpdate()
                _uiState.update { it.copy(state = InAppUpdateState.IDLE, isUpdateDownloaded = false) }
            } catch (e: Throwable) {
                _uiState.update { it.copy(state = InAppUpdateState.FAILED, lastError = e) }
            }
        }
    }
}
