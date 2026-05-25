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
import com.francotte.data.repository.CompositeUserHomeRepository
import com.francotte.data.repository.OfflineFirstHomeRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent


@EntryPoint
@InstallIn(SingletonComponent::class)
interface HomeSyncEntryPoint {
    fun latestRecipes(): OfflineFirstHomeRepository
    fun areasRecipes(): CompositeUserHomeRepository
}

class HomeSyncWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        try {
            val entryPoint =
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    HomeSyncEntryPoint::class.java,
                )
            val latestRecipes = entryPoint.latestRecipes()
            val areas = entryPoint.areasRecipes()
            latestRecipes.refreshLatestRecipes(force = false)
            areas.refreshFoodAreaSection(force = false)
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

object HomeSyncScheduler {
    private const val UNIQUE_ONE_SHOT = "home-sync-once"

    fun enqueueOneShot(context: Context) {
        val appCtx = context.applicationContext
        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<HomeSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30_000,
                    java.util.concurrent.TimeUnit.MILLISECONDS,
                ).addTag("home-sync")
                .build()

        WorkManager.getInstance(appCtx).enqueueUniqueWork(
            UNIQUE_ONE_SHOT,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
