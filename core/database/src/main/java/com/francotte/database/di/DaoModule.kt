/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.francotte.database.di

import com.francotte.database.FoodDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun providesLightRecipeDao(database: FoodDatabase): LightRecipeDao = database.lightRecipeDao()

    @Provides
    fun providesRecipeDao(database: FoodDatabase): FullRecipeDao = database.fullRecipeDao()

    @Provides
    fun providesLightCategoryDao(database: FoodDatabase): LightCategoryDao = database.lightCategoryDao()

    @Provides
    fun providesFullCategoryDao(database: FoodDatabase): FullCategoryDao = database.fullCategoryDao()

    @Provides
    fun providesIngredientsDao(database: FoodDatabase): IngredientDao = database.ingredientsDao()

    @Provides
    fun providesAreasDao(database: FoodDatabase): AreaDao = database.areasDao()

    @Provides
    fun providesFtsAreaDao(database: FoodDatabase): AreaFtsDao = database.areaFtsDao()

    @Provides
    fun providesFtsIngredientDao(database: FoodDatabase): IngredientFtsDao = database.ingredientFtsDao()

    @Provides
    fun providesFtsCategoryDao(database: FoodDatabase): CategoryFtsDao = database.categoryFtsDao()

    @Provides
    fun providesRecipeFtsDao(database: FoodDatabase): RecipeFtsDao = database.recipeFtsDao()

    @Provides
    fun providesSearchIndexStateDao(database: FoodDatabase): SearchIndexStateDao = database.searchIndexStateDao()
}
