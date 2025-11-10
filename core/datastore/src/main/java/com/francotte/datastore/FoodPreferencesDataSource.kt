package com.francotte.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.francotte.datastore_proto.User
import com.francotte.datastore_proto.UserPreferences
import com.francotte.datastore_proto.copy
import com.francotte.datastore_proto.user
import com.francotte.datastore_proto.userInfo
import com.francotte.model.ConnectionMethod
import com.francotte.model.UserData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Singleton


class FoodPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) : UserDataSource {
    override val userData = userPreferences.data
        .map {
            UserData(
                userId = it.userInfo.user.id,
                userName = it.userInfo.user.userName,
                connectionMethod = it.toConnectionMethod(),
                email = it.userInfo.user.email,
                image = it.userInfo.user.image,
                isConnected = it.userInfo.connected,
                token = it.userInfo.token,
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

    override suspend fun updateUserInfo(
        isConnected: Boolean,
        name: String,
        userId: Long,
        userToken: String,
        userEmail: String,
        profilPicture: String
    ) {
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

    override suspend fun deleteUser() {
        userPreferences.updateData {
            it.copy {
                this.favoritesIds.clear()
                this.userInfo = userInfo {
                    connected = false
                    user = user {
                        userName = ""
                        id = -1
                        email = ""
                        image = ""
                    }
                    token = ""
                }
            }
        }
    }

}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providesUserPreferencesDataStore(
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

fun UserPreferences.toConnectionMethod(): ConnectionMethod {
    val protoMethod = userInfo.user.method
    return when (protoMethod) {
        User.ConnectionMethod.EMAIL -> ConnectionMethod.EMAIL
        User.ConnectionMethod.FACEBOOK -> ConnectionMethod.FACEBOOK
        User.ConnectionMethod.GOOGLE -> ConnectionMethod.GOOGLE
        User.ConnectionMethod.UNRECOGNIZED -> ConnectionMethod.EMAIL
    }
}