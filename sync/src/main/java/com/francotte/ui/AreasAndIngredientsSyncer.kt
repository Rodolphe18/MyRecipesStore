package com.francotte.ui

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.francotte.data.repository.OfflineFirstIngredientsAndAreasRepositoryImpl
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AreasAndIngredientsSyncEntryPoint {
    fun getOfflineFirstSearchRepository(): OfflineFirstIngredientsAndAreasRepositoryImpl
}

class AreasAndIngredientsSyncWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            val entryPoint =
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    AreasAndIngredientsSyncEntryPoint::class.java,
                )
            val syncer = entryPoint.getOfflineFirstSearchRepository()
            syncer.refreshAllAreas(force = false)
            syncer.refreshAllIngredients(force = false)
            Result.success()
        } catch (t: Throwable) {
            val maxAttempts = 3
            if (runAttemptCount + 1 >= maxAttempts) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
}

object AreasAndIngredientsSyncScheduler {
    private const val UNIQUE_SEARCH_ONE_SHOT = "search-sync-once"

    fun enqueueOneShot(context: Context) {
        val appCtx = context.applicationContext
        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<AreasAndIngredientsSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30_000,
                    java.util.concurrent.TimeUnit.MILLISECONDS,
                ).addTag("search-sync")
                .build()

        WorkManager.getInstance(appCtx).enqueueUniqueWork(
            UNIQUE_SEARCH_ONE_SHOT,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}

