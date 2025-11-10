package com.francotte.common

import android.content.Context
import androidx.work.Configuration
import coil.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton

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
    fun provideWorkManagerConfiguration(
        sharedExecutor: ExecutorService
    ): Configuration {
        return Configuration.Builder()
            .setExecutor(sharedExecutor)
            .build()
    }


}