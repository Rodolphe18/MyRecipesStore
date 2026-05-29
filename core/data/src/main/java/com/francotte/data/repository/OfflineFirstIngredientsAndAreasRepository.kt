package com.francotte.data.repository

import com.francotte.common.utils.DataResult
import com.francotte.common.utils.userMessage
import com.francotte.data.mapper.dto.asEntity
import com.francotte.data.mapper.entity.asExternalModel
import com.francotte.database.dao.AreaDao
import com.francotte.database.dao.IngredientDao
import com.francotte.datastore.UserDataRepository
import com.francotte.model.LikeableRecipe
import com.francotte.model.mapToLikeableLightRecipes
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkIngredient
import com.francotte.network.utils.safeNetworkCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

class OfflineFirstIngredientsAndAreasRepositoryImpl @Inject constructor(
    private val homeRepository: OfflineFirstHomeRepository,
    private val recipeApi: RecipeApi,
    private val ingredientDao: IngredientDao,
    private val areasDao: AreaDao,
    private val userDataRepository: UserDataRepository,
) : IngredientsAndAreasRepository {


    override fun observeAllIngredients(): Flow<List<String>> =
        ingredientDao.observeIngredients().map { ingredientsEntities ->
            ingredientsEntities.map { it.asExternalModel().name }
        }


    override fun observeAllAreas(): Flow<List<String>> =
        areasDao.observeALlAreas().map { areas -> areas.map { it.asExternalModel().name } }

    override suspend fun refreshAllIngredients(force: Boolean): String? {

        val now = Instant.now()
        val lastUpdate = ingredientDao.getLastUpdatedForIngredients()
        val ttl = Duration.ofDays(3)
        if (!force && lastUpdate != null) {
            val age = Duration.between(lastUpdate, now)
            if (age < ttl) return null
        }

        val networkIngredients = safeNetworkCall(Dispatchers.IO) {
            recipeApi.getAllIngredients().ingredients
        }
        val ingredients = when (networkIngredients) {
            is DataResult.Failure -> return networkIngredients.error.userMessage()
            is DataResult.Success -> networkIngredients.data
        }
        if (ingredients.isEmpty()) return null
        val ids = ingredientDao.insertIngredients(ingredients.map(NetworkIngredient::asEntity))
        val insertedCount = ids.count { it != -1L }
        return when {
            insertedCount == 0 -> "Already up to date."
            else -> "Synced $insertedCount new ingredient(s)."
        }


    }

    override suspend fun refreshAllAreas(force: Boolean): String? {

        val now = Instant.now()
        val lastUpdate = ingredientDao.getLastUpdatedForIngredients()
        val ttl = Duration.ofDays(3)
        if (!force && lastUpdate != null) {
            val age = Duration.between(lastUpdate, now)
            if (age < ttl) return null
        }
        val networkAreas = safeNetworkCall(Dispatchers.IO) {
            recipeApi.getAllAreas().areas
        }

        val areas = when (networkAreas) {
            is DataResult.Failure -> return networkAreas.error.userMessage()
            is DataResult.Success -> networkAreas.data
        }
        if (areas.isEmpty()) return null

        val ids = areasDao.insertAllAreas(areas.map { it.asEntity() })

        val newIdsCount = ids.count { it != -1L }
        return when {
            newIdsCount == 0 -> "Already up to date"
            else -> "Synced $newIdsCount new areas"
        }

    }

    override fun observeRecipesByArea(area: String): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            homeRepository.observeRecipesListByArea(area),
        ) { userData, recipes ->
            val likeable = recipes.mapToLikeableLightRecipes(userData)
                Result.success(likeable)
            }.catch {
                emit(Result.failure(it))
        }

    override suspend fun refreshRecipesByArea(area: String,force:Boolean): String? {
       return homeRepository.refreshRecipesListByArea(area,force)
    }


    override suspend fun refreshRecipesByIngredients(ingredients: List<String>,force: Boolean): String? {
       return homeRepository.refreshRecipesByIngredients(ingredients,force)
    }

    override fun observeRecipesByIngredients(ingredients: List<String>): Flow<Result<List<LikeableRecipe>>> =
        combine(
            userDataRepository.userData,
            homeRepository.observeRecipesByIngredients(ingredients),
        ) { userData, recipes ->
            try {
                val likeable = recipes.mapToLikeableLightRecipes(userData)
                Result.success(likeable)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

}
