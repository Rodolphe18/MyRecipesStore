plugins {
    alias(libs.plugins.myrecipesstore.android.feature.impl)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.francotte.home"
    testOptions.unitTests.isIncludeAndroidResources = true
    packaging {
        resources {
            excludes.addAll(
                arrayOf(
                    "META-INF/versions/**",
                    "META-INF/*.kotlin_module",
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE*",
                    "META-INF/NOTICE*",
                    "META-INF/AL2.0",
                    "META-INF/LGPL2.1",
                    "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
                ),
            )
        }
    }
}

dependencies {

    api(project(":feature:ads"))
    api(project(":feature:detail:api"))

    api(project(":core:data"))
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:ui"))
    api(project(":core:testing"))
    api(project(":core:screenshot-testing"))
    api(project(":core:navigation"))

    api(project(":feature:home:api"))
    api(project(":feature:video:api"))
    api(project(":feature:section:api"))
    api(project(":feature:login:api"))

    implementation(libs.coil.compose)
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.kotlinx.metadata.jvm)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.ui.test.junit4)
    //  implementation(libs.androidx.compose.runtime.tracing)

    testImplementation(libs.junit)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.accessibility.check)
    testImplementation(libs.roborazzi.compose)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.test.manifest)
}
