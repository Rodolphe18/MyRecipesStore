import com.francotte.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("myrecipesstore.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            dependencies {
                val bom = platform(libs.findLibrary("androidx-compose-bom").get())
                "implementation"(bom)
                "androidTestImplementation"(bom)
                "implementation"(libs.findLibrary("androidx-ui-tooling-preview").get())
                "debugImplementation"(libs.findLibrary("androidx-ui-tooling").get())
            }

            extensions.configure<ComposeCompilerGradlePluginExtension> {
                metricsDestination.set(
                    reportDirIfEnabled("enableComposeCompilerMetrics", "compose-metrics"),
                )
                reportsDestination.set(
                    reportDirIfEnabled("enableComposeCompilerReports", "compose-reports"),
                )
            }
        }
    }
}

/**
 * Dossier de sortie du compilateur Compose, ou aucune valeur si la propriete
 * Gradle correspondante n'est pas a `true`.
 */
private fun Project.reportDirIfEnabled(
    gradleProperty: String,
    dirName: String,
): Provider<Directory> = providers.gradleProperty(gradleProperty)
    .filter { it.toBoolean() }
    .flatMap { layout.buildDirectory.dir(dirName) }
