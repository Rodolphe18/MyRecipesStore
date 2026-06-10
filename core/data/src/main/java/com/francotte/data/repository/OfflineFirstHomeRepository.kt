package com.francotte.data.repository

import android.util.Log
import com.francotte.common.utils.DataResult
import com.francotte.common.utils.userMessage
import com.francotte.data.interfaces.HomeRepository
import com.francotte.data.mapper.dto.asEntity
import com.francotte.data.mapper.entity.asExternalModel
import com.francotte.database.crossrefs.RecipeAreaCrossRef
import com.francotte.database.crossrefs.RecipeCategoryCrossRef
import com.francotte.database.crossrefs.RecipeIngredientCrossRef
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.model.LightRecipe
import com.francotte.model.Recipe
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkLightRecipe
import com.francotte.network.model.NetworkRecipe
import com.francotte.network.model.asExternalModel as networkAsExternalModel
import com.francotte.network.utils.safeNetworkCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import javax.inject.Inject


class OfflineFirstHomeRepository @Inject constructor(
    private val lightRecipeDao: LightRecipeDao,
    private val fullRecipeDao: FullRecipeDao,
    private val network: RecipeApi,
) : HomeRepository {
    override suspend fun refreshLatestRecipes(force: Boolean): String? {
        val lastUpdatedResult = fullRecipeDao.getLastUpdatedForLatest()
        Log.d("debug_latest_0", lastUpdatedResult.toString())
        val now = Instant.now()
        val ttl = Duration.ofDays(3)
        Log.d("debug_latest_1", "")
        val shouldRefresh = force || lastUpdatedResult == null || Duration.between(lastUpdatedResult, now) > ttl
        if (!shouldRefresh) return null
        Log.d("debug_latest_2", shouldRefresh.toString())
        val networkResult = safeNetworkCall(Dispatchers.IO) { network.getLatestMeals() }
        Log.d("debug_latest_3", networkResult.toString())
        val response = when (networkResult) {
            is DataResult.Success -> networkResult.data
            is DataResult.Failure -> return networkResult.error.userMessage()
        }

        Log.d("debug_latest_4", response.meals.toString())

        val networkData = response
            .meals
            .filterIsInstance<NetworkRecipe>()
            .filter { !it.strMealThumb.isNullOrBlank() }

        Log.d("debug_latest_4", networkData.toString())
        if (networkData.isEmpty()) return null

        val entities = networkData.map {
            it.asEntity().apply {
                this.isLatest = true
                this.savedTimestamp = now
            }
        }

        fullRecipeDao.refreshLatest(entities)
        return "synced successfully"
    }


    override fun observeLatestRecipes(): Flow<List<Recipe>> {
        return fullRecipeDao.getLatestFullRecipes()
            .map { entities ->
                entities.map { it.asExternalModel() }
            }
    }


    override suspend fun refreshRecipesListByArea(area: String, force: Boolean): String? {
        val now = Instant.now()
        val ttl = Duration.ofDays(3)

        val lastUpdated = lightRecipeDao.getLastUpdatedForArea(area)
        val shouldRefresh = force || lastUpdated == null || Duration.between(lastUpdated, now) > ttl
        if (!shouldRefresh) return null

        val result = safeNetworkCall(Dispatchers.IO) {
            network.getRecipesListByArea(area).meals.filterIsInstance<NetworkLightRecipe>()
        }
        Log.d("debug_area", result.toString())
        val networkRecipes = when (result) {
            is DataResult.Failure -> return result.error.userMessage()
            is DataResult.Success -> result.data
        }

        val recipes = networkRecipes.map { it.asEntity() }
        val refs = networkRecipes.map { recipe ->
            RecipeAreaCrossRef(strArea = area, idMeal = recipe.idMeal, savedTimestamp = now)
        }

        lightRecipeDao.upsertAreaWithRecipes(area, recipes, refs, clearBefore = true)
        return "Synced successfully"
    }

    override fun observeRecipesListByArea(area: String): Flow<List<LightRecipe>> {
        return lightRecipeDao.observeAreaWithRecipes(area).map { areaWithRecipes ->
            val recipes = areaWithRecipes?.recipes ?: emptyList()
            recipes.map { it.asExternalModel() }
        }
    }


    override suspend fun refreshRecipesByCategory(category: String, force: Boolean): Boolean {
        val lastUpdated = lightRecipeDao.getLastUpdatedForCategory(category)
        val now = Instant.now()
        val ttl = Duration.ofDays(3)
        val shouldRefresh = force || lastUpdated == null || Duration.between(lastUpdated, now) > ttl
        if (!shouldRefresh) return false
        val networkData = safeNetworkCall(Dispatchers.IO) {
            network.getRecipesListByCategory(category).meals.filterIsInstance<NetworkLightRecipe>()
        }

        val networkLightRecipes = when (networkData) {
            is DataResult.Failure -> return false
            is DataResult.Success -> networkData.data
        }
        val entities = networkLightRecipes.map { it.asEntity() }
        val refs = networkLightRecipes.map {
            RecipeCategoryCrossRef(category, it.idMeal, now)
        }
        lightRecipeDao.upsertCategoryWithRecipes(category, entities, refs, true)
        return true
    }

    override fun observeRecipesByCategory(category: String): Flow<List<LightRecipe>?> {
        return lightRecipeDao.observeCategoryWithRecipes(category).map { categoryWithRecipes ->
            categoryWithRecipes?.recipes?.map { it.asExternalModel() } ?: emptyList()
        }
    }

    override suspend fun getRecipesByCategory(category: String): DataResult<List<LightRecipe>> =
        safeNetworkCall(Dispatchers.IO) {
            network.getRecipesListByCategory(category).meals
                .filterIsInstance<NetworkLightRecipe>()
                .map { it.networkAsExternalModel() }
        }

    override suspend fun getRecipesByArea(area: String): DataResult<List<LightRecipe>> =
        safeNetworkCall(Dispatchers.IO) {
            network.getRecipesListByArea(area).meals
                .filterIsInstance<NetworkLightRecipe>()
                .map { it.networkAsExternalModel() }
        }

    override suspend fun getRecipesByIngredients(ingredients: List<String>): DataResult<List<LightRecipe>> {
        val ingredient = ingredients.firstOrNull() ?: return DataResult.Success(emptyList())
        return safeNetworkCall(Dispatchers.IO) {
            network.getRecipesListByMultiIngredients(ingredient).meals
                .filterIsInstance<NetworkLightRecipe>()
                .map { it.networkAsExternalModel() }
        }
    }

    override suspend fun refreshRecipesByIngredients(
        ingredients: List<String>,
        force: Boolean
    ): String? {
        val ingredient = ingredients.firstOrNull() ?: return null
        val lastUpdated = lightRecipeDao.getLastUpdatedForIngredientRecipes(ingredient)
        val now = Instant.now()
        val ttl = Duration.ofDays(3)
        val shouldRefresh = force || lastUpdated == null || Duration.between(lastUpdated, now) > ttl
        if (!shouldRefresh) return null
        val networkData = safeNetworkCall(Dispatchers.IO) {
            network.getRecipesListByMultiIngredients(ingredient).meals.filterIsInstance<NetworkLightRecipe>()
        }
        val networkRecipes = when (networkData) {
            is DataResult.Failure -> return networkData.error.userMessage()
            is DataResult.Success -> networkData.data
        }
        val entities = networkRecipes.map { it.asEntity() }
        val refs = networkRecipes.map {
            RecipeIngredientCrossRef(ingredient, it.idMeal, now)
        }
        lightRecipeDao.upsertIngredientWithRecipes(ingredient, entities, refs, true)
        return "Synced successfully"
    }

    override fun observeRecipesByIngredients(ingredients: List<String>): Flow<List<LightRecipe>> {
        val ingredient = ingredients.firstOrNull() ?: return flowOf(emptyList())
        return lightRecipeDao.observeIngredientWithRecipes(ingredient)
            .map { ingredientWithRecipes ->
                ingredientWithRecipes.recipes.map { it.asExternalModel() }
            }
    }
}

