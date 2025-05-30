package com.francotte.myrecipesstore.api

import com.francotte.myrecipesstore.model.AbstractCategory
import com.francotte.myrecipesstore.model.AbstractRecipe
import com.francotte.myrecipesstore.model.Category
import com.francotte.myrecipesstore.model.LightCategory
import com.francotte.myrecipesstore.model.LightRecipe
import com.francotte.myrecipesstore.model.Recipe
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
            polymorphic(AbstractRecipe::class) {
                subclass(Recipe::class, Recipe.serializer())
                subclass(LightRecipe::class, LightRecipe.serializer())
            }
            polymorphic(AbstractCategory::class) {
                subclass(Category::class, Category.serializer())
                subclass(LightCategory::class, LightCategory.serializer())
            }
        }
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
        allowStructuredMapKeys = true
    }

}