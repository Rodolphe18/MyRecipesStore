package com.francotte.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresExtension
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
import retrofit2.HttpException
import java.io.IOException

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
        val repo = entryPoint.foodPreferencesRepo()

        val user = repo.userData.first()
        val token = user.token

        if (!user.isConnected || token.isNullOrBlank()) {
            return Result.failure()
        }

        val pending = repo.getPendingFavorites()
        if (pending.isEmpty()) return Result.success()

        val maxAttemptsBeforeReconcile = 3
        if (runAttemptCount >= maxAttemptsBeforeReconcile) {
            return reconcileFromServer(api, repo, token)
        }

        try {
            // On traite item par item pour éviter qu’un seul item “fatal” bloque tout
            for ((recipeId, desiredFav) in pending) {
                val itemResult = trySyncOne(api, repo, token, recipeId, desiredFav)
                when (itemResult) {
                    ItemSyncResult.Synced -> Unit
                    ItemSyncResult.StopNoLogin -> return Result.failure() // pending conservé
                    ItemSyncResult.Reconcile -> return reconcileFromServer(api, repo, token)
                    ItemSyncResult.Retry -> return Result.retry()
                }
            }

            return Result.success()

        } catch (_: Exception) {
            // Inattendu => retry (tu peux aussi reconcile ici si tu préfères)
            return Result.retry()
        }
    }

    private suspend fun trySyncOne(
        api: FavoriteApi,
        repo: FoodPreferencesDataRepository,
        token: String,
        recipeId: String,
        desiredFav: Boolean
    ): ItemSyncResult {
        return try {
            if (desiredFav) api.addFavorite(recipeId, "Bearer $token")
            else api.removeFavorite(recipeId, "Bearer $token")

            repo.removePendingFavorite(recipeId)
            ItemSyncResult.Synced

        } catch (e: HttpException) {
            when (e.code()) {
                401, 403 -> {
                    // ✅ IMPORTANT : on garde pending, on stoppe (re-login requis)
                    ItemSyncResult.StopNoLogin
                }
                in 400..499 -> {
                    // Erreur définitive => server wins
                    ItemSyncResult.Reconcile
                }
                else -> {
                    // 5xx etc.
                    ItemSyncResult.Retry
                }
            }
        } catch (_: IOException) {
            // Réseau
            ItemSyncResult.Retry
        }
    }

    private suspend fun reconcileFromServer(
        api: FavoriteApi,
        repo: FoodPreferencesDataRepository,
        token: String
    ): Result {
        return try {
            val serverIds = api.getFavoriteRecipeIds("Bearer $token")
            repo.setFavoritesIds(serverIds.toSet())
            repo.clearPendingFavorites()
            Result.success()
        } catch (e: HttpException) {
            return when (e.code()) {
                401, 403 -> {
                    // Pas possible de réconcilier sans login : on garde pending
                    Result.failure()
                }
                else -> Result.retry()
            }
        } catch (_: IOException) {
            Result.retry()
        }
    }

    private enum class ItemSyncResult {
        Synced,
        StopNoLogin,
        Reconcile,
        Retry
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