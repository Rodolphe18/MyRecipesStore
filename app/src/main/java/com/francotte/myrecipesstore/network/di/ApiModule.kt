package com.francotte.myrecipesstore.network.di

import android.content.Context
import android.util.Log
import com.francotte.myrecipesstore.API_KEY
import com.francotte.myrecipesstore.manager.AuthManager
import com.francotte.myrecipesstore.network.api.AuthApi
import com.francotte.myrecipesstore.network.api.FavoriteApi
import com.francotte.myrecipesstore.network.api.RecipeApi
import com.francotte.myrecipesstore.network.utils.HttpLoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.GzipSource
import okio.buffer
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object
ApiModule {

    @Provides
    @Singleton
    fun provideCustomDns(): Dns = object :Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return if (hostname == "www.myrecipesstore18.com" || hostname == "myrecipesstore18.com") {
                listOf(InetAddress.getByName("46.202.170.205"))
            } else {
                Dns.SYSTEM.lookup(hostname)
            }
        }
    }

    private inline fun <reified A> providesFoodApi(client: OkHttpClient, json: Json): A {
        return Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v2/$API_KEY/")
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create()
    }

    private inline fun <reified A> providesUserApi(client: OkHttpClient, json: Json): A {
        return Retrofit.Builder()
            .baseUrl("https://www.myrecipesstore18.com")
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create()
    }


    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        sharedExecutor: ExecutorService,
        dns: Dns
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024L // 10 MB cache
        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)
        val gzipInterceptor = Interceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            val contentEncoding = originalResponse.header("Content-Encoding")

            if (contentEncoding != null && contentEncoding.equals("gzip", ignoreCase = true)) {
                val responseBody = originalResponse.body ?: return@Interceptor originalResponse
                try {
                    val gzipSource = GzipSource(responseBody.source())
                    val decompressedSource = gzipSource.buffer()
                    val newResponseBody = object : ResponseBody() {
                        override fun contentType() = responseBody.contentType()
                        override fun contentLength() = -1L
                        override fun source() = decompressedSource
                    }
                    return@Interceptor originalResponse.newBuilder()
                        .removeHeader("Content-Encoding")
                        .body(newResponseBody)
                        .build()
                } catch (e: IOException) {
                    Log.e("GzipInterceptor", "Erreur de décompression: ${e.message}")
                    return@Interceptor originalResponse
                }
            }
            originalResponse
        }
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .dns(dns)
            .cache(cache)
            .dispatcher(Dispatcher(sharedExecutor))
            .addInterceptor(gzipInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }


    @Singleton
    @Provides
    fun provideRecipeApi(okhttpCall: OkHttpClient, json: Json): RecipeApi =
        providesFoodApi(client = okhttpCall, json = json)

    @Singleton
    @Provides
    fun provideAuthApi(okhttpCall: OkHttpClient, json: Json): AuthApi {
        return providesUserApi(client = okhttpCall, json = json)
    }

    @Singleton
    @Provides
    fun provideFavoriteApi(okhttpCall: OkHttpClient, json: Json): FavoriteApi =
        providesUserApi(client = okhttpCall, json = json)


}