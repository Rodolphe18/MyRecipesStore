package com.francotte.data.repository

import com.francotte.common.utils.DataResult
import com.francotte.common.utils.userMessage
import com.francotte.data.interfaces.CategoriesRepository
import com.francotte.data.mapper.dto.asEntity
import com.francotte.data.mapper.entity.asExternalModel
import com.francotte.database.dao.FullCategoryDao
import com.francotte.model.Category
import com.francotte.network.api.RecipeApi
import com.francotte.network.model.NetworkCategory
import com.francotte.network.utils.safeNetworkCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class OfflineFirstCategoriesRepositoryImpl @Inject constructor(
    private val api: RecipeApi, private val dao: FullCategoryDao,
) : CategoriesRepository {

    override fun observeAllMealCategories(): Flow<List<Category>> =
        dao.getAllCategories().map { categories -> categories.map { it.asExternalModel() } }


    override suspend fun refreshAllMealCategories(force: Boolean): String? {

        val lastUpdate = dao.getLastUpdateForCategories()
        val now = Instant.now()
        val timeToLive = Duration.ofDays(7)

        if (!force && lastUpdate != null) {
            val age = Duration.between(lastUpdate, now)
            if (age < timeToLive) return null
        }

        val networkCategories =
            safeNetworkCall(Dispatchers.IO) { api.getAllMealCategories().categories }

        val categories = when (networkCategories) {
            is DataResult.Failure -> return networkCategories.error.userMessage()
            is DataResult.Success -> networkCategories.data
        }
        val categoriesEntity = categories.map { (it as NetworkCategory).asEntity(now) }
        dao.upsertAllCategories(categoriesEntity)
        return "Synced successfully"
    }


}


