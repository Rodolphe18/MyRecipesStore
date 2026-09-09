plugins {
    alias(libs.plugins.myrecipesstore.android.feature.impl)
}

android {
    namespace = "com.francotte.add_recipe"
}

dependencies {

    api(project(":core:data"))
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:ui"))
    api(project(":core:domain"))

    api(project(":feature:add_recipe:api"))
    api(project(":feature:login:api"))
    implementation(project(":feature:record_video:api"))

    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.kotlinx.metadata.jvm)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.activity.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
