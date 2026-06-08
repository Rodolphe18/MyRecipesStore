package com.francotte.inapp_rating.di

import com.francotte.inapp_rating.InAppRatingManager
import com.francotte.inapp_rating.InAppRatingRepository
import com.francotte.inapp_rating.InAppReviewLauncher
import com.francotte.inapp_rating.InAppReviewLauncherManager
import com.francotte.inapp_rating.PlayStoreLauncher
import com.francotte.inapp_rating.PlayStoreLauncherImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InAppRatingModule {

    @Binds
    abstract fun bindInAppRatingManager(impl: InAppRatingManager): InAppRatingRepository

    @Binds
    @Singleton
    abstract fun bindInAppReview(impl: InAppReviewLauncherManager): InAppReviewLauncher

    @Binds
    @Singleton
    abstract fun bindStoreListingOpener(impl: PlayStoreLauncherImpl): PlayStoreLauncher

}
