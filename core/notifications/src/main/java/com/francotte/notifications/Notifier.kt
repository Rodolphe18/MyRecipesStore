package com.francotte.notifications

interface Notifier {
    fun postNotification(title: String, body: String, idMeal: String?)
}
