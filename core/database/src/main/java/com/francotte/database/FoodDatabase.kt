package com.francotte.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.francotte.database.dao.FullCategoryDao
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.dao.LightCategoryDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.database.model.CategoryEntity
import com.francotte.database.model.FullRecipeEntity
import com.francotte.database.model.LightCategoryEntity
import com.francotte.database.model.LightRecipeEntity

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

