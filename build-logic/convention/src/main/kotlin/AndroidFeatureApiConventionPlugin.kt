import com.francotte.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidFeatureApiConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("myrecipesstore.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                "api"(project(":core:navigation"))

                "implementation"(libs.findLibrary("androidx-core-ktx").get())
                "implementation"(libs.findLibrary("androidx-appcompat").get())
                "implementation"(libs.findLibrary("material").get())

                "testImplementation"(libs.findLibrary("junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
            }
        }
    }
}
