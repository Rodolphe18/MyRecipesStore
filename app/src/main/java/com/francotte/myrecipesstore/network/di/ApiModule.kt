package com.francotte.myrecipesstore.network.di

import com.francotte.myrecipesstore.API_KEY
import com.francotte.myrecipesstore.network.api.AuthApi
import com.francotte.myrecipesstore.network.api.FavoriteApi
import com.francotte.myrecipesstore.network.utils.HttpLoggingInterceptor
import com.francotte.myrecipesstore.network.api.RecipeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    private inline fun <reified A> providesFoodApi(okhttpCallFactory: dagger.Lazy<Call.Factory>, json: Json): A {
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
            .callFactory { okhttpCallFactory.get().newCall(it) }
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create()
    }

    private inline fun <reified A> providesUserApi(okhttpCallFactory: dagger.Lazy<Call.Factory>, json: Json): A {
        return Retrofit.Builder()
            .baseUrl("http://46.202.170.205:8080")
            .callFactory { okhttpCallFactory.get().newCall(it) }
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory =
        OkHttpClient.Builder()
            .addInterceptor(
                okhttp3.logging.HttpLoggingInterceptor()
                    .apply {
                            setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY)
                    },
            )
            .build()

    @Singleton
    @Provides
    fun provideRecipeApi(okhttpCallFactory: dagger.Lazy<Call.Factory>,json: Json): RecipeApi =
        providesFoodApi(okhttpCallFactory = okhttpCallFactory, json = json)

    @Singleton
    @Provides
    fun provideAuthApi(okhttpCallFactory: dagger.Lazy<Call.Factory>,json: Json): AuthApi =
        providesUserApi(okhttpCallFactory = okhttpCallFactory, json = json)

    @Singleton
    @Provides
    fun provideFavoriteApi(okhttpCallFactory: dagger.Lazy<Call.Factory>,json: Json): FavoriteApi =
        providesUserApi(okhttpCallFactory = okhttpCallFactory,json = json)


}