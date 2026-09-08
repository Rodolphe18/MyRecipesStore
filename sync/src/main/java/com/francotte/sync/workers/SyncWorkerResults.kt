package com.francotte.sync.workers

import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import kotlin.coroutines.cancellation.CancellationException

/** How many times a sync is attempted before it gives up for good. */
private const val MAX_SYNC_ATTEMPTS = 3

/**
 * Runs [block] and turns its outcome into a [ListenableWorker.Result].
 *
 * A failure asks WorkManager to retry with its exponential backoff, until [MAX_SYNC_ATTEMPTS] is
 * reached. [CancellationException] is rethrown rather than swallowed, so cancelling the work does
 * not look like a failed sync and burn a retry.
 */
internal suspend fun CoroutineWorker.runSyncCatching(
    block: suspend () -> Unit,
): ListenableWorker.Result = try {
    block()
    ListenableWorker.Result.success()
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (throwable: Throwable) {
    if (runAttemptCount + 1 >= MAX_SYNC_ATTEMPTS) {
        ListenableWorker.Result.failure()
    } else {
        ListenableWorker.Result.retry()
    }
}
