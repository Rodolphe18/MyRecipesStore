plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.plugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
    id("kotlin-parcelize")
}

configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}


android {
    namespace = "com.francotte.myrecipesstore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.francotte.myrecipesstore"
        minSdk = 26
        targetSdk = 35
        versionCode = 12
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    signingConfigs {
        create("release") {
            storeFile = file("release-keystore.jks")
            storePassword = "Nietzsche@18"
            keyAlias = "release-key"
            keyPassword = "Nietzsche@18"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    testOptions {
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
    baselineProfile {
        automaticGenerationDuringBuild = false
        dexLayoutOptimization = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:ads"))
    implementation(project(":core:premium"))
    implementation(project(":core:notifications"))

    implementation(project(":feature:home"))
    implementation(project(":feature:categories"))
    implementation(project(":feature:search"))
    implementation(project(":feature:login"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:add_recipe"))
    implementation(project(":feature:register"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:section"))
    implementation(project(":feature:favorites"))
    implementation(project(":feature:reset"))
    implementation(project(":feature:video"))
    implementation(project(":feature:settings"))


    implementation(libs.kotlinx.metadata.jvm)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.benchmark.macro.junit4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.hilt.android)
    implementation(libs.hilt.core)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.play.services.auth)
    implementation(libs.androidx.dataStore)
    implementation(libs.protobuf.kotlin.lite)

    implementation(libs.startup.runtime)

    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.kotlinx.serialization.json.okio)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.coil.compose)

    implementation(libs.play.services.auth)
    implementation(libs.facebook.android.sdk)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.androidx.compose.material.iconsExtended)

    implementation(libs.androidx.hilt.work)
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp(libs.androidx.hilt.compiler)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.ads)
    implementation(libs.billing.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    implementation("androidx.startup:startup-runtime:1.1.1")

    implementation("androidx.compose.material3:material3:1.2.1") // ou version plus récente
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.material:material:1.11.0")

    androidTestImplementation("androidx.profileinstaller:profileinstaller:1.3.0")
    androidTestImplementation("androidx.benchmark:benchmark-macro-junit4:1.2.0")

    androidTestUtil("androidx.test:orchestrator:1.4.2")
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains" && requested.name == "annotations") {
                useVersion("23.0.0")
                because("Avoid conflict with com.intellij:annotations:12.0")
            }
        }
    }

}
