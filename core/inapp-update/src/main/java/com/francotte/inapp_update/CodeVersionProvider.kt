package com.francotte.inapp_update

import android.content.Context
import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

interface CodeVersionProvider {
    fun currentVersionCode(): Int
}

class AndroidVersionCodeProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : CodeVersionProvider {
    override fun currentVersionCode(): Int {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) packageInfo.versionCode else packageInfo.longVersionCode.toInt()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class VersionModule {
    @Binds
    abstract fun bindVersionCodeProvider(
        impl: AndroidVersionCodeProvider
    ): CodeVersionProvider
}
