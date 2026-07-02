package com.francotte.auth.di

import com.francotte.auth.strategy.EmailPasswordLoginStrategy
import com.francotte.auth.strategy.GoogleLoginStrategy
import com.francotte.auth.strategy.LoginAuthStrategy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthStrategyModule {

    @Binds @IntoSet
    abstract fun bindEmailPasswordStrategy(impl: EmailPasswordLoginStrategy): LoginAuthStrategy

    @Binds @IntoSet
    abstract fun bindGoogleStrategy(impl: GoogleLoginStrategy): LoginAuthStrategy
}
