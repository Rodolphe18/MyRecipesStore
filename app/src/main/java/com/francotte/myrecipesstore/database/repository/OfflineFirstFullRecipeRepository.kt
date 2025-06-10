package com.francotte.myrecipesstore.database.repository

import com.francotte.myrecipesstore.database.dao.FullRecipeDao
import com.francotte.myrecipesstore.database.model.asExternalModel
import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.domain.model.Recipe
import com.francotte.myrecipesstore.network.model.NetworkRecipe
import com.francotte.myrecipesstore.network.model.asEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class OfflineFirstFullRecipeRepositoryImpl @Inject constructor(
    private val api: RecipeApi,
    private val dao: FullRecipeDao
): OfflineFirstFullRecipeRepository {

    override fun getRecipeDetail(id: Long): Flow<Recipe> = flow {
        val localRecipe = dao.getFullRecipeById(id.toString()).first()
        val lastUpdated = dao.getFullRecipeLastUpdated(id.toString())
        val now = System.currentTimeMillis()
        val ttl = 3 * 24 * 60 * 60 * 1000 // 3 jours
        if (localRecipe == null || lastUpdated == null || now - lastUpdated > ttl) {
            try {
                val networkRecipe = api.getMealDetail(id)
                    .meals
                    .filterIsInstance<NetworkRecipe>()
                    .firstOrNull()

                if (networkRecipe != null) {
                    val entity = networkRecipe.asEntity().apply {
                        this.lastUpdated = now
                    }
                    dao.insertFullRecipe(entity)
                }
            } catch (e: Exception) {
                // Facultatif : log ou gérer les erreurs réseau
            }
        }

        val finalRecipe = dao.getFullRecipeById(id.toString()).first()
        if (finalRecipe != null) {
            emit(finalRecipe.asExternalModel())
        }
    }
}


interface OfflineFirstFullRecipeRepository {
    fun getRecipeDetail(id: Long): Flow<Recipe>
}

