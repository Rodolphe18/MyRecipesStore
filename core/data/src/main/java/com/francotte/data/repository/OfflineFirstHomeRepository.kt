package com.francotte.data.repository

import com.francotte.common.utils.DataResult
import com.francotte.data.mapper.dto.asEntity
import com.francotte.data.mapper.entity.asExternalModel
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.database.util.safeDbCall
import com.francotte.model.LightRecipe
import com.francotte.model.Recipe
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkLightRecipe
import com.francotte.network.model.NetworkRecipe
import com.francotte.network.model.asExternalModel
import com.francotte.network.utils.safeNetworkCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import javax.inject.Inject


sealed interface SyncOutcome {
    data object Skipped : SyncOutcome
    data class Success(val inserted: Int) : SyncOutcome
}

class OfflineFirstHomeRepository @Inject constructor(
    private val lightRecipeDao: LightRecipeDao,
    private val fullRecipeDao: FullRecipeDao,
    private val network: RecipeApi,
) : RecipesRepository {
    override suspend fun refreshLatestRecipes(force: Boolean): DataResult<SyncOutcome> {
        val lastUpdatedResult = safeDbCall { fullRecipeDao.getLastUpdatedForLatest() }
        val lastUpdated = when (lastUpdatedResult) {
            is DataResult.Success -> lastUpdatedResult.data
            is DataResult.Failure -> return lastUpdatedResult
        }

        val now = Instant.now()
        val ttl = Duration.ofDays(3)

        val shouldRefresh = force || lastUpdated == null || lastUpdated.isBefore(now.minus(ttl))
        if (!shouldRefresh) return DataResult.Success(SyncOutcome.Skipped)

        val networkResult = safeNetworkCall(Dispatchers.IO) { network.getLatestMeals() }
        val response = when (networkResult) {
            is DataResult.Success -> networkResult.data
            is DataResult.Failure -> return networkResult
        }

        val networkData = response
            .meals
            .filterIsInstance<NetworkRecipe>()
            .filter { !it.strMealThumb.isNullOrBlank() }


        if (networkData.isEmpty()) return DataResult.Success(SyncOutcome.Skipped)

        val entities = networkData.map {
            it.asEntity().apply {
                this.isLatest = true
                this.savedTimestamp = now
            }
        }

        val writeResult = safeDbCall {
            fullRecipeDao.refreshLatest(entities)
            SyncOutcome.Success(inserted = entities.size)
        }

        return writeResult
    }

    override fun observeLatestRecipes(): Flow<List<Recipe>> =
        fullRecipeDao.getLatestFullRecipes().map { it.map { e -> e.asExternalModel() } }

    override fun getRecipesListByArea(area: String): Flow<List<LightRecipe>> =
        flow {
            val lastUpdated = lightRecipeDao.getLastUpdatedForArea(area)
            val now = Instant.now()
            val ttl = Duration.ofDays(3)
            if (lastUpdated == null || lastUpdated.isBefore(now.minus(ttl))) {
                val networkData =
                    network.getRecipesListByArea(area).meals.filterIsInstance<NetworkLightRecipe>()
                val entities =
                    networkData.map {
                        it.asEntity().apply {
                            this.area = area
                            this.savedTimestamp = now
                        }
                    }
                lightRecipeDao.upsertAllLightRecipes(entities)
            }
            emitAll(
                lightRecipeDao
                    .getLightRecipesByArea(area)
                    .map { list -> list.map { it.asExternalModel() } },
            )
        }.flowOn(Dispatchers.IO)

    override fun getRecipesByCategory(category: String): Flow<List<LightRecipe>> =
        flow {
            val lastUpdated = lightRecipeDao.getLastUpdatedForCategory(category)
            val now = Instant.now()
            val ttl = Duration.ofDays(3)
            if (lastUpdated == null || lastUpdated.isBefore(now.minus(ttl))) {
                val networkData =
                    network.getRecipesListByCategory(category).meals.filterIsInstance<NetworkLightRecipe>()
                val entities =
                    networkData.map {
                        it.asEntity().apply {
                            this.category = category
                            this.savedTimestamp = now
                        }
                    }
                lightRecipeDao.upsertAllLightRecipes(entities)
            }
            emitAll(
                lightRecipeDao
                    .getLightRecipesByCategory(category)
                    .map { list -> list.map { it.asExternalModel() } },
            )
        }.flowOn(Dispatchers.IO)

    override fun getRecipesByIngredients(ingredients: List<String>): Flow<List<LightRecipe>> =
        flow {
            try {
                val networkData =
                    network
                        .getRecipesListByMultiIngredients(
                            ingredients.joinToString(),
                        ).meals
                        .filterIsInstance<NetworkLightRecipe>()
                emit(networkData.map { it.asExternalModel() })
            } catch (e: Exception) {
                emit(emptyList())
            }
        }.flowOn(Dispatchers.IO)
}

interface RecipesRepository {
    fun observeLatestRecipes(): Flow<List<Recipe>>

    suspend fun refreshLatestRecipes(force: Boolean = false): DataResult<SyncOutcome>

    fun getRecipesListByArea(area: String): Flow<List<LightRecipe>>

    fun getRecipesByCategory(category: String): Flow<List<LightRecipe>>

    fun getRecipesByIngredients(ingredients: List<String>): Flow<List<LightRecipe>>
}
