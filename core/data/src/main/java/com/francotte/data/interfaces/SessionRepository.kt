package com.francotte.data.interfaces

interface SessionRepository {
    suspend fun logout()
    suspend fun deleteAccount()
}
