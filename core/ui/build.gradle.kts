plugins {
    alias(libs.plugins.myrecipesstore.android.library.compose)
}

android {
    namespace = "com.francotte.ui"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:domain"))
    api(project(":core:designsystem"))
    api(project(":core:inapp-rating"))
    api(project(":core:cmp"))
    api(project(":core:ads"))
    api(project(":core:billing"))

    implementation(libs.coil.compose)
    api(libs.androidx.metrics)
    implementation(libs.androidx.tracing.ktx)
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.kotlinx.metadata.jvm)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android)
    implementation(libs.hilt.core)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
    androidTestImplementation(libs.androidx.ui.test.manifest)
}
