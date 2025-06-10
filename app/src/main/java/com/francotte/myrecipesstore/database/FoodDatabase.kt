package com.francotte.myrecipesstore.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.francotte.myrecipesstore.database.dao.FullCategoryDao
import com.francotte.myrecipesstore.database.dao.FullRecipeDao
import com.francotte.myrecipesstore.database.dao.LightCategoryDao
import com.francotte.myrecipesstore.database.dao.LightRecipeDao
import com.francotte.myrecipesstore.database.model.CategoryEntity
import com.francotte.myrecipesstore.database.model.FullRecipeEntity
import com.francotte.myrecipesstore.database.model.LightCategoryEntity
import com.francotte.myrecipesstore.database.model.LightRecipeEntity

@Database(
    entities = [
        LightRecipeEntity::class,
        FullRecipeEntity::class,
        LightCategoryEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = true,
)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun lightRecipeDao(): LightRecipeDao
    abstract fun fullRecipeDao(): FullRecipeDao
    abstract fun lightCategoryDao(): LightCategoryDao
    abstract fun fullCategoryDao(): FullCategoryDao
}