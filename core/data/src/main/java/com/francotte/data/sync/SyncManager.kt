package com.francotte.data.sync

import kotlinx.coroutines.flow.Flow

/**
 * Requests synchronisations and reports on whether one is in progress.
 *
 * Implemented in the sync module on top of WorkManager, so callers never depend on it: they say
 * what they need synced, not when or how it should run.
 */
interface SyncManager {
    /** True while at least one sync is actually running, not merely enqueued. */
    val isSyncing: Flow<Boolean>

    fun requestSync(kind: SyncKind)
}
