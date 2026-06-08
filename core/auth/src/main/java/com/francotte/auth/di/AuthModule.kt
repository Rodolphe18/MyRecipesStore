package com.francotte.auth.di

import com.francotte.auth.RegistrationManager
import com.francotte.auth.RegistrationRepository
import com.francotte.auth.SessionManager
import com.francotte.auth.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: SessionManager): SessionRepository

    @Binds @Singleton
    abstract fun bindRegistrationRepository(impl: RegistrationManager): RegistrationRepository
}
