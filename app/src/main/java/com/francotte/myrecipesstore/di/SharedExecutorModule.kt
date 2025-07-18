package com.francotte.myrecipesstore.di

import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import coil.ImageLoader
import com.francotte.myrecipesstore.database.FoodDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.asCoroutineDispatcher
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object SharedExecutorModule {

    @Provides
    @Singleton
    fun provideSharedExecutor(): ExecutorService {
        return Executors.newFixedThreadPool(4)
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        context: Context,
        sharedExecutor: ExecutorService
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .dispatcher(sharedExecutor.asCoroutineDispatcher())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        sharedExecutor: ExecutorService
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .dispatcher(
                Dispatcher(sharedExecutor)
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkManagerConfiguration(
        sharedExecutor: ExecutorService
    ): Configuration {
        return Configuration.Builder()
            .setExecutor(sharedExecutor)
            .build()
    }

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



