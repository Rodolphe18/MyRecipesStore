package com.francotte.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token:String, val user: NetworkUser)


