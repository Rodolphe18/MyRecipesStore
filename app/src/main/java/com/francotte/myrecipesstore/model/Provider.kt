package com.francotte.myrecipesstore.model

import com.francotte.myrecipesstore.protobuf.User

enum class Provider(val fullName: String, val tag: String) {
    FACEBOOK("Facebook", "Facebook_connect"),
    GOOGLE("Google", "Google_connect"),
    EMAIL("Email", "Classic")
}

fun Provider.toConnectionMethod(): User.ConnectionMethod {
    return when (this) {
        Provider.FACEBOOK -> User.ConnectionMethod.FACEBOOK
        Provider.GOOGLE -> User.ConnectionMethod.GOOGLE
        Provider.EMAIL -> User.ConnectionMethod.EMAIL
    }
}

fun User.ConnectionMethod.toProvider(): Provider {
    return when (this) {
        User.ConnectionMethod.FACEBOOK -> Provider.FACEBOOK
        User.ConnectionMethod.GOOGLE -> Provider.GOOGLE
        User.ConnectionMethod.EMAIL -> Provider.EMAIL
        User.ConnectionMethod.UNRECOGNIZED -> Provider.EMAIL
    }
}