package com.francotte.myrecipesstore.user

import com.francotte.myrecipesstore.protobuf.UserInfo

data class UserData(
    val userInfo:UserInfo,
    val favoriteRecipesIds: Set<String>,
)