plugins {
    alias(libs.plugins.myrecipesstore.android.library)
    alias(libs.plugins.myrecipesstore.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.francotte.domain"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:datastore"))
    api(project(":core:data"))
    api(project(":core:database"))
    api(project(":core:network"))
    api(project(":core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.kotlinx.serialization.json.okio)

}
