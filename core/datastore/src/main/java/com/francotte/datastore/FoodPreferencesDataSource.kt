package com.francotte.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.francotte.datastore_proto.User
import com.francotte.datastore_proto.UserPreferences
import com.francotte.datastore_proto.copy
import com.francotte.datastore_proto.pendingFavoriteAction
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.inject.Singleton

class FoodPreferencesDataSource @Inject constructor(private val userPreferences: DataStore<UserPreferences>) {

    val userData = userPreferences.data
            .map { prefs ->
                UserData(
                    userId = prefs.userInfo.user.id,
                    userName = prefs.userInfo.user.userName,
                    connectionMethod = prefs.toConnectionMethod(),
                    email = prefs.userInfo.user.email,
                    image = prefs.userInfo.user.image,
                    isConnected = prefs.userInfo.connected,
                    token = prefs.userInfo.token,
                    favoriteRecipesIds = prefs.favoritesIdsMap.keys,
                    pendingFavorites = prefs.pendingFavoritesMap.mapValues { it.value.desiredFavorite },
                    isPremium = prefs.userInfo.premium,
                    launchCount = prefs.launchCount,
                    hasRated = prefs.hasRated,
                    lastPromptLaunch = prefs.lastPromptLaunch,
                )
            }

    suspend fun setFavoritesIds(favoritesIds: Set<String>) {
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

    suspend fun setFavoriteId(
        favoriteId: String,
        isFavorite: Boolean,
    ) {
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

    suspend fun updateUserInfo(
        isConnected: Boolean,
        name: String,
        userId: Long,
        userToken: String,
        userEmail: String,
        userImage: String,
    ) {
        try {
            userPreferences.updateData { currentPrefs ->
                currentPrefs.copy {
                    this.userInfo =
                        userInfo {
                            connected = isConnected
                            user =
                                user {
                                    userName = name
                                    id = userId
                                    email = userEmail
                                    image = userImage
                                }
                            token = userToken
                            premium = currentPrefs.userInfo.premium
                        }
                }
            }
        } catch (ioException: IOException) {
            Log.e("FoodPreferences", "Failed to update user info", ioException)
        }
    }

    suspend fun deleteFavoriteIds() {
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

    suspend fun deleteUser() {
        userPreferences.updateData {
            it.copy {
                this.favoritesIds.clear()
                this.userInfo =
                    userInfo {
                        connected = false
                        user =
                            user {
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

    suspend fun isFavoriteLocal(recipeId: String): Boolean =
        userPreferences.data
            .first()
            .favoritesIdsMap
            .containsKey(recipeId)

    suspend fun upsertPendingFavorite(
        recipeId: String,
        desiredFavorite: Boolean,
    ) {
        userPreferences.updateData { prefs ->
            prefs.copy {
                this.pendingFavorites.put(
                    recipeId,
                    pendingFavoriteAction {
                        this.recipeId = recipeId
                        this.desiredFavorite = desiredFavorite
                        this.createdAt = System.currentTimeMillis()
                    },
                )
            }
        }
    }

    suspend fun removePendingFavorite(recipeId: String) {
        userPreferences.updateData { prefs ->
            prefs.copy {
                this.pendingFavorites.remove(recipeId)
            }
        }
    }

    suspend fun getPendingFavorites(): List<Pair<String, Boolean>> {
        val prefs = userPreferences.data.first()
        return prefs.pendingFavoritesMap.values.map { it.recipeId to it.desiredFavorite }
    }

    suspend fun clearPendingFavorites() {
        userPreferences.updateData { prefs ->
            prefs.copy {
                this.pendingFavorites.clear()
            }
        }
    }

    suspend fun incrementLaunchCount(): Int {
        var newCount = 0
        userPreferences.updateData { prefs ->
            newCount = prefs.launchCount + 1
            prefs.copy { launchCount = newCount }
        }
        return newCount
    }

    suspend fun setHasRated(hasRated: Boolean) {
        userPreferences.updateData { prefs ->
            prefs.copy { this.hasRated = hasRated }
        }
    }

    suspend fun setLastPromptLaunch(lastPromptLaunch: Int) {
        userPreferences.updateData { prefs ->
            prefs.copy { this.lastPromptLaunch = lastPromptLaunch }
        }
    }

    suspend fun setPremium(isPremium: Boolean) {
        userPreferences.updateData { currentPrefs ->
            currentPrefs.copy {
                this.userInfo = userInfo {
                    connected = currentPrefs.userInfo.connected
                    user = currentPrefs.userInfo.user
                    token = currentPrefs.userInfo.token
                    premium = isPremium
                }
            }
        }
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
