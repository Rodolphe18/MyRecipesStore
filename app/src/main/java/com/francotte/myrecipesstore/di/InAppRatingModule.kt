package com.francotte.myrecipesstore.di

import com.francotte.inapp_rating.InAppRatingPreferences
import com.francotte.inapp_rating.InAppReview
import com.francotte.inapp_rating.InAppReviewImpl
import com.francotte.inapp_rating.PlayStoreOpener
import com.francotte.inapp_rating.PlayStoreOpenerImpl
import com.francotte.shared_prefs.InAppRatingPreferencesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RatingDiBindsModule {

    @Binds
    @Singleton
    abstract fun bindInAppRatingPrefs(
        impl: InAppRatingPreferencesImpl
    ): InAppRatingPreferences

    @Binds @Singleton
    abstract fun bindInAppReview(
        impl: InAppReviewImpl
    ): InAppReview

    @Binds @Singleton
    abstract fun bindStoreListingOpener(
        impl: PlayStoreOpenerImpl
    ): PlayStoreOpener
}