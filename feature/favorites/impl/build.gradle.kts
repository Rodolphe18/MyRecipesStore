plugins {
    alias(libs.plugins.myrecipesstore.android.feature.impl)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.francotte.favorites"
}

dependencies {
    api(project(":core:data"))
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:domain"))
    api(project(":core:ui"))
    api(project(":core:navigation"))
    api(project(":feature:favorites:api"))
    api(project(":feature:detail:api"))
    api(project(":feature:login:api"))

    implementation(libs.androidx.hilt.lifecycle.viewModelCompose)
    implementation(libs.coil.compose)
    implementation("com.google.android.material:material:1.11.0")
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

    implementation(libs.androidx.activity.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.compose.material.iconsExtended)
}
