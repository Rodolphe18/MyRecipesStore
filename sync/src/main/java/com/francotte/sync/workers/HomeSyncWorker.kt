package com.francotte.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.francotte.common.extension.Dispatcher
import com.francotte.common.extension.FoodDispatchers.IO
import com.francotte.data.interfaces.HomeRepository
import com.francotte.data.interfaces.UserHomeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/** Area sections prefetched on every home sync, on top of the generic ones. */
private val PREFETCHED_AREAS = listOf("Japanese", "British")

/**
 * Refreshes the home screen content: the latest recipes and the area sections.
 *
 * Each repository decides on its own whether the network is worth hitting, based on how old the
 * local rows are, hence `force = false` throughout.
 */
@HiltWorker
internal class HomeSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val homeRepository: HomeRepository,
    private val userHomeRepository: UserHomeRepository,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        runSyncCatching {
            val refreshes: List<suspend () -> Unit> = buildList {
                add { homeRepository.refreshLatestRecipes(force = false) }
                add { userHomeRepository.refreshMultipleFoodAreaSection(force = false) }
                PREFETCHED_AREAS.forEach { area ->
                    add { userHomeRepository.refreshSpecificFoodAreaSection(area, force = false) }
                }
            }
            coroutineScope {
                refreshes.map { refresh -> async { refresh() } }.awaitAll()
            }
        }
    }
}
