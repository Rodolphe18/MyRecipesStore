plugins {
    alias(libs.plugins.myrecipesstore.android.library.compose)
    alias(libs.plugins.myrecipesstore.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.francotte.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.savedstate.compose)
    implementation(libs.androidx.lifecycle.viewModel.navigation3)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
