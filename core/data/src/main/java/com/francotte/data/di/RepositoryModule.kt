package com.francotte.data.di

import com.francotte.data.repository.FullRecipeRepository
import com.francotte.data.repository.FullRecipeRepositoryImpl
import com.francotte.data.repository.HomeRepository
import com.francotte.data.repository.LikeableLightRecipesRepository
import com.francotte.data.repository.SearchRepository
import com.francotte.data.repository.SearchRepositoryImpl
import com.francotte.database.dao.FullCategoryDao
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.data.repository.CategoriesRepository
import com.francotte.data.repository.OfflineFirstCategoriesRepositoryImpl
import com.francotte.data.repository.OfflineFirstFavoritesRepository
import com.francotte.data.repository.OfflineFirstFavoritesRepositoryImpl
import com.francotte.data.repository.OfflineFirstFullRecipeRepository
import com.francotte.data.repository.OfflineFirstFullRecipeRepositoryImpl
import com.francotte.data.repository.OfflineFirstHomeRepository
import com.francotte.data.repository.OfflineSearchRepository
import com.francotte.data.repository.OfflineSearchRepositoryImpl
import com.francotte.data.repository.RecipesRepository
import com.francotte.datastore.UserDataSource
import com.francotte.network.api.RecipeApi
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
    fun provideOfflineSearchRepository(api: RecipeApi): OfflineSearchRepository =
        OfflineSearchRepositoryImpl(api)

    @Singleton
    @Provides
    fun provideSearchRepository(homeRepository: OfflineFirstHomeRepository,
                                offlineSearchRepository: OfflineSearchRepository,
                                userDataSource: UserDataSource): SearchRepository =
        SearchRepositoryImpl(homeRepository, offlineSearchRepository, userDataSource)


    @Singleton
    @Provides
    fun provideLikeableFullRecipeRepository(offlineFullRecipeData: OfflineFirstFullRecipeRepository, userDataSource: UserDataSource): FullRecipeRepository =
        FullRecipeRepositoryImpl(offlineFullRecipeData, userDataSource)

    @Singleton
    @Provides
    fun provideOfflineFirstFullRecipeRepository(api: RecipeApi, dao: FullRecipeDao): OfflineFirstFullRecipeRepository =
        OfflineFirstFullRecipeRepositoryImpl(api, dao)

    @Singleton
    @Provides
    fun provideOfflineFirstFavoritesRepository(api: RecipeApi, dao: FullRecipeDao, userDataSource: UserDataSource): OfflineFirstFavoritesRepository =
        OfflineFirstFavoritesRepositoryImpl(api, dao, userDataSource)


    @Singleton
    @Provides
    fun provideOfflineFirstHomeRepository(api: RecipeApi, lightRecipeDao: LightRecipeDao, fullRecipeDao: FullRecipeDao): RecipesRepository = OfflineFirstHomeRepository(lightRecipeDao, fullRecipeDao,api)

    @Singleton
    @Provides
    fun provideLikeableLightRecipesRepository(homeRepository: OfflineFirstHomeRepository, userDataSource: UserDataSource): LikeableLightRecipesRepository =
        HomeRepository(homeRepository, userDataSource)

    @Singleton
    @Provides
    fun provideCategoriesRepository(api: RecipeApi, dao: FullCategoryDao): CategoriesRepository =
        OfflineFirstCategoriesRepositoryImpl(api, dao)


}