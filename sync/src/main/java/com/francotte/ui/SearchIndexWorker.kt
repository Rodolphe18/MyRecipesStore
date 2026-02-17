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
import com.francotte.common.utils.DataResult
import com.francotte.data.mapper.dto.asEntity
import com.francotte.database.dao.LightRecipeDao
import com.francotte.database.dao.fts.RecipeFtsDao
import com.francotte.database.dao.fts.SearchIndexStateDao
import com.francotte.database.model.LightRecipeEntity
import com.francotte.database.model.SearchIndexCategoryStateEntity
import com.francotte.database.model.asFtsEntity
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkLightRecipe
import com.francotte.network.utils.safeNetworkCall
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import java.time.Duration
import java.time.Instant


class SearchIndexWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        val maxAttempts = 3
        if (runAttemptCount >= maxAttempts) return Result.failure()

        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SearchIndexEntryPoint::class.java
            )

            val api = entryPoint.recipeApi()
            val stateDao = entryPoint.searchIndexStateDao()
            val lightRecipeDao = entryPoint.lightRecipeDao()
            val recipeFtsDao = entryPoint.recipeFtsDao()

            val now = Instant.now()
            val ttl = Duration.ofDays(21)
            val staleBefore = now.minus(ttl)
            val batchSize = 5
            Log.d("debug_worker_search", "")
            val categories = stateDao.getCategoriesToIndex(staleBefore=staleBefore,limit = batchSize)
            Log.d("debug_worker_search_cat", categories.joinToString())
            if (categories.isEmpty()) return Result.retry()

            val allLightRecipes = mutableListOf<LightRecipeEntity>()
            val successfullyIndexed = mutableListOf<String>()

            for (category in categories) {
                val networkResult = safeNetworkCall(Dispatchers.IO) {
                    api.getRecipesListByCategory(category).meals.filterIsInstance<NetworkLightRecipe>()
                }
                Log.d("debug_worker_search", category)
                when (networkResult) {
                    is DataResult.Failure -> return Result.retry()
                    is DataResult.Success -> {
                        val networkRecipes = networkResult.data
                        val entities = networkRecipes.map { it.asEntity() }
                        allLightRecipes += entities
                        successfullyIndexed += category
                    }
                }
            }
            if (allLightRecipes.isNotEmpty()) {
                Log.d("debug_worker_search_all_light_recipes", allLightRecipes.joinToString { "," })
                lightRecipeDao.upsertLightRecipes(allLightRecipes)
                recipeFtsDao.insertAll(allLightRecipes.map { it.asFtsEntity() })
            }

            if (successfullyIndexed.isNotEmpty()) {
                Log.d("debug_worker_search_upsert_states", "upsertStates")
                stateDao.upsertStates(
                    successfullyIndexed.map { category ->
                        SearchIndexCategoryStateEntity(
                            strCategory = category,
                            lastIndexedAt = now
                        )
                    }
                )
            }
            val remaining = stateDao.getRemainingToIndexCount(staleBefore)
            Log.d("debug_worker_search_remaining_1", remaining.toString())

            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}

object SearchIndexScheduler {
    private const val UNIQUE_CHAIN = "search-index-chain"

    fun enqueueIndexChain(context: Context, runs: Int = 3) {
        val appCtx = context.applicationContext

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun newRequest() =
            OneTimeWorkRequestBuilder<SearchIndexWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10_000,
                    java.util.concurrent.TimeUnit.MILLISECONDS,
                )
                .addTag("search-index")
                .build()

        var continuation = WorkManager.getInstance(appCtx)
            .beginUniqueWork(UNIQUE_CHAIN, ExistingWorkPolicy.KEEP, newRequest())

        repeat(runs - 1) {
            continuation = continuation.then(newRequest())
        }

        continuation.enqueue()
    }
}
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SearchIndexEntryPoint {
    fun recipeApi(): RecipeApi
    fun searchIndexStateDao(): SearchIndexStateDao
    fun lightRecipeDao(): LightRecipeDao
    fun recipeFtsDao(): RecipeFtsDao
}
