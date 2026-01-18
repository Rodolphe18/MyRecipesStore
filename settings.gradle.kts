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
include(":feature:home:impl")
include(":feature:search:impl")
include(":feature:login:impl")
include(":feature:detail:impl")
include(":feature:add_recipe:impl")
include(":feature:register:impl")
include(":feature:profile:impl")
include(":feature:section:impl")
include(":feature:favorites:impl")
include(":feature:video:impl")
include(":feature:settings:impl")
include(":feature:categories:impl")
include(":feature:reset:impl")
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
include(":core:navigation")
include(":feature:home:api")
include(":feature:categories:api")
include(":feature:search:api")
include(":feature:login:api")
include(":feature:favorites:api")
include(":feature:detail:api")
include(":feature:section:api")
include(":feature:video:api")
include(":feature:register:api")
include(":feature:profile:api")
include(":feature:reset:api")
include(":feature:add_recipe:api")
include(":feature:settings:api")
