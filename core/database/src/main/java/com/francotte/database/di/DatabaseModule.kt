package com.francotte.database.di

import android.content.Context
import androidx.room.Room
import com.francotte.database.FoodDatabase
import com.francotte.database.MIGRATION_2_3
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
        sharedExecutor: ExecutorService,
    ): FoodDatabase =
        Room
            .databaseBuilder(context, FoodDatabase::class.java, "food-database")
            .addMigrations(MIGRATION_2_3)
            .setQueryExecutor(sharedExecutor)
            .setTransactionExecutor(sharedExecutor)
            .build()
}
