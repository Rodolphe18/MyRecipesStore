plugins {
    alias(libs.plugins.myrecipesstore.android.library)
    alias(libs.plugins.myrecipesstore.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.francotte.data"
}

dependencies {

    api(project(":core:database"))
    api(project(":core:datastore"))
    api(project(":core:network"))
    api(project(":core:model"))

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)

}
