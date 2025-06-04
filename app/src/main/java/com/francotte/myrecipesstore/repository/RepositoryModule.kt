package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.network.api.RecipeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideDetailRecipeRepository(api: RecipeApi): DetailRecipeRepository = DetailRecipeRepositoryImpl(api)

    @Singleton
    @Provides
    fun provideMealRecipeRepository(api: RecipeApi): RecipesRepository = RecipesRepositoryImpl(api)

    @Singleton
    @Provides
    fun provideCategoriesRepository(api: RecipeApi): CategoriesRepository = CategoriesRepositoryImpl(api)


}