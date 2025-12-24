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
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "MyRecipesStore"
include(":app")
include(":benchmark")
include(":sync")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:common")
include(":core:domain")
include(":core:ui")
include(":core:ads")
include(":core:premium")
include(":core:notifications")
include(":core:designsystem")
include(":feature:home")
include(":feature:search")
include(":feature:login")
include(":feature:detail")
include(":feature:add_recipe")
include(":feature:register")
include(":feature:profile")
include(":feature:section")
include(":feature:favorites")
include(":feature:video")
include(":feature:settings")
include(":feature:categories")
include(":feature:reset")
include(":core:billing")
include(":core:testing")
include(":core:screenshot-testing")
include(":core:cmp")
include(":core:web")
include(":core:inapp-update")
include(":feature:inapp-update")
include(":feature:inapp-rating")
include(":core:inapp-rating")
include(":core:shared-prefs")
include(":feature:ads")
