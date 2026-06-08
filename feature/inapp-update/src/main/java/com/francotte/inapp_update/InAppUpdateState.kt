package com.francotte.inapp_update

enum class InAppUpdateState { IDLE, CHECKING, ASKING, DOWNLOADING, DOWNLOADED, INSTALLING, FAILED }

data class InAppUpdateUiState(
    val state: InAppUpdateState = InAppUpdateState.IDLE,
    val isUpdateDownloaded: Boolean = false,
    val lastError: Throwable? = null,
)

sealed interface InAppUpdateEffect {
    data class LaunchUpdateFlow(val type: UpdateType) : InAppUpdateEffect
}
