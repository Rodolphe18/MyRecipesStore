package com.francotte.auth.strategy

interface LoginCredentials

data class EmailPasswordCredentials(
    val userNameOrMail: String,
    val password: String,
) : LoginCredentials

data class GoogleCredentials(val idToken: String) : LoginCredentials
