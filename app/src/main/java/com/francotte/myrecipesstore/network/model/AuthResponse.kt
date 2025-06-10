package com.francotte.myrecipesstore.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token:String, val user: CurrentUser)


