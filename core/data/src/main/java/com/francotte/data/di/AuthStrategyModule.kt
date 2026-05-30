package com.francotte.data.di

import com.francotte.data.interfaces.LoginAuthStrategy
import com.francotte.data.manager.EmailPasswordAuthStrategy
import com.francotte.data.manager.GoogleAuthStrategy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthStrategyModule {

    @Binds @IntoSet
    abstract fun bindEmailPasswordStrategy(impl: EmailPasswordAuthStrategy): LoginAuthStrategy

    @Binds @IntoSet
    abstract fun bindGoogleStrategy(impl: GoogleAuthStrategy): LoginAuthStrategy
}
