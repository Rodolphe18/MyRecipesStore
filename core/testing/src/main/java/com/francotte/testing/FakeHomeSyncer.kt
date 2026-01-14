package com.francotte.testing

import com.francotte.ui.HomeSyncer
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class FakeHomeSyncer(
    private val delayMs: Long = 0L,
    private var nextResult: Result<Unit> = Result.success(Unit)
) : HomeSyncer {

    // Spy: nombre d'appels + derniers paramètres
    private val _callCount = AtomicInteger(0)
    val callCount: Int get() = _callCount.get()

    @Volatile
    var lastForce: Boolean? = null
        private set

    fun willSucceed() {
        nextResult = Result.success(Unit)
    }

    fun willFail(error: Throwable = IOException("Fake sync failed")) {
        nextResult = Result.failure(error)
    }

    override suspend fun syncLatest(force: Boolean) {
        _callCount.incrementAndGet()
        lastForce = force

        if (delayMs > 0) delay(delayMs)

        nextResult.getOrThrow()
    }
}