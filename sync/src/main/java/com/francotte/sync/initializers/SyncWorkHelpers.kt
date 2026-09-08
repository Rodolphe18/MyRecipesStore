package com.francotte.sync.initializers

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import com.francotte.sync.workers.DelegatingWorker
import com.francotte.sync.workers.delegatedData
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/** All sync work needs an internet connection. */
internal val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

/**
 * Shared tag on every sync request, so [com.francotte.sync.status.WorkManagerSyncManager] can
 * observe them all through a single flow.
 */
internal const val SYNC_WORK_TAG = "sync"

/** Delay before the first retry; WorkManager doubles it on each further attempt. */
private const val BACKOFF_DELAY_SECONDS = 30L

// Unique work names. Changing one makes WorkManager treat pending work as a different job, so
// they are kept together and left alone.
internal const val HOME_SYNC_WORK_NAME = "home-sync"
internal const val CATEGORIES_SYNC_WORK_NAME = "categories-sync"
internal const val AREAS_SYNC_WORK_NAME = "areas-and-ingredients-sync"
internal const val SEARCH_INDEX_WORK_NAME = "search-index-chain"
internal const val FAVORITES_SYNC_WORK_NAME = "favorites-sync"

/**
 * Builds the request WorkManager will persist.
 *
 * It always targets [DelegatingWorker]; the receiver only travels as a class name inside the input
 * data, so Hilt can build the real worker when the work eventually runs.
 */
internal fun KClass<out CoroutineWorker>.syncWorkRequest(
    extras: Data = Data.EMPTY,
): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<DelegatingWorker>()
        .setConstraints(SyncConstraints)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            BACKOFF_DELAY_SECONDS,
            TimeUnit.SECONDS,
        )
        .addTag(SYNC_WORK_TAG)
        .setInputData(delegatedData(extras))
        .build()
