package com.francotte.myrecipesstore.repository

import com.francotte.myrecipesstore.database.dao.FullCategoryDao
import com.francotte.myrecipesstore.database.dao.FullRecipeDao
import com.francotte.myrecipesstore.database.dao.LightRecipeDao
import com.francotte.myrecipesstore.database.repository.CategoriesRepository
import com.francotte.myrecipesstore.database.repository.OfflineFirstCategoriesRepositoryImpl
import com.francotte.myrecipesstore.database.repository.OfflineFirstFavoritesRepository
import com.francotte.myrecipesstore.database.repository.OfflineFirstFavoritesRepositoryImpl
import com.francotte.myrecipesstore.database.repository.OfflineFirstFullRecipeRepository
import com.francotte.myrecipesstore.database.repository.OfflineFirstFullRecipeRepositoryImpl
import com.francotte.myrecipesstore.database.repository.OfflineFirstHomeRepository
import com.francotte.myrecipesstore.database.repository.RecipesRepository
import com.francotte.myrecipesstore.datastore.UserDataSource
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
    fun provideLikeableFullRecipeRepository(offlineFullRecipeData: OfflineFirstFullRecipeRepository, userDataSource: UserDataSource): FullRecipeRepository = FullRecipeRepositoryImpl(offlineFullRecipeData,userDataSource)

    @Singleton
    @Provides
    fun provideOfflineFirstFullRecipeRepository(api: RecipeApi, dao: FullRecipeDao): OfflineFirstFullRecipeRepository = OfflineFirstFullRecipeRepositoryImpl(api, dao)

    @Singleton
    @Provides
    fun provideOfflineFirstFavoritesRepository(api: RecipeApi, dao: FullRecipeDao, userDataSource: UserDataSource): OfflineFirstFavoritesRepository = OfflineFirstFavoritesRepositoryImpl(api, dao, userDataSource)


    @Singleton
    @Provides
    fun provideOfflineFirstHomeRepository(api: RecipeApi, lightRecipeDao: LightRecipeDao, fullRecipeDao: FullRecipeDao): RecipesRepository = OfflineFirstHomeRepository(lightRecipeDao, fullRecipeDao,api)

    @Singleton
    @Provides
    fun provideLikeableLightRecipesRepository(homeRepository: OfflineFirstHomeRepository, userDataSource: UserDataSource): LikeableLightRecipesRepository = HomeRepository(homeRepository, userDataSource)

    @Singleton
    @Provides
    fun provideCategoriesRepository(api: RecipeApi, dao: FullCategoryDao): CategoriesRepository = OfflineFirstCategoriesRepositoryImpl(api, dao)

    @Singleton
    @Provides
    fun provideFavoritesRepository(offlineFirstFavoritesRepository: OfflineFirstFavoritesRepository, userDataSource: UserDataSource): FavoritesRepository = FavoritesRepositoryImpl(offlineFirstFavoritesRepository, userDataSource)

}