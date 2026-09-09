plugins {
    alias(libs.plugins.myrecipesstore.android.library)
    alias(libs.plugins.myrecipesstore.hilt)
}

android {
    namespace = "com.francotte.billing"
}

dependencies {

    implementation(project(":core:data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.billing)
    implementation(libs.billing.ktx)
}
