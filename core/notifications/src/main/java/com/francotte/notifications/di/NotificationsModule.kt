package com.francotte.notifications.di

import com.francotte.notifications.AndroidNotifier
import com.francotte.notifications.Notifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindNotifier(impl: AndroidNotifier): Notifier
}
