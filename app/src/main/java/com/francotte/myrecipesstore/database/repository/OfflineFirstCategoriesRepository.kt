package com.francotte.myrecipesstore.database.repository

import com.francotte.myrecipesstore.database.dao.FullCategoryDao
import com.francotte.myrecipesstore.database.model.asExternalModel
import com.francotte.myrecipesstore.domain.model.Category
import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.network.model.NetworkCategory
import com.francotte.myrecipesstore.network.model.asEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstCategoriesRepositoryImpl @Inject constructor(
    private val api: RecipeApi,
    private val dao: FullCategoryDao
) : CategoriesRepository {

    override fun observeAllMealCategories(): Flow<Result<List<Category>>> {
        return flow {
            try {
                val localData = dao.getAllCategories().first()
                if (localData.isEmpty()) {
                    val networkData = api.getAllMealCategories()
                    val entities = networkData.categories.filterIsInstance<NetworkCategory>().map { it.asEntity() }
                    dao.upsertAllCategories(entities)
                }
                emitAll(
                    dao.getAllCategories()
                        .map { list -> Result.success(list.map { it.asExternalModel() }) }
                )
            } catch (e:Exception) {
                emit(Result.failure(e))
            }
        }
    }
}

interface CategoriesRepository {
    fun observeAllMealCategories(): Flow<Result<List<Category>>>
}

