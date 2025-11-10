pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyRecipesStore"
include(":app")
include(":benchmark")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:datastore")
include(":sync")
include(":core:common")
include(":core:domain")
include(":core:ui")
include(":feature:home")
include(":feature:search")
include(":feature:login")
include(":core:designsystem")
include(":feature:detail")
include(":feature:add_recipe")
include(":feature:register")
include(":feature:profile")
include(":feature:section")
include(":feature:favorites")
include(":feature:video")
include(":core:notifications")
include(":feature:settings")
include(":core:ads")
include(":core:premium")
include(":feature:categories")
include(":feature:reset")
include(":core:datastore-proto")
