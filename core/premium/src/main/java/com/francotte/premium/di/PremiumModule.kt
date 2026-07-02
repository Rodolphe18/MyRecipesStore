package com.francotte.premium.di

import com.francotte.premium.PremiumManager
import com.francotte.premium.PremiumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PremiumModule {

    @Binds @Singleton
    abstract fun bindPremiumRepository(impl: PremiumManager): PremiumRepository
}
