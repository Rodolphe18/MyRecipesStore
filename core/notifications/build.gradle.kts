plugins {
    alias(libs.plugins.myrecipesstore.android.library)
    alias(libs.plugins.myrecipesstore.hilt)
}

android {
    namespace = "com.francotte.notifications"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
