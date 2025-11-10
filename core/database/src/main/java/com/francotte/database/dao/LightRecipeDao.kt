package com.francotte.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.francotte.database.model.LightRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LightRecipeDao {

    @Query("SELECT * FROM light_recipe_entity")
    fun getAllLightRecipes(): Flow<List<LightRecipeEntity>>

    @Query("SELECT * FROM light_recipe_entity WHERE idMeal = :id")
    suspend fun getLightRecipeById(id: String): LightRecipeEntity?

    @Query("SELECT * FROM light_recipe_entity WHERE category = :category")
    fun getLightRecipesByCategory(category: String): Flow<List<LightRecipeEntity>>

    @Query("SELECT * FROM light_recipe_entity WHERE area = :area")
    fun getLightRecipesByArea(area: String): Flow<List<LightRecipeEntity>>

    @Upsert
    suspend fun upsertAllLightRecipes(recipes: List<LightRecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLightRecipe(recipe: LightRecipeEntity)

    @Query("SELECT * FROM light_recipe_entity WHERE isLatest = 1")
    fun getLatestLightRecipes(): Flow<List<LightRecipeEntity>>

    @Query("SELECT COUNT(*) FROM light_recipe_entity WHERE isLatest = 1")
    suspend fun countLatestLightRecipes(): Int



    @Query("SELECT MAX(lastUpdated) FROM light_recipe_entity WHERE area = :area")
    suspend fun getLastUpdatedForArea(area: String): Long?

    @Query("SELECT MAX(lastUpdated) FROM light_recipe_entity WHERE category = :category")
    suspend fun getLastUpdatedForCategory(category: String): Long?

    @Query("SELECT MAX(lastUpdated) FROM light_recipe_entity WHERE isLatest = 1")
    suspend fun getLastUpdatedForLatest(): Long?

    @Query("DELETE FROM light_recipe_entity")
    suspend fun clearAll()
}

