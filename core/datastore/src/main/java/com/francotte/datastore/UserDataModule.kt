package com.francotte.datastore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface UserDataModule {

    @Suppress("unused")
    @Singleton
    @Binds
    fun bindUserDataModule(foodPreferencesDataSource: FoodPreferencesDataRepository): UserDataRepository
}