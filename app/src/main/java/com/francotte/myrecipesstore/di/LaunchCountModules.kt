package com.francotte.myrecipesstore.di

import com.francotte.common.counters.DefaultLaunchCounter
import com.francotte.common.counters.LaunchCounter
import com.francotte.common.counters.LaunchCountPreferences
import com.francotte.shared_prefs.LaunchCountPreferencesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LaunchDiModule {

    @Binds
    @Singleton
    abstract fun bindLaunchPrefs(impl: LaunchCountPreferencesImpl): LaunchCountPreferences

    @Binds
    @Singleton
    abstract fun bindLaunchCounter(impl: DefaultLaunchCounter): LaunchCounter
}