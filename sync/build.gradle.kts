plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.plugin)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.francotte.sync"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {

    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))
    implementation(project(":core:data"))

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    ksp(libs.hilt.compiler)
    // Generates the WorkerAssistedFactory for every @HiltWorker. Without it the annotation is
    // silently ignored and DelegatingWorker finds nothing to delegate to.
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.core)
    implementation(libs.androidx.hilt.work)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.retrofit.core)
}
