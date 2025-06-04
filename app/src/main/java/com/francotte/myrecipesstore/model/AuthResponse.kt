package com.francotte.myrecipesstore.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token:String, val user: CurrentUser)


