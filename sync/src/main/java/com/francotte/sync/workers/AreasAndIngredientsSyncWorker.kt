package com.francotte.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.francotte.common.extension.Dispatcher
import com.francotte.common.extension.FoodDispatchers.IO
import com.francotte.data.interfaces.IngredientsAndAreasRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Refreshes the areas and ingredients backing the search filters. */
@HiltWorker
internal class AreasAndIngredientsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ingredientsAndAreasRepository: IngredientsAndAreasRepository,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        runSyncCatching {
            ingredientsAndAreasRepository.refreshAllAreas(force = false)
            ingredientsAndAreasRepository.refreshAllIngredients(force = false)
        }
    }
}
