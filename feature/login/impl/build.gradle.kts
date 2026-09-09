plugins {
    alias(libs.plugins.myrecipesstore.android.feature.impl)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.francotte.login"
}

dependencies {

    api(project(":feature:login:api"))
    api(project(":feature:favorites:api"))
    api(project(":feature:register:api"))
    api(project(":feature:reset:api"))

    api(project(":core:auth"))
    api(project(":core:data"))
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:domain"))
    api(project(":core:navigation"))
    api(project(":core:ui"))

    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.android.identity.googleid)
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
}
