package com.francotte.ui

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.francotte.datastore.FoodPreferencesDataRepository
import com.francotte.network.api.FavoriteApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class FavoritesSyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            FavoritesSyncEntryPoint::class.java
        )
        val api = entryPoint.favoriteApi()
        val dataRepo = entryPoint.foodPreferencesRepo()

        val user = dataRepo.userData.first()
        val token = user.token
        if (token.isNullOrBlank()) return Result.failure()

        val pending = dataRepo.getPendingFavorites()
        if (pending.isEmpty()) return Result.success()

        return try {
            for ((recipeId, desiredFav) in pending) {
                if (desiredFav) api.addFavorite(recipeId, "Bearer $token")
                else api.removeFavorite(recipeId, "Bearer $token")
                dataRepo.removePendingFavorite(recipeId)
            }
            Log.d("debug_worker_fav", "FavoritesSyncWorker2")
            Result.success()
        } catch (e: Exception) {
            Log.d("debug_worker_fav", "FavoritesSyncWorker3 ${e.message}")
            Result.retry()
        }
    }
}

object FavoritesSyncScheduler {
    fun enqueue(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<FavoritesSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30_000,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
            "favorites-sync",
            ExistingWorkPolicy.KEEP,
            request
        )

    //        val wm = WorkManager.getInstance(context.applicationContext)
//        wm.getWorkInfosForUniqueWorkLiveData("favorites-sync")
//            .observeForever { infos ->
//                infos.forEach { info ->
//                    Log.d(
//                        "fav_sync",
//                        "state=${info.state} attempt=${info.runAttemptCount} " +
//                                "tags=${info.tags} id=${info.id}"
//                    )
//                }
//            }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FavoritesSyncEntryPoint {
    fun favoriteApi(): FavoriteApi
    fun foodPreferencesRepo(): FoodPreferencesDataRepository
}