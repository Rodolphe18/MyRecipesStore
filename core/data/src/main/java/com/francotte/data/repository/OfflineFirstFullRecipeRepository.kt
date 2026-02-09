package com.francotte.data.repository

import com.francotte.data.mapper.dto.asEntity
import com.francotte.data.mapper.entity.asExternalModel
import com.francotte.database.dao.FullRecipeDao
import com.francotte.model.Recipe
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstFullRecipeRepositoryImpl
@Inject
constructor(
    private val api: RecipeApi,
    private val dao: FullRecipeDao,
) : OfflineFirstFullRecipeRepository {

    override fun getRecipeDetail(id: Long): Flow<Recipe> =
        flow {
            val localRecipe = dao.getFullRecipeById(id.toString()).first()
            val lastUpdated = localRecipe?.savedTimestamp
            val now = Instant.now()
            val timeToLive = Duration.ofDays(3)
            if (localRecipe == null || lastUpdated == null || lastUpdated.isBefore(
                    now.minus(
                        timeToLive
                    )
                )
            ) {
                try {
                    val networkRecipe = api
                        .getMealDetail(id)
                        .meals
                        .filterIsInstance<NetworkRecipe>()
                        .firstOrNull()
                    if (networkRecipe != null) {
                        val entity = networkRecipe.asEntity().apply {
                            this.savedTimestamp = now
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
