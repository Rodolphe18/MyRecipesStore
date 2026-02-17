package com.francotte.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.francotte.database.model.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface FullCategoryDao {
    @Query("SELECT * FROM full_category_entity")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM full_category_entity")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Query("SELECT * FROM full_category_entity WHERE strCategory = :categoryName")
    suspend fun getLightCategoryByName(categoryName: String): CategoryEntity?

    @Query("SELECT MAX(savedTimestamp) FROM full_category_entity")
    suspend fun getLastUpdateForCategories(): Instant?

    @Upsert
    suspend fun upsertAllCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFullCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM full_category_entity")
    suspend fun clearAll()

}
