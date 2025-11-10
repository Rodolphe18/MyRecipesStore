package com.francotte.common


import java.util.concurrent.atomic.AtomicInteger

object ScreenCounter {

    var screenCount = AtomicInteger(0)

    fun increment() {
        screenCount.incrementAndGet()
    }

}