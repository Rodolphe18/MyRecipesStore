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

package com.francotte.myrecipesstore.database.di

import com.francotte.myrecipesstore.database.dao.FullRecipeDao
import com.francotte.myrecipesstore.database.dao.LightRecipeDao
import com.francotte.myrecipesstore.database.FoodDatabase
import com.francotte.myrecipesstore.database.dao.FullCategoryDao
import com.francotte.myrecipesstore.database.dao.LightCategoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaoModule {

    @Provides
    fun providesLightRecipeDao(
        database: FoodDatabase,
    ): LightRecipeDao = database.lightRecipeDao()

    @Provides
    fun providesRecipeDao(
        database: FoodDatabase,
    ): FullRecipeDao = database.fullRecipeDao()

    @Provides
    fun providesLightCategoryDao(
        database: FoodDatabase,
    ): LightCategoryDao = database.lightCategoryDao()

    @Provides
    fun providesFullCategoryDao(
        database: FoodDatabase,
    ): FullCategoryDao = database.fullCategoryDao()

}
