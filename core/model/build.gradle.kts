plugins {
    alias(libs.plugins.myrecipesstore.android.library)
}

android {
    namespace = "com.francotte.model"
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.annotation)
}
