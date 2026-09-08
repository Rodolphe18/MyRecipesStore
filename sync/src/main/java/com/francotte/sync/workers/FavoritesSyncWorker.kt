package com.francotte.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.francotte.common.extension.Dispatcher
import com.francotte.common.extension.FoodDispatchers.IO
import com.francotte.data.mapper.dto.asEntity
import com.francotte.data.sync.FavoritesSyncReason
import com.francotte.database.dao.FullRecipeDao
import com.francotte.datastore.FoodPreferencesDataSource
import com.francotte.network.api.FavoriteApi
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/** Input data key carrying the [FavoritesSyncReason] name. */
internal const val KEY_FAVORITES_REASON = "favorites_reason"

/**
 * After this many failed attempts the local pending list is considered unreliable and the server
 * state wins instead of being pushed again.
 */
private const val MAX_ATTEMPTS_BEFORE_RECONCILE = 3

/**
 * Pushes the favorites the user changed while offline, then reconciles with the server.
 *
 * Toggling a favorite writes it locally and adds it to a pending list; this worker drains that
 * list. On login it also reconciles the whole list and prefetches the recipe details that are
 * missing locally, so the favorites screen works offline right away.
 */
@HiltWorker
internal class FavoritesSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val favoriteApi: FavoriteApi,
    private val recipeApi: RecipeApi,
    private val preferences: FoodPreferencesDataSource,
    private val fullRecipeDao: FullRecipeDao,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        val user = preferences.userData.first()
        val token = user.token

        // Nothing can be pushed without credentials. The pending list is kept: the next login
        // enqueues this worker again and drains it then.
        if (!user.isConnected || token.isNullOrBlank()) return@withContext Result.failure()

        val pending = preferences.getPendingFavorites()
        if (pending.isEmpty()) return@withContext Result.success()

        // Repeated failures mean the pending list cannot be pushed as is; stop guessing and take
        // the server as the source of truth.
        if (runAttemptCount >= MAX_ATTEMPTS_BEFORE_RECONCILE) {
            return@withContext reconcileFromServer(token)
        }

        try {
            for ((recipeId, desiredFavorite) in pending) {
                when (trySyncOne(token, recipeId, desiredFavorite)) {
                    ItemSyncResult.Synced -> Unit
                    // Pending entries are kept so a later login can retry them.
                    ItemSyncResult.StopNoLogin -> return@withContext Result.failure()
                    ItemSyncResult.Reconcile -> return@withContext reconcileFromServer(token)
                    ItemSyncResult.Retry -> return@withContext Result.retry()
                }
            }

            val reason = inputData.getString(KEY_FAVORITES_REASON)
                ?.let { name -> FavoritesSyncReason.entries.firstOrNull { it.name == name } }
                ?: FavoritesSyncReason.Toggle

            if (reason == FavoritesSyncReason.Login && !prefetchMissingFavoriteRecipes()) {
                return@withContext Result.retry()
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun trySyncOne(
        token: String,
        recipeId: String,
        desiredFavorite: Boolean,
    ): ItemSyncResult = try {
        if (desiredFavorite) {
            favoriteApi.addFavorite(recipeId, "Bearer $token")
        } else {
            favoriteApi.removeFavorite(recipeId, "Bearer $token")
        }
        preferences.removePendingFavorite(recipeId)
        ItemSyncResult.Synced
    } catch (e: HttpException) {
        when (e.code()) {
            // Credentials no longer valid: keep the pending entries for the next login.
            401, 403 -> ItemSyncResult.StopNoLogin
            // Any other client error is definitive, so the server state wins.
            in 400..499 -> ItemSyncResult.Reconcile
            else -> ItemSyncResult.Retry
        }
    } catch (_: IOException) {
        ItemSyncResult.Retry
    }

    /** Drops the local pending changes and adopts the server's favorites list. */
    private suspend fun reconcileFromServer(token: String): Result = try {
        val serverIds = favoriteApi.getFavoriteRecipeIds("Bearer $token")
        preferences.setFavoritesIds(serverIds.toSet())
        preferences.clearPendingFavorites()
        Result.success()
    } catch (e: HttpException) {
        when (e.code()) {
            401, 403 -> Result.failure()
            else -> Result.retry()
        }
    } catch (_: IOException) {
        Result.retry()
    }

    /**
     * Downloads the details of favorited recipes that are not in the database yet, so they are
     * readable offline. Returns false if the network got in the way.
     */
    private suspend fun prefetchMissingFavoriteRecipes(): Boolean {
        val ids = preferences.userData.first().favoriteRecipesIds.distinct()
        if (ids.isEmpty()) return true

        val existing = fullRecipeDao.getExistingIds(ids).toSet()
        val missing = ids.filterNot(existing::contains)
        if (missing.isEmpty()) return true

        return try {
            missing.forEach { idAsString ->
                val id = idAsString.toLongOrNull() ?: return@forEach
                val recipe = recipeApi.getMealDetail(id)
                    .meals
                    .filterIsInstance<NetworkRecipe>()
                    .firstOrNull() ?: return@forEach

                fullRecipeDao.insertFullRecipe(recipe.asEntity())
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private enum class ItemSyncResult { Synced, StopNoLogin, Reconcile, Retry }
}
