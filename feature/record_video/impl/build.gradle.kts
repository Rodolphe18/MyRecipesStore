plugins {
    alias(libs.plugins.myrecipesstore.android.feature.impl)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.francotte.record_video"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:navigation"))
    api(project(":feature:record_video:api"))

    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.iconsExtended)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation("com.google.guava:guava:33.3.1-android")

    testImplementation(libs.junit)
}
