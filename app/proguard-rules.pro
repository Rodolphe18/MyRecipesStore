#######################################
# Jetpack Compose
#######################################
-keep class androidx.compose.** { *; }
-keep class androidx.activity.ComponentActivity { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-dontwarn androidx.compose.**
-dontwarn kotlin.Unit

#######################################
# Hilt / Dagger
#######################################
-keep class dagger.** { *; }
-keep interface dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }
-dontwarn dagger.**
-dontwarn javax.inject.**
-dontwarn dagger.hilt.**

#######################################
# Room
#######################################
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

#######################################
# Navigation Compose
#######################################
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

#######################################
# Lifecycle ViewModel & Runtime
#######################################
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

#######################################
# Coil (Image loading)
#######################################
-keep class coil.** { *; }
-dontwarn coil.**

#######################################
# Retrofit + Serialization
#######################################
-keepattributes Signature, Exceptions, *Annotation*
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Retrofit HTTP annotations
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

#######################################
# Kotlin Coroutines
#######################################
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

#######################################
# Google Play Services (Ads / Auth)
#######################################
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

#######################################
# Facebook SDK
#######################################
-keep class com.facebook.** { *; }
-dontwarn com.facebook.**

#######################################
# WorkManager
#######################################
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

#######################################
# SplashScreen API
#######################################
-keep class androidx.core.splashscreen.** { *; }
-dontwarn androidx.core.splashscreen.**

#######################################
# Billing
#######################################
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

#######################################
# DataStore / ProtoBuf
#######################################
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

#######################################
# OkHttp Logging Interceptor
#######################################
-keep class okhttp3.logging.** { *; }
-dontwarn okhttp3.logging.**

#######################################
# Debug/Test-only dependencies (à ignorer en prod)
#######################################
-dontwarn io.mockk.**
-dontwarn org.junit.**
-dontwarn androidx.test.**
-dontwarn com.google.testing.**
-dontwarn androidx.benchmark.**

#######################################
# UIAutomator (test only)
#######################################
-dontwarn androidx.test.uiautomator.**


