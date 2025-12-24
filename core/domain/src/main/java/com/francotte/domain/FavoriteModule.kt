package com.francotte.domain

import com.francotte.data.repository.OfflineFirstFavoritesRepository
import com.francotte.datastore.UserDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FavoriteModule {

    @Singleton
    @Provides
    fun provideFavoritesRepository(offlineFirstFavoritesRepository: OfflineFirstFavoritesRepository, userDataRepository: UserDataRepository, favoriteManager: FavoriteManager): FavoritesRepository =
        FavoritesRepositoryImpl(offlineFirstFavoritesRepository, favoriteManager, userDataRepository)

}