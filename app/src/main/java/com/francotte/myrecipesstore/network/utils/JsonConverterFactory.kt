package com.francotte.myrecipesstore.network.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ToJsonString

@Singleton
class JsonConverterFactory @Inject constructor(private val json: Json) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return JsonResponseBodyConverter(json, json.serializersModule.serializer(type))
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return JsonRequestBodyConverter(json, json.serializersModule.serializer(type))
    }

    override fun stringConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, String>? {
        return if (annotations.containsType<ToJsonString>()) {
            json.serializersModule.serializerOrNull(type)?.let { JsonStringConverter(json, it) }
        } else {
            null
        }
    }
}

private class JsonResponseBodyConverter<T>(private val json: Json, private val serializer: DeserializationStrategy<T>) :
    Converter<ResponseBody, T> {

    @OptIn(ExperimentalSerializationApi::class)
    override fun convert(value: ResponseBody): T? {
        return json.decodeFromBufferedSource(serializer, value.source())
    }
}

private class JsonRequestBodyConverter<T>(private val json: Json, private val serializer: SerializationStrategy<T>) :
    Converter<T, RequestBody> {

    @OptIn(ExperimentalSerializationApi::class)
    override fun convert(value: T): RequestBody {
        return object : RequestBody() {
            override fun contentType() = CONTENT_TYPE

            override fun writeTo(sink: BufferedSink) {
                json.encodeToBufferedSink(serializer, value, sink)
            }
        }
    }

    companion object {
        private val CONTENT_TYPE = "application/json".toMediaType()
    }
}

private class JsonStringConverter<T>(private val json: Json, private val serializer: SerializationStrategy<T>) :
    Converter<T, String> {

    override fun convert(value: T): String {
        return json.encodeToString(serializer, value)
    }
}

inline fun <reified A : Annotation> Array<out Annotation>.containsType(): Boolean {
    return any { it is A }
}

