package com.francotte.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francotte.database.model.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Insert
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredients(ingredients:List<IngredientEntity>):List<Long>

    @Query("SELECT MAX(savedTimestamp) FROM ingredient")
    suspend fun getLastUpdatedForIngredients(): java.time.Instant?

    @Query("SELECT * FROM ingredient")
    fun observeIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredient")
    suspend fun getAllIngredientsOnce(): List<IngredientEntity>

    @Delete
    suspend fun clearAllIngredients(ingredients:List<IngredientEntity>)

}
