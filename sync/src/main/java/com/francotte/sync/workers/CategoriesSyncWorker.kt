package com.francotte.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.francotte.common.extension.Dispatcher
import com.francotte.common.extension.FoodDispatchers.IO
import com.francotte.data.interfaces.CategoriesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Refreshes the meal categories list. */
@HiltWorker
internal class CategoriesSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val categoriesRepository: CategoriesRepository,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        runSyncCatching {
            categoriesRepository.refreshAllMealCategories(force = false)
        }
    }
}
