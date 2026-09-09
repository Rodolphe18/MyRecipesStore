plugins {
    alias(libs.plugins.myrecipesstore.android.library.compose)
}

android {
    namespace = "com.francotte.screenshot_testing"
}

dependencies {

    api(project(":core:designsystem"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.compose.ui.test.junit4)
    implementation(libs.androidx.compose.material3.window.size.class1)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    api(libs.roborazzi)
    api(libs.roborazzi.accessibility.check)
    testImplementation(libs.androidx.activity.compose)
    testImplementation(libs.robolectric)

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.9.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.9.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
