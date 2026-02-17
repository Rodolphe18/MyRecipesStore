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
import androidx.work.workDataOf
import com.francotte.data.mapper.dto.asEntity
import com.francotte.database.dao.FullRecipeDao
import com.francotte.datastore.FoodPreferencesDataRepository
import com.francotte.network.api.FavoriteApi
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkRecipe
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

class FavoritesSyncWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val entryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                FavoritesSyncEntryPoint::class.java,
            )
        val api = entryPoint.favoriteApi()
        val repo = entryPoint.foodPreferencesRepo()

        val user = repo.userData.first()
        val token = user.token

        val reason = inputData.getString(KEY_REASON) ?: REASON_TOGGLE
        val isReasonLogin = reason == REASON_LOGIN

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
            for ((recipeId, desiredFav) in pending) {
                val itemResult = trySyncOne(api, repo, token, recipeId, desiredFav)
                when (itemResult) {
                    ItemSyncResult.Synced -> Unit
                    ItemSyncResult.StopNoLogin -> return Result.failure() // pending conservé
                    ItemSyncResult.Reconcile -> return reconcileFromServer(api, repo, token)
                    ItemSyncResult.Retry -> return Result.retry()
                }
            }
            if (isReasonLogin) {
                val recipeApi = entryPoint.recipeApi()
                val dao = entryPoint.fullRecipeDao()
                val prefetchResult = prefetchMissingFavoriteRecipes(repo, dao, recipeApi)

                // si tu veux être strict : si prefetch network fail => retry
                if (!prefetchResult) return Result.retry()
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
        desiredFav: Boolean,
    ): ItemSyncResult =
        try {
            if (desiredFav) {
                api.addFavorite(recipeId, "Bearer $token")
            } else {
                api.removeFavorite(recipeId, "Bearer $token")
            }

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

    private suspend fun reconcileFromServer(
        api: FavoriteApi,
        repo: FoodPreferencesDataRepository,
        token: String,
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

    private suspend fun prefetchMissingFavoriteRecipes(
        repo: FoodPreferencesDataRepository,
        dao: FullRecipeDao,
        api: RecipeApi,
    ): Boolean {
        val ids = repo.userData.first().favoriteRecipesIds.distinct()
        if (ids.isEmpty()) return true

        // ⚠️ Ça marche même si Room ne stocke pas l'état favori : c'est juste "est-ce que le détail existe"
        val existing = dao.getExistingIds(ids).toSet()
        val missing = ids.filterNot(existing::contains)
        if (missing.isEmpty()) return true

        return try {
            for (idStr in missing) {
                val id = idStr.toLongOrNull() ?: continue
                val network = api.getMealDetail(id)
                    .meals
                    .filterIsInstance<NetworkRecipe>()
                    .firstOrNull() ?: continue

                dao.insertFullRecipe(network.asEntity())
            }
            true
        } catch (_: IOException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private enum class ItemSyncResult {
        Synced,
        StopNoLogin,
        Reconcile,
        Retry,
    }
}

object FavoritesSyncScheduler {

    fun enqueueForToggle(context: Context) {
        enqueue(context, REASON_TOGGLE, ExistingWorkPolicy.KEEP)
    }

    fun enqueueForLogin(context: Context) {
        enqueue(context, REASON_LOGIN, ExistingWorkPolicy.REPLACE)
    }

    private fun enqueue(context: Context,
                        reason: String,
                        policy: ExistingWorkPolicy) {
        val constraints =
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<FavoritesSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30_000,
                    java.util.concurrent.TimeUnit.MILLISECONDS,
                )
                .setInputData(
                    workDataOf(KEY_REASON to reason)
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                "favorites-sync",
                policy,
                request,
            )

    }
}

private const val KEY_REASON = "reason"
private const val REASON_TOGGLE = "toggle"
private const val REASON_LOGIN = "login"

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FavoritesSyncEntryPoint {
    fun favoriteApi(): FavoriteApi
    fun foodPreferencesRepo(): FoodPreferencesDataRepository
    fun recipeApi(): RecipeApi
    fun fullRecipeDao(): FullRecipeDao
}
