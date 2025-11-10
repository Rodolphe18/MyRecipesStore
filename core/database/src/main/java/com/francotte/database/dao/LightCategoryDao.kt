package com.francotte.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.francotte.database.model.LightCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LightCategoryDao {

        @Query("SELECT * FROM light_category_entity")
        fun getAllLightCategories(): Flow<List<LightCategoryEntity>>

        @Query("SELECT * FROM light_category_entity WHERE strCategory = :categoryName")
        suspend fun getLightCategoryByName(categoryName: String): LightCategoryEntity?

        @Upsert
        suspend fun upsertAllLightCategories(categories: List<LightCategoryEntity>)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertLightCategory(category:LightCategoryEntity)

        @Query("DELETE FROM light_category_entity")
        suspend fun clearAll()

}