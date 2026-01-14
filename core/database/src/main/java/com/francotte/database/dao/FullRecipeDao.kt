package com.francotte.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.francotte.database.model.FullRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FullRecipeDao {

    @Query("SELECT * FROM full_recipe_entity")
    fun getAllFullRecipes(): Flow<List<FullRecipeEntity>>

    @Query("SELECT * FROM full_recipe_entity WHERE isLatest = 1")
    fun getLatestFullRecipes(): Flow<List<FullRecipeEntity>>

    @Query("SELECT * FROM full_recipe_entity WHERE isFavorite = 1")
    fun getAllFavoritesFullRecipes(): Flow<List<FullRecipeEntity>>

    @Query("SELECT * FROM full_recipe_entity WHERE idMeal = :id")
    fun getFullRecipeById(id: String): Flow<FullRecipeEntity?>

    @Query("SELECT * FROM full_recipe_entity WHERE idMeal IN (:ids)")
    fun getFullRecipesByIds(ids: List<String>): Flow<List<FullRecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFullRecipe(recipe: FullRecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFullRecipes(recipes: List<FullRecipeEntity>)

    @Upsert
    suspend fun upsertAllFullRecipes(recipes: List<FullRecipeEntity>)

    @Query("SELECT MAX(lastUpdated) FROM full_recipe_entity WHERE idMeal = :id")
    suspend fun getFullRecipeLastUpdated(id:String): Long?

    @Query("SELECT MAX(lastUpdated) FROM full_recipe_entity WHERE isLatest = 1")
    suspend fun getLastUpdatedForLatest(): Long?

    @Query("DELETE FROM full_recipe_entity WHERE isLatest = 1")
    suspend fun deleteOldLatestRecipes()

    @Query("DELETE FROM full_recipe_entity WHERE isFavorite = 1")
    suspend fun deleteAllFavoritesRecipes()

    @Query("DELETE FROM full_recipe_entity")
    suspend fun clearAll()

    @Transaction
    suspend fun refreshLatest(recipes: List<FullRecipeEntity>) {
        deleteOldLatestRecipes()
        upsertAllFullRecipes(recipes)
    }
}