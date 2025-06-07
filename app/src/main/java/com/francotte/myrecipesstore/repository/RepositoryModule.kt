package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.user.UserDataSource
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
    fun provideDetailRecipeRepository(api: RecipeApi, userDataSource: UserDataSource): DetailRecipeRepository = DetailRecipeRepositoryImpl(api,userDataSource)

    @Singleton
    @Provides
    fun provideMealRecipeRepository(api: RecipeApi, preferencesDataSource: UserDataSource): RecipesRepository = RecipesRepositoryImpl(api, preferencesDataSource)

    @Singleton
    @Provides
    fun provideCategoriesRepository(api: RecipeApi): CategoriesRepository = CategoriesRepositoryImpl(api)

    @Singleton
    @Provides
    fun provideFavoritesRepository(api: RecipeApi, userDataSource: UserDataSource): FavoritesRepository = FavoritesRepositoryImpl(api, userDataSource)

}