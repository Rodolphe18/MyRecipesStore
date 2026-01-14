package com.francotte.testing

import com.francotte.datastore.UserDataRepository
import com.francotte.model.ConnectionMethod
import com.francotte.model.UserData
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull


val emptyUserData = UserData(
    userId = -1L,
    userName = "Paul",
    connectionMethod = ConnectionMethod.EMAIL,
    email = "",
    image = "",
    isConnected = false,
    token = null,
    favoriteRecipesIds = emptySet(),
)

class TestUserDataRepository : UserDataRepository {

    private val _userData =
        MutableSharedFlow<UserData>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val currentUserData: UserData
        get() = _userData.replayCache.firstOrNull() ?: emptyUserData

    override val userData: Flow<UserData> = _userData.filterNotNull()

    override suspend fun setFavoritesIds(favoritesIds: Set<String>) {
        _userData.tryEmit(currentUserData.copy(favoriteRecipesIds = favoritesIds))
    }

    override suspend fun setFavoriteId(favoriteId: String, isFavorite: Boolean) {
        val current = currentUserData
        val updated = if (isFavorite) {
            current.favoriteRecipesIds + favoriteId
        } else {
            current.favoriteRecipesIds - favoriteId
        }
        _userData.tryEmit(current.copy(favoriteRecipesIds = updated))
    }

    override suspend fun updateUserInfo(
        isConnected: Boolean,
        name: String,
        userId: Long,
        userToken: String,
        userEmail: String,
        userImage: String
    ) {
        _userData.tryEmit(
            currentUserData.copy(
                isConnected = isConnected,
                userName = name,
                userId = userId,
                token = userToken,
                email = userEmail,
                image = userImage,
            )
        )
    }

    override suspend fun deleteFavoriteIds() {
        _userData.tryEmit(currentUserData.copy(favoriteRecipesIds = emptySet()))
    }

    override suspend fun deleteUser() {
       _userData.tryEmit(
            currentUserData.copy(
                isConnected = false,
                userName = "",
                userId = -1L,
                email = "",
                image = "",
                token = null,
                favoriteRecipesIds = emptySet(),
            )
        )
    }

    override suspend fun isFavoriteLocal(recipeId: String): Boolean {
        return false
    }

    override suspend fun upsertPendingFavorite(
        recipeId: String,
        desiredFavorite: Boolean
    ) {

    }

    override suspend fun removePendingFavorite(recipeId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getPendingFavorites(): List<Pair<String, Boolean>> {
        TODO("Not yet implemented")
    }


    fun setUserData(userData: UserData) {
        _userData.tryEmit(userData)
    }
}


