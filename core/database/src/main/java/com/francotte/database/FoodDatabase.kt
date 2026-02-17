package com.francotte.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.francotte.database.converters.InstantConverters
import com.francotte.database.crossrefs.RecipeAreaCrossRef
import com.francotte.database.crossrefs.RecipeCategoryCrossRef
import com.francotte.database.crossrefs.RecipeIngredientCrossRef
import com.francotte.database.dao.AreaDao
import com.francotte.database.dao.FullCategoryDao
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.dao.IngredientDao
import com.francotte.database.dao.LightCategoryDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.database.dao.fts.AreaFtsDao
import com.francotte.database.dao.fts.CategoryFtsDao
import com.francotte.database.dao.fts.IngredientFtsDao
import com.francotte.database.dao.fts.RecipeFtsDao
import com.francotte.database.dao.fts.SearchIndexStateDao
import com.francotte.database.model.AreaEntity
import com.francotte.database.model.AreaFtsEntity
import com.francotte.database.model.CategoryEntity
import com.francotte.database.model.CategoryFtsEntity
import com.francotte.database.model.FullRecipeEntity
import com.francotte.database.model.IngredientEntity
import com.francotte.database.model.IngredientFtsEntity
import com.francotte.database.model.LightCategoryEntity
import com.francotte.database.model.LightRecipeEntity
import com.francotte.database.model.RecipeFtsEntity
import com.francotte.database.model.SearchIndexCategoryStateEntity

@Database(
    entities = [
        LightRecipeEntity::class,
        FullRecipeEntity::class,
        LightCategoryEntity::class,
        CategoryEntity::class,
        IngredientEntity::class,
        AreaEntity::class,
        RecipeIngredientCrossRef::class,
        RecipeCategoryCrossRef::class,
        RecipeAreaCrossRef::class,
        RecipeFtsEntity::class,
        AreaFtsEntity::class,
        CategoryFtsEntity::class,
        IngredientFtsEntity::class,
        SearchIndexCategoryStateEntity::class
    ],
    version = 8,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
            spec = FoodDatabase.Migration1To2::class
        ),
    ],
    exportSchema = true,
)
@TypeConverters(InstantConverters::class)
abstract class FoodDatabase : RoomDatabase() {

    abstract fun lightRecipeDao(): LightRecipeDao

    abstract fun fullRecipeDao(): FullRecipeDao

    abstract fun lightCategoryDao(): LightCategoryDao

    abstract fun fullCategoryDao(): FullCategoryDao

    abstract fun ingredientsDao(): IngredientDao

    abstract fun areasDao(): AreaDao

    abstract fun areaFtsDao(): AreaFtsDao

    abstract fun categoryFtsDao(): CategoryFtsDao

    abstract fun ingredientFtsDao(): IngredientFtsDao

    abstract fun searchIndexStateDao(): SearchIndexStateDao

    abstract fun recipeFtsDao(): RecipeFtsDao


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
