package com.francotte.notifications.di

import com.francotte.notifications.AndroidNotifier
import com.francotte.notifications.FirebaseNotificationsInitializer
import com.francotte.notifications.NotificationsInitializer
import com.francotte.notifications.Notifier
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindNotifier(impl: AndroidNotifier): Notifier

    @Binds
    @Singleton
    internal abstract fun bindNotificationsInitializer(
        impl: FirebaseNotificationsInitializer,
    ): NotificationsInitializer

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
    }
}
