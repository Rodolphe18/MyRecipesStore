package com.francotte.domain

import android.content.Context
import com.francotte.common.extension.ApplicationScope
import com.francotte.database.dao.FullRecipeDao
import com.francotte.datastore.UserDataRepository
import com.francotte.network.api.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideAuthManager(
        @ApplicationContext context: Context,
        api: AuthApi,
        preferences: UserDataRepository,
        fullRecipeDao: FullRecipeDao,
        @ApplicationScope coroutineScope: CoroutineScope
    ) = AuthManager(context, api, preferences, fullRecipeDao, coroutineScope)


}