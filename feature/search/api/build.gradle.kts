plugins {
    alias(libs.plugins.myrecipesstore.android.feature.api)
}

android {
    namespace = "com.francotte.feature.search.api"
}

dependencies {

    api(project(":feature:categories:api"))

}
