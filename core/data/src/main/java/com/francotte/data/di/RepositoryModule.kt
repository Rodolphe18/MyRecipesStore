package com.francotte.data.di

import com.francotte.data.interfaces.CategoriesRepository
import com.francotte.data.repository.CompositeUserFullRecipeRepository
import com.francotte.data.repository.CompositeUserHomeRepository
import com.francotte.data.repository.DefaultSearchContentsRepository
import com.francotte.data.interfaces.FavoritesRepository
import com.francotte.data.repository.FavoritesRepositoryImpl
import com.francotte.data.repository.OfflineFirstCategoriesRepositoryImpl
import com.francotte.data.interfaces.OfflineFirstFavoritesRepository
import com.francotte.data.repository.OfflineFirstFavoritesRepositoryImpl
import com.francotte.data.interfaces.OfflineFirstFullRecipeRepository
import com.francotte.data.repository.OfflineFirstFullRecipeRepositoryImpl
import com.francotte.data.repository.OfflineFirstHomeRepository
import com.francotte.data.repository.OfflineFirstIngredientsAndAreasRepositoryImpl
import com.francotte.data.interfaces.IngredientsAndAreasRepository
import com.francotte.data.favorite.FavoriteManager
import com.francotte.data.interfaces.FavoriteHelper
import com.francotte.data.interfaces.HomeRepository
import com.francotte.data.interfaces.SearchContentsRepository
import com.francotte.data.interfaces.UserDataRepository
import com.francotte.data.interfaces.UserFullRecipeRepository
import com.francotte.data.interfaces.UserHomeRepository
import com.francotte.data.repository.LocalUserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindIngredientsAndAreasRepository(impl: OfflineFirstIngredientsAndAreasRepositoryImpl): IngredientsAndAreasRepository

    @Binds @Singleton
    abstract fun bindSearchContentsRepository(impl: DefaultSearchContentsRepository): SearchContentsRepository

    @Binds @Singleton
    abstract fun bindUserFullRecipeRepository(impl: CompositeUserFullRecipeRepository): UserFullRecipeRepository

    @Binds @Singleton
    abstract fun bindOfflineFirstFullRecipeRepository(impl: OfflineFirstFullRecipeRepositoryImpl): OfflineFirstFullRecipeRepository

    @Binds @Singleton
    abstract fun bindOfflineFirstFavoritesRepository(impl: OfflineFirstFavoritesRepositoryImpl): OfflineFirstFavoritesRepository

    @Binds @Singleton
    abstract fun bindRecipesRepository(impl: OfflineFirstHomeRepository): HomeRepository

    @Binds @Singleton
    abstract fun bindUserHomeRepository(impl: CompositeUserHomeRepository): UserHomeRepository

    @Binds @Singleton
    abstract fun bindCategoriesRepository(impl: OfflineFirstCategoriesRepositoryImpl): CategoriesRepository

    @Binds @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds @Singleton
    abstract fun bindFavoriteToggler(impl: FavoriteManager): FavoriteHelper

    @Binds
    abstract fun bindsUserDataRepository(userDataRepository: LocalUserDataRepository): UserDataRepository
}
