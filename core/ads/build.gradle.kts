plugins {
    alias(libs.plugins.myrecipesstore.android.library.compose)
    alias(libs.plugins.myrecipesstore.hilt)
}

android {
    namespace = "com.francotte.core.ads"
}

dependencies {

    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:cmp"))
    implementation(project(":core:premium"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.play.services.ads)
    implementation(libs.play.services.ads.lite)
    implementation(libs.androidx.material3)
}
