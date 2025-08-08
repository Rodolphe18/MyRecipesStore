package com.francotte.myrecipesstore.network.di

import com.francotte.myrecipesstore.network.model.NetworkAbstractCategory
import com.francotte.myrecipesstore.network.model.NetworkAbstractRecipe
import com.francotte.myrecipesstore.network.model.NetworkCategory
import com.francotte.myrecipesstore.network.model.NetworkLightCategory
import com.francotte.myrecipesstore.network.model.NetworkLightRecipe
import com.francotte.myrecipesstore.network.model.NetworkRecipe
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JsonModule {

    @Singleton
    @Provides
    fun providesJson(): Json = Json {
        serializersModule = SerializersModule {
            polymorphic(NetworkAbstractRecipe::class) {
                subclass(NetworkRecipe::class, NetworkRecipe.serializer())
                subclass(NetworkLightRecipe::class, NetworkLightRecipe.serializer())
            }
            polymorphic(NetworkAbstractCategory::class) {
                subclass(NetworkCategory::class, NetworkCategory.serializer())
                subclass(NetworkLightCategory::class, NetworkLightCategory.serializer())
            }
        }
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
        allowStructuredMapKeys = true
        coerceInputValues = true
    }

}