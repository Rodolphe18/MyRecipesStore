package com.francotte.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.francotte.database.converters.InstantConverters
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
        CategoryEntity::class,
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
            spec = FoodDatabase.Migration1To2::class
        )
    ],
    exportSchema = true,
)
@TypeConverters(InstantConverters::class)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun lightRecipeDao(): LightRecipeDao

    abstract fun fullRecipeDao(): FullRecipeDao

    abstract fun lightCategoryDao(): LightCategoryDao

    abstract fun fullCategoryDao(): FullCategoryDao

    @RenameColumn.Entries(
        RenameColumn(
            tableName = "light_recipe_entity",
            fromColumnName = "lastUpdated",
            toColumnName = "savedTimestamp"
        ),
        RenameColumn(
            tableName = "full_recipe_entity",
            fromColumnName = "lastUpdated",
            toColumnName = "savedTimestamp"
        )
    )
    class Migration1To2 : AutoMigrationSpec
}
