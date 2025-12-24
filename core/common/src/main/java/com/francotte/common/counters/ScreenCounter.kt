package com.francotte.common.counters


import java.util.concurrent.atomic.AtomicInteger

object ScreenCounter {

    var screenCount = AtomicInteger(0)

    fun increment() {
        screenCount.incrementAndGet()
    }

}