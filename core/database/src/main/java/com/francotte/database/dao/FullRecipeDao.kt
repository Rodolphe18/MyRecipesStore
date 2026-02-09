package com.francotte.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.francotte.database.model.FullRecipeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

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

    @Query("SELECT idMeal FROM full_recipe_entity WHERE idMeal IN (:ids)")
    suspend fun getExistingIds(ids: List<String>): List<String>

    @Query("SELECT * FROM full_recipe_entity WHERE idMeal IN (:ids)")
    fun observeFullRecipesByIds(ids: List<String>): Flow<List<FullRecipeEntity>>

    @Query("SELECT * FROM full_recipe_entity WHERE idMeal IN (:ids)")
    suspend fun getFullRecipesByIdsSnapshot(ids: List<String>): List<FullRecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFullRecipe(recipe: FullRecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFullRecipes(recipes: List<FullRecipeEntity>)

    @Upsert
    suspend fun upsertAllFullRecipes(recipes: List<FullRecipeEntity>)

    @Query("SELECT MAX(savedTimestamp) FROM full_recipe_entity WHERE isLatest = 1")
    suspend fun getLastUpdatedForLatest(): java.time.Instant?

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
