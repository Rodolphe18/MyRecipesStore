package com.francotte.sync.status

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.francotte.data.sync.SyncKind
import com.francotte.data.sync.SyncManager
import com.francotte.sync.initializers.SYNC_WORK_TAG
import com.francotte.sync.initializers.enqueueSync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [SyncManager] backed by [WorkInfo] from [WorkManager].
 *
 * Every sync request carries [SYNC_WORK_TAG], so a single flow reports on all of them at once.
 */
@Singleton
internal class WorkManagerSyncManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SyncManager {

    override val isSyncing: Flow<Boolean> =
        WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(SYNC_WORK_TAG)
            .map(List<WorkInfo>::anyRunning)
            .conflate()

    override fun requestSync(kind: SyncKind) {
        WorkManager.getInstance(context).enqueueSync(kind)
    }
}

// Only work that is actually running counts: work merely waiting for a network connection should
// not show a loading indicator for what could be hours.
private fun List<WorkInfo>.anyRunning() = any { it.state == WorkInfo.State.RUNNING }
