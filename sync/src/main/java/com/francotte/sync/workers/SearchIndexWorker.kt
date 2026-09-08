package com.francotte.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.francotte.common.extension.Dispatcher
import com.francotte.common.extension.FoodDispatchers.IO
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Duration
import java.time.Instant

/** A category is re-indexed once its last indexing is older than this. */
private val INDEX_TTL: Duration = Duration.ofDays(21)

/** Categories indexed per run. The scheduler chains several runs to cover the catalogue. */
private const val BATCH_SIZE = 5

/**
 * Fills the full text search index one batch of stale categories at a time.
 *
 * Progress is recorded per category, so an interrupted run never re-downloads what it already
 * indexed: the next run simply picks up the categories still older than [INDEX_TTL].
 */
@HiltWorker
internal class SearchIndexWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recipeApi: RecipeApi,
    private val searchIndexStateDao: SearchIndexStateDao,
    private val lightRecipeDao: LightRecipeDao,
    private val recipeFtsDao: RecipeFtsDao,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        runSyncCatching {
            val now = Instant.now()
            val categories = searchIndexStateDao.getCategoriesToIndex(
                staleBefore = now.minus(INDEX_TTL),
                limit = BATCH_SIZE,
            )
            // Nothing stale left: the index is up to date, which is a success, not a retry.
            if (categories.isEmpty()) return@runSyncCatching

            val fetchedRecipes = mutableListOf<LightRecipeEntity>()
            val fetchedCategories = mutableListOf<String>()
            var failedCategory: String? = null

            for (category in categories) {
                val result = safeNetworkCall(ioDispatcher) {
                    recipeApi.getRecipesListByCategory(category)
                        .meals
                        .filterIsInstance<NetworkLightRecipe>()
                }
                when (result) {
                    is DataResult.Failure -> {
                        failedCategory = category
                        break
                    }

                    is DataResult.Success -> {
                        fetchedRecipes += result.data.map { it.asEntity() }
                        fetchedCategories += category
                    }
                }
            }

            // Persist what did succeed before deciding on the outcome, so a retry resumes instead
            // of starting the batch over.
            if (fetchedRecipes.isNotEmpty()) {
                lightRecipeDao.upsertLightRecipes(fetchedRecipes)
                recipeFtsDao.insertAll(fetchedRecipes.map(LightRecipeEntity::asFtsEntity))
            }
            if (fetchedCategories.isNotEmpty()) {
                searchIndexStateDao.upsertStates(
                    fetchedCategories.map { category ->
                        SearchIndexCategoryStateEntity(
                            strCategory = category,
                            lastIndexedAt = now,
                        )
                    },
                )
            }

            if (failedCategory != null) {
                throw IOException("Failed to index category $failedCategory")
            }
        }
    }
}
