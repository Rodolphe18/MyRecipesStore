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
# Serialization
#######################################
-keepattributes Signature, Exceptions, *Annotation*
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

#######################################
# Kotlin Coroutines
#######################################
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}


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

-dontwarn javax.annotation.**
