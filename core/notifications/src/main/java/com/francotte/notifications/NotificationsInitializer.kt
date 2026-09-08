package com.francotte.notifications

/** Initializes notification channels and starts subscribing to recipe notifications. */
interface NotificationsInitializer {
    /** Called at application startup; subscription completes asynchronously. */
    fun initialize()
}
