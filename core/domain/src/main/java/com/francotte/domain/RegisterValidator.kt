package com.francotte.domain

object RegisterValidator {

    private val nameRegex = Regex("^(?=.{6,}\$)(?!.* {2,})(?!.* \$)[^\\n]+\$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d).{6,}$")

    fun isValidName(name: String): Boolean = nameRegex.matches(name)
    fun isValidEmail(email: String): Boolean = emailRegex.matches(email)
    fun isValidPassword(password: String): Boolean = passwordRegex.matches(password)
    fun isPasswordConfirmed(password: String, confirmation: String): Boolean = password == confirmation
}
