package com.francotte.network.di

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonConverterFactory @Inject constructor(private val json: Json) : Converter.Factory() {

    @OptIn(ExperimentalSerializationApi::class)
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation?>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {

        val deserializer = json.serializersModule.serializer(type) as DeserializationStrategy<Any?>

        return object : Converter<ResponseBody,Any?> {
            override fun convert(body: ResponseBody): Any? = body.use { responseBody ->
                val source = responseBody.source()
                if (responseBody.contentLength() == 0L || source.exhausted()) return null
                json.decodeFromBufferedSource(deserializer,source)
            }
        }

    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        @Suppress("UNCHECKED_CAST")
        val serializer = json.serializersModule.serializer(type) as SerializationStrategy<Any>
        val contentType = "application/json".toMediaType()

        return object : Converter<Any, RequestBody> {
            @OptIn(ExperimentalSerializationApi::class)
            override fun convert(value: Any): RequestBody {
                return object : RequestBody() {
                    override fun contentType() = contentType
                    override fun writeTo(sink: BufferedSink) {
                        json.encodeToBufferedSink(serializer, value, sink)
                    }
                }
            }
        }
    }

}


