package com.francotte.myrecipesstore.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.francotte.myrecipesstore.protobuf.UserPreferences
import com.francotte.myrecipesstore.protobuf.copy
import com.francotte.myrecipesstore.protobuf.user
import com.francotte.myrecipesstore.protobuf.userInfo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Singleton

interface UserDataSource {
    val userData: Flow<UserData>
    suspend fun setFavoritesIds(favoritesIds: Set<String>)
    suspend fun setFavoriteId(favoriteId: String, isFavorite: Boolean)
    suspend fun updateUserInfo(isConnected:Boolean, name:String="", userId:Long=0, userToken:String="", email:String="",image: String="")
    suspend fun deleteFavoriteIds()
}

@Module
@InstallIn(SingletonComponent::class)
interface UserDataModule {

    @Suppress("unused")
    @Singleton
    @Binds
    fun bindUserDataModule(foodPreferencesDataSource: FoodPreferencesDataSource): UserDataSource
}


class FoodPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
): UserDataSource {
   override val userData = userPreferences.data
        .map {
            UserData(
                userInfo = it.userInfo,
                favoriteRecipesIds = it.favoritesIdsMap.keys
            )
        }

   override suspend fun setFavoritesIds(favoritesIds: Set<String>) {
        try {
            userPreferences.updateData {
                it.copy {
                    this.favoritesIds.clear()
                    this.favoritesIds.putAll(favoritesIds.associateWith { true })
                }
            }
        } catch (ioException: IOException) {
            Log.e("FoodPreferences", "Failed to update favorite ids", ioException)
        }
    }

   override suspend fun setFavoriteId(favoriteId: String, isFavorite: Boolean) {
        try {
            userPreferences.updateData {
                it.copy {
                    if (isFavorite) {
                        this.favoritesIds.put(favoriteId, true)
                    } else {
                        this.favoritesIds.remove(favoriteId)
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e("FoodPreferences", "Failed to update user favorites ids", ioException)
        }
    }

   override suspend fun updateUserInfo(isConnected:Boolean, name:String, userId:Long, userToken:String, userEmail:String, profilPicture: String) {
        try {
        userPreferences.updateData {
            it.copy {
                this.userInfo = userInfo {
                    connected = isConnected
                    user = user {
                        userName = name
                        id = userId
                        email = userEmail
                        image = profilPicture
                    }
                    token = userToken

                }
            }
        }
        } catch (ioException: IOException) {
            Log.e("FoodPreferences", "Failed to update user info", ioException)
        }
    }

    override suspend fun deleteFavoriteIds() {
        try {
            userPreferences.updateData {
                it.copy {
                    this.favoritesIds.clear()
                 }
            }
        } catch (ioException: IOException) {
            Log.e("FoodPreferences", "Failed to update favorite ids", ioException)
        }
    }

}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    internal fun providesUserPreferencesDataStore(
        @ApplicationContext context: Context,
        userPreferencesSerializer: UserPreferencesSerializer,
        sharedExecutor: ExecutorService
    ): DataStore<UserPreferences> =
        DataStoreFactory.create(
            serializer = userPreferencesSerializer,
            scope = CoroutineScope(sharedExecutor.asCoroutineDispatcher() + SupervisorJob()),
        ) {
            context.dataStoreFile("user_preferences.pb")
        }
}
