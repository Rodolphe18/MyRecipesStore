package com.francotte.inapp_update

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType

enum class InAppUpdateState {
    IDLE,
    CHECKING,
    ASKING,
    DOWNLOADING,
    DOWNLOADED,
    INSTALLING,
    FAILED
}

data class InAppUpdateUiState(
    val state: InAppUpdateState = InAppUpdateState.IDLE,
    val isUpdateDownloaded: Boolean = false,
    val lastError: Throwable? = null
)

sealed interface InAppUpdateEffect {
    data class LaunchUpdateFlow(
        val info: AppUpdateInfo,
        @AppUpdateType val type: Int
    ) : InAppUpdateEffect
}