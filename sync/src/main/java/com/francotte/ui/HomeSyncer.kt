package com.francotte.ui
import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

interface HomeSyncer {
    suspend fun syncLatest(force: Boolean = false)
}


@EntryPoint
@InstallIn(SingletonComponent::class)
interface HomeSyncEntryPoint {
    fun homeSyncer(): HomeSyncer
}

class HomeSyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                HomeSyncEntryPoint::class.java
            )
            val syncer = entryPoint.homeSyncer()
            // force=false => ton TTL décide si on refresh ou non
            syncer.syncLatest(force = false)
            Log.d("home_sync", "WORKER SUCCESS")
            Result.success()
        } catch (t: Throwable) {
            Log.e("home_sync", "WORKER ERROR", t)
            Result.retry()
        }
    }
}


object HomeSyncScheduler {

    private const val UNIQUE_ONE_SHOT = "home-sync-once"


    fun enqueueOneShot(context: Context) {
        val appCtx = context.applicationContext
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<HomeSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30_000,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .addTag("home-sync")
            .build()

        WorkManager.getInstance(appCtx).enqueueUniqueWork(
            UNIQUE_ONE_SHOT,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

}