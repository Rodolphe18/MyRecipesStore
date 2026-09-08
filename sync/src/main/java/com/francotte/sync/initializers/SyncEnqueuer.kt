package com.francotte.sync.initializers

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.francotte.data.sync.FavoritesSyncReason
import com.francotte.data.sync.SyncKind
import com.francotte.sync.workers.AreasAndIngredientsSyncWorker
import com.francotte.sync.workers.CategoriesSyncWorker
import com.francotte.sync.workers.FavoritesSyncWorker
import com.francotte.sync.workers.HomeSyncWorker
import com.francotte.sync.workers.KEY_FAVORITES_REASON
import com.francotte.sync.workers.SearchIndexWorker

/** How many index batches are chained per request. Each run indexes one batch of categories. */
private const val SEARCH_INDEX_RUNS = 3

/**
 * Translates a [SyncKind] into work on the queue.
 *
 * Shared by [Sync.initialize] and
 * [com.francotte.sync.status.WorkManagerSyncManager] so both go through the same unique names and
 * the same policies.
 */
internal fun WorkManager.enqueueSync(kind: SyncKind) {
    when (kind) {
        SyncKind.Home -> enqueueUniqueWork(
            HOME_SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            HomeSyncWorker::class.syncWorkRequest(),
        )

        SyncKind.Categories -> enqueueUniqueWork(
            CATEGORIES_SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            CategoriesSyncWorker::class.syncWorkRequest(),
        )

        SyncKind.AreasAndIngredients -> enqueueUniqueWork(
            AREAS_SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            AreasAndIngredientsSyncWorker::class.syncWorkRequest(),
        )

        SyncKind.SearchIndex -> enqueueSearchIndexChain()

        is SyncKind.Favorites -> enqueueUniqueWork(
            FAVORITES_SYNC_WORK_NAME,
            // A login must reconcile with the server, so it supersedes a queued toggle sync.
            // A toggle keeps whatever is already running: its change is in the pending list and
            // will be picked up by that run anyway.
            when (kind.reason) {
                FavoritesSyncReason.Login -> ExistingWorkPolicy.REPLACE
                FavoritesSyncReason.Toggle -> ExistingWorkPolicy.KEEP
            },
            FavoritesSyncWorker::class.syncWorkRequest(
                workDataOf(KEY_FAVORITES_REASON to kind.reason.name),
            ),
        )
    }
}

/**
 * Indexing the whole catalogue in one run would be a long, easily interrupted job, so the work is
 * chained: each run takes the next batch of stale categories and the following one picks up where
 * it stopped.
 */
private fun WorkManager.enqueueSearchIndexChain() {
    var continuation = beginUniqueWork(
        SEARCH_INDEX_WORK_NAME,
        ExistingWorkPolicy.KEEP,
        SearchIndexWorker::class.syncWorkRequest(),
    )
    repeat(SEARCH_INDEX_RUNS - 1) {
        continuation = continuation.then(SearchIndexWorker::class.syncWorkRequest())
    }
    continuation.enqueue()
}
