package com.francotte.data.di

import com.francotte.data.favorite.FavoriteDelegate
import com.francotte.data.favorite.FavoriteDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * Bound in [ViewModelComponent] so each feature ViewModel gets its own [FavoriteDelegate]
 * instance (and therefore its own one-shot event stream).
 */
@Module
@InstallIn(ViewModelComponent::class)
abstract class FavoriteDelegateModule {

    @Binds
    abstract fun bindFavoriteDelegate(impl: FavoriteDelegateImpl): FavoriteDelegate
}
