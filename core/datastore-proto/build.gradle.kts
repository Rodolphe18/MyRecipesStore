plugins {
    alias(libs.plugins.myrecipesstore.android.library)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.francotte.datastore_proto"
}

protobuf {
    protoc {
        artifact =
            libs.protobuf.protoc
                .get()
                .toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    api(libs.protobuf.kotlin.lite)
}
