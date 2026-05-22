package com.francotte.common

import android.content.Context
import android.os.Build
import androidx.tracing.trace
import androidx.work.Configuration
import coil.ImageLoader
import coil.request.CachePolicy
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
    fun provideSharedExecutor(): ExecutorService = Executors.newFixedThreadPool(4)

    @Provides
    @Singleton
    fun provideImageLoader(
        context: Context,
        sharedExecutor: ExecutorService,
    ): ImageLoader = trace("FoodImageLoader") {
        ImageLoader
            .Builder(context)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .dispatcher(sharedExecutor.asCoroutineDispatcher())
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkManagerConfiguration(sharedExecutor: ExecutorService): Configuration =
        Configuration
            .Builder()
            .setExecutor(sharedExecutor)
            .build()
}
