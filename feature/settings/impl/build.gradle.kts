plugins {
    alias(libs.plugins.myrecipesstore.android.feature.impl)
}

android {
    namespace = "com.francotte.settings"
}

dependencies {

    api(project(":core:data"))
    api(project(":core:domain"))
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":feature:login:impl"))
    api(project(":core:ui"))
    api(project(":core:billing"))
    api(project(":core:navigation"))

    api(project(":feature:settings:api"))

    implementation(libs.kotlinx.metadata.jvm)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.coil.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.billing)
    implementation(libs.billing.ktx)
    implementation("androidx.browser:browser:1.8.0")
}
