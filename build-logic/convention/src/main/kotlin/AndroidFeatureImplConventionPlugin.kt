import com.francotte.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidFeatureImplConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("myrecipesstore.android.library.compose")
            pluginManager.apply("myrecipesstore.hilt")

            dependencies {
                "api"(project(":core:designsystem"))

                "implementation"(libs.findLibrary("androidx-ui").get())
                "implementation"(libs.findLibrary("androidx-activity-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-hilt-navigation-compose").get())
            }
        }
    }
}
