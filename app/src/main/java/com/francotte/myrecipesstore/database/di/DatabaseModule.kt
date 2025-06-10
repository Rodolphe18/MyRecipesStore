
package com.francotte.myrecipesstore.database.di

import android.content.Context
import androidx.room.Room
import com.francotte.myrecipesstore.database.FoodDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesFoodDatabase(
        @ApplicationContext context: Context,
    ): FoodDatabase = Room.databaseBuilder(
        context,
        FoodDatabase::class.java,
        "food-database",
    ).build()
}
