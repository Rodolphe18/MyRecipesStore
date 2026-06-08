package com.francotte.data.repository

import com.francotte.data.interfaces.UserDataRepository
import com.francotte.datastore.FoodPreferencesDataSource
import com.francotte.model.UserData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow


class LocalUserDataRepository @Inject constructor(
    private val foodPreferencesDataSource: FoodPreferencesDataSource
) : UserDataRepository {


    override val userData: Flow<UserData> = foodPreferencesDataSource.userData

    override suspend fun setFavoritesIds(favoritesIds: Set<String>) {
        foodPreferencesDataSource.setFavoritesIds(favoritesIds)
    }

    override suspend fun setFavoriteId(favoriteId: String, isFavorite: Boolean) {
        foodPreferencesDataSource.setFavoriteId(favoriteId, isFavorite)
    }

    override suspend fun updateUserInfo(
        isConnected: Boolean,
        name: String,
        userId: Long,
        userToken: String,
        userEmail: String,
        userImage: String
    ) {
        foodPreferencesDataSource.updateUserInfo(
            isConnected,
            name,
            userId,
            userToken,
            userEmail,
            userImage
        )
    }

    override suspend fun deleteFavoriteIds() {
        foodPreferencesDataSource.deleteFavoriteIds()
    }

    override suspend fun deleteUser() {
        foodPreferencesDataSource.deleteUser()
    }

    override suspend fun isFavoriteLocal(recipeId: String): Boolean {
        return foodPreferencesDataSource.isFavoriteLocal(recipeId)
    }

    override suspend fun upsertPendingFavorite(
        recipeId: String,
        desiredFavorite: Boolean
    ) {
        foodPreferencesDataSource.upsertPendingFavorite(recipeId, desiredFavorite)
    }

    override suspend fun removePendingFavorite(recipeId: String) {
        foodPreferencesDataSource.removePendingFavorite(recipeId)
    }

    override suspend fun getPendingFavorites(): List<Pair<String, Boolean>> {
        return foodPreferencesDataSource.getPendingFavorites()
    }

    override suspend fun clearPendingFavorites() {
        foodPreferencesDataSource.clearPendingFavorites()
    }

    override suspend fun setPremium(isPremium: Boolean) {
        foodPreferencesDataSource.setPremium(isPremium)
    }

    override suspend fun incrementLaunchCount(): Int {
        return  foodPreferencesDataSource.incrementLaunchCount()
    }

    override suspend fun setHasRated(hasRated: Boolean) {
        foodPreferencesDataSource.setHasRated(hasRated)
    }

    override suspend fun setLastPromptLaunch(lastPromptLaunch: Int) {
        foodPreferencesDataSource.setLastPromptLaunch(lastPromptLaunch)
    }


}
