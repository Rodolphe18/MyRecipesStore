plugins {
    alias(libs.plugins.myrecipesstore.android.library)
}

android {
    namespace = "com.francotte.testing"
}

dependencies {

    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:data"))
    api(project(":core:billing"))
    api(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.test.rules)
    implementation(libs.hilt.android.testing)
}
