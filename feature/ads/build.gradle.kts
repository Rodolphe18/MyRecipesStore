plugins {
    alias(libs.plugins.myrecipesstore.android.library.compose)
    alias(libs.plugins.myrecipesstore.hilt)
}

android {
    namespace = "com.francotte.feature.ads"
}

dependencies {

    api(project(":core:ads"))
    api(project(":core:ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.material3)
}
