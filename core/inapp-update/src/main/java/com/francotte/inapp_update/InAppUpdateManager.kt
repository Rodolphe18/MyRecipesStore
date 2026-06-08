package com.francotte.inapp_update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestUpdateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppUpdateManager @Inject constructor(
    private val appUpdateManager: AppUpdateManager,
    private val versionCodeProvider: CodeVersionProvider,
) : InAppUpdateRepository {

    var minimumVersionCode: Int? = null
    private var lastUpdateInfo: AppUpdateInfo? = null

    override fun observeUpdates(): Flow<InAppUpdateDomainState> =
        appUpdateManager.requestUpdateFlow().map { result ->
            when (result) {
                is AppUpdateResult.NotAvailable -> InAppUpdateDomainState.NotAvailable
                is AppUpdateResult.InProgress -> InAppUpdateDomainState.InProgress
                is AppUpdateResult.Downloaded -> InAppUpdateDomainState.Downloaded
                is AppUpdateResult.Available -> {
                    val info = result.updateInfo
                    lastUpdateInfo = info
                    val current = versionCodeProvider.currentVersionCode()
                    val available = info.availableVersionCode()
                    if (available <= current) return@map InAppUpdateDomainState.NotAvailable

                    val min = minimumVersionCode
                    val forceImmediate = min != null && current < min && available >= min
                    val type = if (forceImmediate) UpdateType.IMMEDIATE else UpdateType.FLEXIBLE
                    val allowed = if (type == UpdateType.IMMEDIATE) info.isImmediateUpdateAllowed else info.isFlexibleUpdateAllowed
                    if (!allowed) return@map InAppUpdateDomainState.NotAvailable

                    InAppUpdateDomainState.Available(type)
                }
            }
        }

    override fun launchUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>, type: UpdateType) {
        val info = lastUpdateInfo ?: return
        val playType = if (type == UpdateType.IMMEDIATE) IMMEDIATE else FLEXIBLE
        appUpdateManager.startUpdateFlowForResult(info, launcher, AppUpdateOptions.defaultOptions(playType))
    }

    override suspend fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
}
