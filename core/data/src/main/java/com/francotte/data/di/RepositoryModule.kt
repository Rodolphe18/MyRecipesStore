package com.francotte.data.di

import com.francotte.data.repository.CategoriesRepository
import com.francotte.data.repository.CompositeUserFullRecipeRepository
import com.francotte.data.repository.CompositeUserHomeRepository
import com.francotte.data.repository.OfflineFirstCategoriesRepositoryImpl
import com.francotte.data.repository.OfflineFirstFavoritesRepository
import com.francotte.data.repository.OfflineFirstFavoritesRepositoryImpl
import com.francotte.data.repository.OfflineFirstFullRecipeRepository
import com.francotte.data.repository.OfflineFirstFullRecipeRepositoryImpl
import com.francotte.data.repository.OfflineFirstHomeRepository
import com.francotte.data.repository.RecipesRepository
import com.francotte.data.repository.SearchRepository
import com.francotte.data.repository.SearchRepositoryImpl
import com.francotte.data.repository.UserFullRecipeRepository
import com.francotte.data.repository.UserHomeRepository
import com.francotte.database.dao.FullCategoryDao
import com.francotte.database.dao.FullRecipeDao
import com.francotte.database.dao.LightRecipeDao
import com.francotte.datastore.UserDataRepository
import com.francotte.network.api.FavoriteApi
import com.francotte.network.api.RecipeApi
import dagger.Binds
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
    fun provideSearchRepository(
        homeRepository: OfflineFirstHomeRepository,
        api: RecipeApi,
        userDataRepository: UserDataRepository,
    ): SearchRepository = SearchRepositoryImpl(homeRepository, api, userDataRepository)

    @Singleton
    @Provides
    fun provideLikeableFullRecipeRepository(
        offlineFullRecipeData: OfflineFirstFullRecipeRepository,
        userDataRepository: UserDataRepository,
    ): UserFullRecipeRepository = CompositeUserFullRecipeRepository(offlineFullRecipeData, userDataRepository)

    @Singleton
    @Provides
    fun provideOfflineFirstFullRecipeRepository(
        api: RecipeApi,
        dao: FullRecipeDao,
    ): OfflineFirstFullRecipeRepository = OfflineFirstFullRecipeRepositoryImpl(api, dao)

    @Singleton
    @Provides
    fun provideOfflineFirstFavoritesRepository(
        dao: FullRecipeDao,
        favoriteApi: FavoriteApi,
        recipeApi: RecipeApi,
        userDataRepository: UserDataRepository,
    ): OfflineFirstFavoritesRepository = OfflineFirstFavoritesRepositoryImpl(dao, favoriteApi,recipeApi, userDataRepository)

    @Singleton
    @Provides
    fun provideOfflineFirstHomeRepository(
        api: RecipeApi,
        lightRecipeDao: LightRecipeDao,
        fullRecipeDao: FullRecipeDao,
    ): RecipesRepository = OfflineFirstHomeRepository(lightRecipeDao, fullRecipeDao, api)

    @Singleton
    @Provides
    fun provideHomeRepository(
        homeRepository: OfflineFirstHomeRepository,
        userDataRepository: UserDataRepository,
    ): UserHomeRepository = CompositeUserHomeRepository(homeRepository, userDataRepository)

    @Singleton
    @Provides
    fun provideCategoriesRepository(
        api: RecipeApi,
        dao: FullCategoryDao,
    ): CategoriesRepository = OfflineFirstCategoriesRepositoryImpl(api, dao)


}
