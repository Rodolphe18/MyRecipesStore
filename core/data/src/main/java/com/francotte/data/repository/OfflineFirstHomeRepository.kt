package com.francotte.data.repository

import com.francotte.data.mapper.asEntity
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.database.model.asExternalModel
import com.francotte.model.LightRecipe
import com.francotte.model.Recipe
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkLightRecipe
import com.francotte.network.model.NetworkRecipe
import com.francotte.network.model.asExternalModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstHomeRepository @Inject constructor(
    private val lightRecipeDao: LightRecipeDao,
    private val fullRecipeDao: FullRecipeDao,
    private val network: RecipeApi,
) : RecipesRepository {


    override suspend fun refreshLatestRecipes(force: Boolean) = withContext(Dispatchers.IO) {
        val lastUpdated = fullRecipeDao.getLastUpdatedForLatest()
        val now = System.currentTimeMillis()
        val ttl = 24 * 60 * 60 * 1000

        val shouldRefresh = force || lastUpdated == null || now - lastUpdated > ttl
        if (!shouldRefresh) return@withContext

        val networkData = network.getLatestMeals().meals
            .filterIsInstance<NetworkRecipe>()
            .filter { !it.strMealThumb.isNullOrBlank() }

        val entities = networkData.map {
            it.asEntity().apply {
                this.isLatest = true
                this.lastUpdated = now
            }
        }
        fullRecipeDao.refreshLatest(entities)
    }

    override fun observeLatestRecipes(): Flow<List<Recipe>> =
        fullRecipeDao.getLatestFullRecipes().map { it.map { e -> e.asExternalModel() } }


    override fun getRecipesListByArea(area: String): Flow<List<LightRecipe>> = flow {
        val lastUpdated = lightRecipeDao.getLastUpdatedForArea(area)
        val now = System.currentTimeMillis()
        val ttl = 3 * 24 * 60 * 60 * 1000 // 3 jours
        if (lastUpdated == null || now - lastUpdated > ttl) {
            val networkData = network.getRecipesListByArea(area).meals.filterIsInstance<NetworkLightRecipe>()
            val entities = networkData.map {
                it.asEntity().apply {
                    this.area = area
                    this.lastUpdated = now
                }
            }
            lightRecipeDao.upsertAllLightRecipes(entities)
        }
        emitAll(
            lightRecipeDao.getLightRecipesByArea(area)
                .map { list -> list.map { it.asExternalModel() } }
        )
    }.flowOn(Dispatchers.IO)

    override fun getRecipesByCategory(category: String): Flow<List<LightRecipe>> = flow {
        val lastUpdated = lightRecipeDao.getLastUpdatedForCategory(category)
        val now = System.currentTimeMillis()
        val ttl = 3 * 24 * 60 * 60 * 1000 // 3 jours
        if (lastUpdated == null || now - lastUpdated > ttl) {
            val networkData = network.getRecipesListByCategory(category).meals.filterIsInstance<NetworkLightRecipe>()
            val entities = networkData.map {
                it.asEntity().apply {
                    this.category = category
                    this.lastUpdated = now
                }
            }
            lightRecipeDao.upsertAllLightRecipes(entities)
        }
        emitAll(
            lightRecipeDao.getLightRecipesByCategory(category)
                .map { list -> list.map { it.asExternalModel() } }
        )
    }.flowOn(Dispatchers.IO)

    override fun getRecipesByIngredients(ingredients: List<String>): Flow<List<LightRecipe>> = flow {
        try {
            val networkData = network.getRecipesListByMultiIngredients(ingredients.joinToString()).meals.filterIsInstance<NetworkLightRecipe>()
            emit(networkData.map { it.asExternalModel() })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)


}

interface RecipesRepository {
    fun observeLatestRecipes(): Flow<List<Recipe>>
    suspend fun refreshLatestRecipes(force: Boolean = false)
    fun getRecipesListByArea(area: String): Flow<List<LightRecipe>>
    fun getRecipesByCategory(category: String): Flow<List<LightRecipe>>
    fun getRecipesByIngredients(ingredients:List<String>): Flow<List<LightRecipe>>
}