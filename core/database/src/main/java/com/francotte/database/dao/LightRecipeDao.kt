package com.francotte.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.francotte.database.crossrefs.AreaWithRecipes
import com.francotte.database.crossrefs.CategoryWithRecipes
import com.francotte.database.crossrefs.IngredientWithRecipes
import com.francotte.database.crossrefs.RecipeAreaCrossRef
import com.francotte.database.crossrefs.RecipeCategoryCrossRef
import com.francotte.database.crossrefs.RecipeIngredientCrossRef
import com.francotte.database.model.AreaEntity
import com.francotte.database.model.LightCategoryEntity
import com.francotte.database.model.LightRecipeEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface LightRecipeDao {

    // ------- AREA ---------
    @Transaction
    @Query("SELECT * FROM area WHERE strArea = :area")
    fun observeAreaWithRecipes(area: String): Flow<AreaWithRecipes>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertArea(area: AreaEntity)

    @Query("SELECT MAX(savedTimestamp) FROM recipe_area_xref WHERE strArea = :area")
    suspend fun getLastUpdatedForArea(area: String): Instant?

    @Query("DELETE FROM recipe_area_xref WHERE strArea = :area")
    suspend fun clearAreaRefs(area: String)

    @Upsert
    suspend fun upsertAreaRefs(refs: List<RecipeAreaCrossRef>)

    @Transaction
    suspend fun upsertAreaWithRecipes(
        area: String,
        recipes: List<LightRecipeEntity>,
        refs: List<RecipeAreaCrossRef>,
        clearBefore: Boolean
    ) {
        upsertArea(AreaEntity(strArea = area))
        upsertLightRecipes(recipes)
        if (clearBefore) clearAreaRefs(area)
        upsertAreaRefs(refs)
    }

    // ------- CATEGORY ---------
    @Transaction
    @Query("SELECT * FROM light_category_entity WHERE strCategory = :category")
    fun observeCategoryWithRecipes(category: String): Flow<CategoryWithRecipes?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertCategory(category: LightCategoryEntity): Long

    @Upsert
    suspend fun upsertCategoryRefs(refs: List<RecipeCategoryCrossRef>)

    @Query("DELETE FROM recipe_category_xref WHERE strCategory = :category")
    suspend fun clearCategoryRefs(category: String)

    @Transaction
    suspend fun upsertCategoryWithRecipes(
        category: String,
        recipes: List<LightRecipeEntity>,
        refs: List<RecipeCategoryCrossRef>,
        clearBefore: Boolean
    ): Boolean {
        upsertCategory(LightCategoryEntity(strCategory = category))
        upsertLightRecipes(recipes)
        if (clearBefore) clearCategoryRefs(category)
        upsertCategoryRefs(refs)
        return true
    }

    // ----- INGREDIENTS ---------

    @Transaction
    @Query("SELECT * FROM ingredient WHERE name = :ingredientName")
    fun observeIngredientWithRecipes(ingredientName: String): Flow<IngredientWithRecipes>

    @Query("SELECT MAX(savedTimestamp) FROM recipe_ingredient_xref WHERE ingredientName = :ingredientName")
    suspend fun getLastUpdatedForIngredientRecipes(ingredientName: String): Instant?

    @Query("DELETE FROM recipe_ingredient_xref WHERE ingredientName = :ingredientName")
    suspend fun clearIngredientRefs(ingredientName: String)

    @Upsert
    suspend fun upsertRecipes(recipes: List<LightRecipeEntity>)

    @Upsert
    suspend fun upsertIngredientRefs(refs: List<RecipeIngredientCrossRef>)

    @Transaction
    suspend fun upsertIngredientWithRecipes(
        ingredientName: String,
        recipes: List<LightRecipeEntity>,
        refs: List<RecipeIngredientCrossRef>,
        clearBefore: Boolean
    ) {
        upsertRecipes(recipes)
        if (clearBefore) clearIngredientRefs(ingredientName)
        upsertIngredientRefs(refs)
    }


    // --- RECIPES ---
    @Upsert
    suspend fun upsertLightRecipes(recipes: List<LightRecipeEntity>)

    // LAST UPDATES

    @Query("SELECT MAX(savedTimestamp) FROM recipe_category_xref WHERE strCategory = :category")
    suspend fun getLastUpdatedForCategory(category: String): Instant?


    @Query("SELECT * FROM light_recipe_entity WHERE idMeal = :id")
    suspend fun getLightRecipeById(id: String): LightRecipeEntity?

    @Upsert
    suspend fun upsertAllLightRecipes(recipes: List<LightRecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLightRecipe(recipe: LightRecipeEntity)

    @Query("DELETE FROM light_recipe_entity")
    suspend fun clearAll()

    // ---- FTS --------

    @Query("""
        SELECT * FROM light_recipe_entity
        WHERE CASE WHEN :useFilterIds
            THEN idMeal IN (:filterIds)
            ELSE 0
        END
    """)
    fun observeByIds(
        useFilterIds: Boolean,
        filterIds: Set<String>,
    ): Flow<List<LightRecipeEntity>>

    // pour populate FTS (one-shot)
    @Query("SELECT * FROM light_recipe_entity")
    suspend fun getAllOnce(): List<LightRecipeEntity>


}
