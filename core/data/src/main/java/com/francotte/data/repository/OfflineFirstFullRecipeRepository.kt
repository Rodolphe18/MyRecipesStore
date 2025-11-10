package com.francotte.data.repository

import com.francotte.data.mapper.asEntity
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.model.asExternalModel
import com.francotte.model.Recipe
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkRecipe
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

