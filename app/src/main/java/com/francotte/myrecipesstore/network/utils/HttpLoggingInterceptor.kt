/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francotte.myrecipesstore.network.utils

import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.util.TreeSet
import java.util.concurrent.TimeUnit

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors] or as a [network interceptor][OkHttpClient.networkInterceptors].
 *
 * The format of the logs created by this class should not be considered stable and may
 * change slightly between releases. If you need a stable logging format, use your own interceptor.
 */
class HttpLoggingInterceptor @JvmOverloads constructor(
    private val logger: Logger = Logger.DEFAULT
) : Interceptor {

    @Volatile
    private var headersToRedact = emptySet<String>()

    @set:JvmName("level")
    @Volatile
    var level = Level.NONE

    @Suppress("unused")
    enum class Level {
        /** No logs. */
        NONE,

        /**
         * Logs request and response lines.
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * ```
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * ```
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * ```
         */
        BODY
    }

    fun interface Logger {
        fun log(message: String)

        companion object {
            /** A [Logger] defaults output appropriate for the current platform. */
            @JvmField
            val DEFAULT: Logger = DefaultLogger()

            private class DefaultLogger : Logger {
                override fun log(message: String) {
                    Log.d("HTTP", message)
                }
            }
        }
    }

    @Suppress("unused")
    fun redactHeader(name: String) {
        val newHeadersToRedact = TreeSet(String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact += headersToRedact
        newHeadersToRedact += name
        headersToRedact = newHeadersToRedact
    }

    /**
     * Sets the level and returns this.
     *
     * This was deprecated in OkHttp 4.0 in favor of the [level] val. In OkHttp 4.3 it is
     * un-deprecated because Java callers can't chain when assigning Kotlin vals. (The getter remains
     * deprecated).
     */
    fun setLevel(level: Level) = apply {
        this.level = level
    }

    @JvmName("-deprecated_level")
    @Deprecated(
        message = "moved to var",
        replaceWith = ReplaceWith(expression = "level"),
        level = DeprecationLevel.ERROR
    )
    fun getLevel(): Level = level

    @Throws(IOException::class)
    @Suppress("ComplexMethod")
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = this.level

        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        val logBody = level == Level.BODY
        val logHeaders = logBody || level == Level.HEADERS

        val requestBody = request.body

        val connection = chain.connection()
        var requestStartMessage =
            "--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}"
        if (!logHeaders && requestBody != null) {
            requestStartMessage += " (${requestBody.contentLength()}-byte body)"
        }
        logger.log(requestStartMessage)

        if (logHeaders) {
            val headers = request.headers

            if (requestBody != null) {
                // Request body headers are only present when installed as a network interceptor. When not
                // already present, force them to be included (if available) so their values are known.
                requestBody.contentType()?.let {
                    if (headers["Content-Type"] == null) {
                        logger.log("Content-Type: $it")
                    }
                }
                if (requestBody.contentLength() != -1L) {
                    if (headers["Content-Length"] == null) {
                        logger.log("Content-Length: ${requestBody.contentLength()}")
                    }
                }
            }

            for (i in 0 until headers.size) {
                logHeader(headers, i)
            }

            when {
                !logBody || requestBody == null -> logger.log("--> END ${request.method}")
                bodyHasUnknownEncoding(request.headers) -> logger.log("--> END ${request.method} (encoded body omitted)")
                requestBody.isDuplex() -> logger.log("--> END ${request.method} (duplex request body omitted)")
                requestBody.isOneShot() -> logger.log("--> END ${request.method} (one-shot body omitted)")
                requestBody is MultipartBody -> requestBody.log(request.method)
                else -> requestBody.defaultLog(request.method)
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.log("<-- HTTP FAILED: $e")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        logResponse(response, tookMs, logHeaders, logBody)

        return response
    }

    @Suppress("ComplexMethod")
    private fun logResponse(
        response: Response,
        tookMs: Long,
        logHeaders: Boolean,
        logBody: Boolean,
        isPriorResponse: Boolean = false
    ) {
        val priorResponse = response.priorResponse
        if (priorResponse != null) {
            logResponse(priorResponse, 0L, logHeaders, logBody = false, isPriorResponse = true)
        }
        val responseBody = if (isPriorResponse) null else requireNotNull(response.body)
        val contentLength = responseBody?.contentLength() ?: -1L
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        if (isPriorResponse) {
            logger.log(
                "<-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} ${response.request.url} (prior response)"
            )
        } else {
            logger.log(
                "<-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} ${response.request.url} (${tookMs}ms${if (!logHeaders) ", $bodySize body" else ""})"
            )
        }

        if (logHeaders) {
            val headers = response.headers
            for (i in 0 until headers.size) {
                logHeader(headers, i)
            }

            if (isPriorResponse || !logBody || !response.promisesBody()) {
                logger.log("<-- END HTTP")
            } else if (bodyHasUnknownEncoding(response.headers)) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody?.source() ?: Buffer()
                source.request(Long.MAX_VALUE) // Buffer the entire body.
                var buffer = source.buffer

                var gzippedLength: Long? = null
                if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                    gzippedLength = buffer.size
                    GzipSource(buffer.clone()).use { gzippedResponseBody ->
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    }
                }

                val contentType = responseBody?.contentType()
                val charset: Charset = contentType?.charset(UTF_8) ?: UTF_8

                if (!buffer.isProbablyUtf8()) {
                    logger.log("")
                    logger.log("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
                    return
                }

                if (contentLength != 0L) {
                    logger.log("")
                    logger.log(buffer.clone().readString(charset))
                }

                if (gzippedLength != null) {
                    logger.log("<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body)")
                } else {
                    logger.log("<-- END HTTP (${buffer.size}-byte body)")
                }
            }
        }
    }

    private fun RequestBody.defaultLog(method: String) {
        val buffer = Buffer()
        writeTo(buffer)

        val contentType = contentType()
        val charset: Charset = contentType?.charset(UTF_8) ?: UTF_8

        logger.log("")
        if (buffer.isProbablyUtf8()) {
            logger.log(buffer.readString(charset))
            logger.log("--> END $method (${contentLength()}-byte body)")
        } else {
            logger.log(
                "--> END $method (binary ${contentLength()}-byte body omitted)"
            )
        }
    }

    private fun MultipartBody.log(method: String) {
        logger.log(
            buildString {
                parts.forEach { part ->
                    val headers = part.headers
                    val body = part.body
                    append(DASHDASH).append(boundary).append(CRLF)

                    headers?.forEach { (name, value) ->
                        append(name).append(COLONSPACE).append(value).append(CRLF)
                    }

                    val contentType = body.contentType()
                    if (contentType != null) {
                        append("Content-Type: ").append(contentType).append(CRLF)
                    }

                    val contentLength = body.contentLength()
                    if (contentLength != -1L) {
                        append("Content-Length: ").append(contentLength).append(CRLF)
                    }

                    append(CRLF)

                    val buffer = Buffer()
                    body.writeTo(buffer)
                    val charset = contentType?.charset(UTF_8) ?: UTF_8
                    if (buffer.isProbablyUtf8()) {
                        append(buffer.readString(charset))
                    } else {
                        append("(binary $contentLength-byte body omitted)")
                    }
                    append(CRLF)
                }
                append(DASHDASH).append(boundary).append(DASHDASH).append(CRLF)
            }
        )
        logger.log("--> END $method (${contentLength()}-byte body)")
    }

    private fun logHeader(headers: Headers, i: Int) {
        val value = if (headers.name(i) in headersToRedact) "██" else headers.value(i)
        logger.log(headers.name(i) + ": " + value)
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }

    companion object {
        private const val COLONSPACE = ": "
        private const val DASHDASH = "--"
        private const val CRLF = "\r\n"

        private fun Buffer.isProbablyUtf8(): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = size.coerceAtMost(64)
                copyTo(prefix, 0, byteCount)
                for (i in 0 until 16) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (_: EOFException) {
                return false // Truncated UTF-8 sequence.
            }
        }
    }
}
