package com.francotte.inapp_update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.flow.Flow

interface InAppUpdateRepository {
    fun observeUpdates(): Flow<InAppUpdateDomainState>
    fun launchUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>, type: UpdateType)
    suspend fun completeUpdate()
}
