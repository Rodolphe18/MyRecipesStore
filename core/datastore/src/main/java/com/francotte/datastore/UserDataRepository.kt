package com.francotte.datastore

import com.francotte.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>
    suspend fun setFavoritesIds(favoritesIds: Set<String>)
    suspend fun setFavoriteId(favoriteId: String, isFavorite: Boolean)
    suspend fun updateUserInfo(isConnected:Boolean, name:String="", userId:Long=0, userToken:String="", userEmail:String="",userImage: String="")
    suspend fun deleteFavoriteIds()
    suspend fun deleteUser()
    suspend fun isFavoriteLocal(recipeId: String): Boolean
    suspend fun upsertPendingFavorite(recipeId: String, desiredFavorite: Boolean)
    suspend fun removePendingFavorite(recipeId: String)
    suspend fun getPendingFavorites(): List<Pair<String, Boolean>>
    suspend fun clearPendingFavorites()
}

