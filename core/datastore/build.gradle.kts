plugins {
    alias(libs.plugins.myrecipesstore.android.library)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.myrecipesstore.hilt)
}

android {
    namespace = "com.francotte.datastore"
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
}

dependencies {

    api(project(":core:common"))
    api(project(":core:model"))
    api(project(":core:datastore-proto"))

    implementation(libs.androidx.dataStore)

    testImplementation(libs.kotlinx.coroutines.test)
}
