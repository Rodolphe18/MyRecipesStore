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
import com.francotte.data.repository.OfflineFirstCategoriesRepositoryImpl
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CategoriesSyncEntryPoint {
    fun getOfflineFirstCategoriesRepository(): OfflineFirstCategoriesRepositoryImpl
}

class CategoriesSyncWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            val entryPoint =
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    CategoriesSyncEntryPoint::class.java,
                )
            val syncer = entryPoint.getOfflineFirstCategoriesRepository()
            syncer.refreshAllMealCategories(false)
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

object CategoriesSyncScheduler {
    private const val UNIQUE_CATEGORIES_ONE_SHOT = "categories-sync-once"

    fun enqueueOneShot(context: Context) {
        val appCtx = context.applicationContext
        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<CategoriesSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30_000,
                    java.util.concurrent.TimeUnit.MILLISECONDS,
                ).addTag("search-sync")
                .build()

        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch { WorkManager.getInstance(appCtx).enqueueUniqueWork(
            UNIQUE_CATEGORIES_ONE_SHOT,
            ExistingWorkPolicy.REPLACE,
            request,
        )
        }


    }
}
