package com.francotte.myrecipesstore.network.di

import com.francotte.myrecipesstore.API_KEY
import com.francotte.myrecipesstore.network.api.AuthApi
import com.francotte.myrecipesstore.network.api.FavoriteApi
import com.francotte.myrecipesstore.network.utils.HttpLoggingInterceptor
import com.francotte.myrecipesstore.network.utils.JsonConverterFactory
import com.francotte.myrecipesstore.network.api.RecipeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    private inline fun <reified A> providesApi(jsonConverterFactory: JsonConverterFactory): A {
        return Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v2/$API_KEY/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                    .build()
            )
            .addConverterFactory(jsonConverterFactory)
            .build()
            .create()
    }

    @Singleton
    @Provides
    fun provideRecipeApi(jsonConverterFactory: JsonConverterFactory): RecipeApi = providesApi(jsonConverterFactory = jsonConverterFactory)

    @Singleton
    @Provides
    fun provideAuthApi(jsonConverterFactory: JsonConverterFactory): AuthApi = providesApi(jsonConverterFactory = jsonConverterFactory)

    @Singleton
    @Provides
    fun provideFavoriteApi(jsonConverterFactory: JsonConverterFactory): FavoriteApi = providesApi(jsonConverterFactory = jsonConverterFactory)


}