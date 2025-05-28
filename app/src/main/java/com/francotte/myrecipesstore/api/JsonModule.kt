package com.francotte.myrecipesstore.api

import com.francotte.myrecipesstore.model.AbstractMeal
import com.francotte.myrecipesstore.model.LightMeal
import com.francotte.myrecipesstore.model.Meal
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
            polymorphic(AbstractMeal::class) {
                subclass(Meal::class, Meal.serializer())
                subclass(LightMeal::class, LightMeal.serializer())
            }
        }
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
        allowStructuredMapKeys = true
    }

}