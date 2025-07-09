package com.francotte.myrecipesstore.network.model

import kotlinx.serialization.Serializable


@Serializable
data class AuthRequest(val username:String, val password:String)

@Serializable
data class GoogleIdTokenRequest(val id_token: String)

@Serializable
data class FacebookAccessTokenRequest(val access_token: String)