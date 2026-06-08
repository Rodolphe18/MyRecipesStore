package com.francotte.inapp_update

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InAppUpdateModule {
    @Provides
    @Singleton
    fun provideAppUpdateManager(
        @ApplicationContext context: Context,
    ): AppUpdateManager = AppUpdateManagerFactory.create(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class InAppUpdateBindingModule {
    @Binds @Singleton
    abstract fun bindInAppUpdateRepository(impl: InAppUpdateManager): InAppUpdateRepository
}
