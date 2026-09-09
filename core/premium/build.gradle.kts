plugins {
    alias(libs.plugins.myrecipesstore.android.library)
    alias(libs.plugins.myrecipesstore.hilt)
}

android {
    namespace = "com.francotte.premium"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
