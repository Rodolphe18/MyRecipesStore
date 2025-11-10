package com.francotte.database.di

import android.content.Context
import androidx.room.Room
import com.francotte.database.FoodDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.ExecutorService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext context: Context,
        sharedExecutor: ExecutorService
    ): FoodDatabase {
        return Room.databaseBuilder(context, FoodDatabase::class.java, "food-database")
            .setQueryExecutor(sharedExecutor)
            .setTransactionExecutor(sharedExecutor)
            .build()
    }

}