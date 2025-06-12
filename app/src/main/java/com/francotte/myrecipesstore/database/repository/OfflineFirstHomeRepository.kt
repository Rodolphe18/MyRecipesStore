package com.francotte.myrecipesstore.database.repository

import android.util.Log
import androidx.room.Dao
import com.francotte.myrecipesstore.database.dao.FullRecipeDao
import com.francotte.myrecipesstore.database.dao.LightRecipeDao
import com.francotte.myrecipesstore.database.model.LightRecipeEntity
import com.francotte.myrecipesstore.database.model.asExternalModel
import com.francotte.myrecipesstore.database.sync.Synchronizer
import com.francotte.myrecipesstore.domain.model.LightRecipe
import com.francotte.myrecipesstore.domain.model.Recipe
import com.francotte.myrecipesstore.network.model.NetworkLightRecipe
import com.francotte.myrecipesstore.network.model.asEntity
import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.network.model.NetworkRecipe
import com.francotte.myrecipesstore.util.FoodAreaSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstHomeRepository @Inject constructor(
    private val lightRecipeDao: LightRecipeDao,
    private val fullRecipeDao: FullRecipeDao,
    private val network: RecipeApi,
) : RecipesRepository {


    suspend fun sync(synchronizer: Synchronizer<NetworkLightRecipe, LightRecipeEntity>) {
        synchronizer.sync(
            modelFetcher = {
                val latestMeals = network.getLatestMeals().meals.filterIsInstance<NetworkLightRecipe>()
                val map = mutableMapOf<String, List<NetworkLightRecipe>>()

                enumValues<FoodAreaSection>().forEach { section ->
                    val recipes = network.getRecipesListByArea(section.title).meals
                    map[section.title] = recipes.filterIsInstance<NetworkLightRecipe>()
                }
                val foodAreaList = listOf(
                    FoodAreaSection.AMERICAN.title,
                    FoodAreaSection.CHINESE.title,
                    FoodAreaSection.FRENCH.title,
                    FoodAreaSection.INDIAN.title
                ).flatMap { key ->
                    map[key] ?: emptyList()
                }
                latestMeals + foodAreaList
            } ,
            modelMapper = { it.asEntity() },
            modelSaver = { lightRecipeDao.upsertAllLightRecipes(it) }
        )
    }

    override fun getLatestRecipes(): Flow<List<Recipe>> = flow {
        Log.d("debug_offline_debut1", "")
        val lastUpdated = fullRecipeDao.getLastUpdatedForLatest()
        Log.d("debug_offline_debut1_dao", "")
        val now = System.currentTimeMillis()
        val ttl = 24 * 60 * 60 * 1000 // 24h
        if (lastUpdated == null || now - lastUpdated > ttl) {
            val networkData = network.getLatestMeals().meals as List<NetworkRecipe>
            Log.d("debug_offline_network1", networkData.toString())
            val entities = networkData.map {
                it.asEntity().apply {
                    this.isLatest = true
                    this.lastUpdated = now
                }
            }
            Log.d("debug_offline_entity1", entities.toString())
            fullRecipeDao.deleteOldLatestRecipes()
            fullRecipeDao.upsertAllFullRecipes(entities)
        }
        Log.d("debug_offline_fin1", "")
        emitAll(
            fullRecipeDao.getLatestFullRecipes()
                .map { list ->
                    list.map { it.asExternalModel() } }
        )

    }

    override fun getRecipesListByArea(area: String): Flow<List<LightRecipe>> = flow {
        Log.d("debug_offline_debut2", "")
        val lastUpdated = lightRecipeDao.getLastUpdatedForArea(area)
        Log.d("debug_offline_debut2_dao", "")
        val now = System.currentTimeMillis()
        val ttl = 3 * 24 * 60 * 60 * 1000 // 3 jours
        if (lastUpdated == null || now - lastUpdated > ttl) {

            val networkData =
                network.getRecipesListByArea(area).meals.filterIsInstance<NetworkLightRecipe>()
            Log.d("debug_offline_network2", networkData.toString())
            val entities = networkData.map { it.asEntity().apply {
                this.area = area
                this.lastUpdated = now
            } }
            Log.d("debug_offline_entity2", entities.toString())
            lightRecipeDao.upsertAllLightRecipes(entities)
        }
        Log.d("debug_offline_fin2", "")
        emitAll(
            lightRecipeDao.getLightRecipesByArea(area)
                .map { list -> list.map { it.asExternalModel() } }
        )

    }

    override fun getRecipesByCategory(category: String): Flow<List<LightRecipe>> = flow {
        Log.d("debug_offline_debut3", "")
        val lastUpdated = lightRecipeDao.getLastUpdatedForCategory(category)
        Log.d("debug_offline_debut3_dao", "")
        val now = System.currentTimeMillis()
        val ttl = 3 * 24 * 60 * 60 * 1000 // 3 jours
        if (lastUpdated == null || now - lastUpdated > ttl) {
            val networkData =
                network.getRecipesListByCategory(category).meals.filterIsInstance<NetworkLightRecipe>()
            Log.d("debug_offline_entity3", networkData.toString())
            val entities = networkData.map { it.asEntity().apply {
                this.category = category
                this.lastUpdated = now
            } }
            Log.d("debug_offline_entity3", entities.toString())
            lightRecipeDao.upsertAllLightRecipes(entities)
        }
        Log.d("debug_offline_fin3", "")
        emitAll(
            lightRecipeDao.getLightRecipesByCategory(category)
                .map { list -> list.map { it.asExternalModel() } }
        )

    }


}

interface RecipesRepository {
    fun getLatestRecipes(): Flow<List<Recipe>>
    fun getRecipesListByArea(area: String): Flow<List<LightRecipe>>
    fun getRecipesByCategory(category:String): Flow<List<LightRecipe>>
}