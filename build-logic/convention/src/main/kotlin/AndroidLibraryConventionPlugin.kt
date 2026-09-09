import com.android.build.api.dsl.LibraryExtension
import com.francotte.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")
            pluginManager.apply("myrecipesstore.quality")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                val optimizeProguardFile =
                    getDefaultProguardFile("proguard-android-optimize.txt")

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                        proguardFiles(optimizeProguardFile, "proguard-rules.pro")
                    }
                }
            }
        }
    }
}
