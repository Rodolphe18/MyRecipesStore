package com.francotte.ads.di

import com.francotte.ads.AdMobBannerAdProvider
import com.francotte.ads.BannerAdProvider
import com.francotte.ads.BannerConfigProvider
import com.francotte.ads.BannerManager
import com.francotte.ads.BannerRepository
import com.francotte.ads.DefaultBannerConfigProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdsModule {

    @Binds @Singleton
    abstract fun bindBannerRepository(impl: BannerManager): BannerRepository

    @Binds @Singleton
    abstract fun bindBannerAdProvider(impl: AdMobBannerAdProvider): BannerAdProvider

    @Binds @Singleton
    abstract fun bindBannerConfigProvider(impl: DefaultBannerConfigProvider): BannerConfigProvider
}
