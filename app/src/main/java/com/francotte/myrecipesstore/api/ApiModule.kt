package com.francotte.myrecipesstore.api

import com.francotte.authmanagerforandroid.api.HttpLoggingInterceptor
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
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor())
                    .build()
            )
            .addConverterFactory(jsonConverterFactory)
            .build()
            .create()
    }

    @Singleton
    @Provides
    fun provideRecipeApi(jsonConverterFactory: JsonConverterFactory): RecipeApi = providesApi(jsonConverterFactory = jsonConverterFactory)

}