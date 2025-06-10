package com.francotte.myrecipesstore.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.francotte.myrecipesstore.database.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FullCategoryDao {

    @Query("SELECT * FROM full_category_entity")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM full_category_entity WHERE strCategory = :categoryName")
    suspend fun getLightCategoryByName(categoryName: String): CategoryEntity?

    @Upsert
    suspend fun upsertAllCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLightCategory(category: CategoryEntity)

    @Query("DELETE FROM full_category_entity")
    suspend fun clearAll()
}