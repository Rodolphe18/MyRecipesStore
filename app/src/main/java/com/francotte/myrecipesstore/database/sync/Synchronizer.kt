package com.francotte.myrecipesstore.database.sync

import com.francotte.myrecipesstore.database.model.CategoryEntity
import com.francotte.myrecipesstore.database.model.FullRecipeEntity
import com.francotte.myrecipesstore.database.model.LightCategoryEntity
import com.francotte.myrecipesstore.database.model.LightRecipeEntity
import com.francotte.myrecipesstore.network.model.NetworkCategory
import com.francotte.myrecipesstore.network.model.NetworkLightCategory
import com.francotte.myrecipesstore.network.model.NetworkLightRecipe
import com.francotte.myrecipesstore.network.model.NetworkRecipe
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface Synchronizer<T, R> {
    suspend fun sync(
        modelFetcher: suspend () -> List<T>,
        modelMapper: (T) -> R,
        modelSaver: suspend (List<R>) -> Unit,
    ): Boolean
}

class DefaultSynchronizer<T, R> @Inject constructor() : Synchronizer<T, R> {
    override suspend fun sync(
        modelFetcher: suspend () -> List<T>,
        modelMapper: (T) -> R,
        modelSaver: suspend (List<R>) -> Unit,
    ): Boolean {
        val networkItems = modelFetcher()
        if (networkItems.isEmpty()) return false

        val entities = networkItems.map(modelMapper)
        modelSaver(entities)

        return true
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideLightRecipeSynchronizer(): Synchronizer<NetworkLightRecipe, LightRecipeEntity> =
        DefaultSynchronizer()

    @Provides
    @Singleton
    fun provideFullRecipeSynchronizer(): Synchronizer<NetworkRecipe, FullRecipeEntity> =
        DefaultSynchronizer()


    @Provides
    @Singleton
    fun provideLightCategorySynchronizer(): Synchronizer<NetworkLightCategory, LightCategoryEntity> =
        DefaultSynchronizer()

    @Provides
    @Singleton
    fun provideFullCategorySynchronizer(): Synchronizer<NetworkCategory, CategoryEntity> =
        DefaultSynchronizer()

}