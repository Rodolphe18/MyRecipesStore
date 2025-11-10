package com.francotte.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge

/**
 * There isn’t a built-in mechanism for a user-triggered restart to refresh the screen
 * We create our own implementation of SharingStarted interface to trigger the restart of an existing flow
 * for more information, read :
 * https://medium.com/@der.x/restartable-stateflows-in-compose-46316ce670a9
 */

interface SharingRestartable: SharingStarted {
    fun restart()
}

private data class SharingRestartableImpl(private val sharingStarted: SharingStarted): SharingRestartable {

    private val restartFlow = MutableSharedFlow<SharingCommand>(extraBufferCapacity = 2)

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> {
        return merge(restartFlow, sharingStarted.command(subscriptionCount))
    }
    override fun restart() {
        restartFlow.tryEmit(SharingCommand.STOP_AND_RESET_REPLAY_CACHE)
        restartFlow.tryEmit(SharingCommand.START)
    }

}

fun SharingStarted.makeRestartable() : SharingRestartable {
    return SharingRestartableImpl(this)
}


private const val StopTimeoutMillis: Long = 5000

val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(StopTimeoutMillis)

val restartableWhileSubscribed = WhileUiSubscribed.makeRestartable()
