package com.francotte.myrecipesstore.auth

import android.content.Context
import androidx.datastore.dataStore
import com.francotte.myrecipesstore.UserInfoSerializer
import com.francotte.myrecipesstore.protobuf.UserInfo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userInfoDataStore by dataStore("userInfo", UserInfoSerializer())

interface AuthStorage {
    val userInfo: Flow<UserInfo>
    suspend fun updateUserInfo(userInfo: UserInfo)
}

class DatastoreAuthStorage @Inject constructor(@ApplicationContext private val context: Context) :
    AuthStorage {
    override val userInfo: Flow<UserInfo>
        get() = context.userInfoDataStore.data

    override suspend fun updateUserInfo(userInfo: UserInfo) {
        context.userInfoDataStore.updateData { userInfo }
    }

}

@Module
@InstallIn(SingletonComponent::class)
interface AuthStorageModule {

    @Singleton
    @Binds
    fun bindDatastoreAuthStorage(storage: DatastoreAuthStorage): AuthStorage
}