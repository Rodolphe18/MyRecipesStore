package com.francotte.domain


private val USER_NAME_REGEX = Regex("^(?=.{6,}\$)(?!.* {2,})(?!.* \$)[^\\n]+\$")
private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*\\d).{6,}$")

fun interface Validator {
    fun isValid(value: String): Boolean
}

val userNameValidator = Validator { it.matches(USER_NAME_REGEX) }
val emailValidator = Validator { it.matches(EMAIL_REGEX) }
val passwordValidator = Validator { it.matches(PASSWORD_REGEX) }

fun matchValidator(other: String) = Validator { it == other }
