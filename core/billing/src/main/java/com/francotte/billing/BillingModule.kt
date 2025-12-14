package com.francotte.billing

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class PremiumStatusModule {

    @Binds
    abstract fun bindPremiumStatusProvider(
        billingManager: BillingManager
    ): PremiumStatusProvider
}