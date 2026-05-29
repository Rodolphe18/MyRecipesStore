package com.francotte.data.repository

import com.francotte.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
    fun observeAllMealCategories(): Flow<List<Category>>
    suspend fun refreshAllMealCategories(force: Boolean): String?
}
