import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

class QualityConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            extensions.configure<KtlintExtension> {
                ignoreFailures.set(true)
            }

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                config.setFrom(file("$rootDir/config/detekt/detekt.yml"))
            }

            tasks.matching { it.name == "check" }.configureEach {
                dependsOn("ktlintCheck")
                dependsOn("detekt")
            }
        }
    }
}
