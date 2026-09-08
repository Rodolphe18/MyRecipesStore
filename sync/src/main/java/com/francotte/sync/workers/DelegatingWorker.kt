package com.francotte.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

/**
 * An entry point to retrieve the [HiltWorkerFactory] at runtime.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
    fun hiltWorkerFactory(): HiltWorkerFactory
}

private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

/**
 * Adds metadata to a WorkRequest to identify what [CoroutineWorker] the [DelegatingWorker] should
 * delegate to. [extras] carries the worker's own input data, if it needs any.
 */
internal fun KClass<out CoroutineWorker>.delegatedData(extras: Data = Data.EMPTY): Data =
    Data.Builder()
        .putAll(extras)
        .putString(WORKER_CLASS_NAME, qualifiedName)
        .build()

/**
 * A worker that delegates the work to another [CoroutineWorker] constructed with a
 * [HiltWorkerFactory].
 *
 * WorkManager instantiates workers by reflection and only knows the `(Context, WorkerParameters)`
 * constructor, so a worker that needs injected dependencies normally forces the app module to
 * install a custom [androidx.work.WorkerFactory] in the WorkManager `Configuration` singleton.
 *
 * This class has the constructor WorkManager expects, and pulls the [HiltWorkerFactory] out of the
 * graph itself. Every other worker in this module can then stay an ordinary `@HiltWorker`, and the
 * app module owns nothing.
 */
class DelegatingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val workerClassName =
        workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""

    private val delegateWorker =
        EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)
            .hiltWorkerFactory()
            .createWorker(appContext, workerClassName, workerParams)
            as? CoroutineWorker
            ?: throw IllegalArgumentException("Unable to find appropriate worker: $workerClassName")

    override suspend fun getForegroundInfo(): ForegroundInfo =
        delegateWorker.getForegroundInfo()

    override suspend fun doWork(): Result =
        delegateWorker.doWork()
}
