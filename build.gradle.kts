// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.plugin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Ktlint partout (ou filtre si besoin)
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Detekt partout (ou filtre si besoin)
    apply(plugin = "io.gitlab.arturbosch.detekt")

    // -------- KTLINT --------
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
        // Optionnel : versions/flags selon ton besoin
         ignoreFailures.set(true)
        // android.set(true)
        // outputToConsole.set(true)
        // reporters { reporter(ReporterType.CHECKSTYLE) }
    }

    // -------- DETEKT --------
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension>("detekt") {
        buildUponDefaultConfig = true
        // Point important : une config partagée
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        // Optionnel
        // autoCorrect = true
    }
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn("ktlintCheck")
        dependsOn("detekt")
    }
}
