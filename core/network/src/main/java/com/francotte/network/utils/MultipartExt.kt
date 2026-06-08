package com.francotte.network.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun Uri?.toMultiPartBody(context: Context): MultipartBody.Part? =
    this?.let { uri ->
        val resolver = context.contentResolver
        val inputStream = resolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        val resizedBitmap = originalBitmap?.let {
            val maxSize = 800
            val scale = minOf(maxSize / it.width.toFloat(), maxSize / it.height.toFloat(), 1f)
            it.scale((it.width * scale).toInt(), (it.height * scale).toInt())
        }
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        FileOutputStream(file).use { out ->
            resizedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("image", file.name, requestFile)
    }
