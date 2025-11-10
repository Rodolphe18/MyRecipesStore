########## LOGS LISIBLES EN PROD
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

########## 1) TON CAS : RÉFLEXION SUR LES CHAMPS DE Recipe
-keepclassmembers class com.francotte.model.Recipe {
    *** strIngredient*;
    *** strMeasure*;
}
########## 2) WORKMANAGER : NOMS DE WORKERS (PERSISTENCE)
# Garde le nom des classes Worker pour que les jobs reprennent après MAJ
-keepnames class ** extends androidx.work.ListenableWorker

########## 3) RETROFIT / OKHTTP / ANNOTATIONS
# Retrofit repose sur des annotations runtime
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, AnnotationDefault
# Évite les faux warnings (souvent sans impact)
-dontwarn javax.annotation.**
-dontwarn okio.**
-dontwarn kotlin.Unit

########## 4) KOTLINX SERIALIZATION
# En général, les libs fournissent leurs consumer-rules.
# Si tu utilises du polymorphisme ou des serializers récupérés par nom,
# garde les classes de serializers générés :
-keep,allowobfuscation,allowshrinking class **$$serializer { *; }
# (Active cette règle si tu constates des NoSuchMethodException de serializer)

########## Protobuf runtime (safe)
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

########## TES messages générés (ne pas renommer les champs)
# → évite que R8 obfusque des champs comme favoritesIds_ / bitField0_, etc.
-keepclassmembers class com.francotte.myrecipesstore.protobuf.** extends com.google.protobuf.GeneratedMessageLite { *; }

########## Qualité des stacktraces / annotations (utile avec Retrofit/Serialization)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable

########## 6) ANDROID COMPONENTS (par sûreté)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Conserver le nom (FQCN) des classes sérialisables utilisées par Navigation
-keepnames @kotlinx.serialization.Serializable class com.francotte.myrecipesstore.**

# Garder les serializers générés (utile si tu vois des erreurs de serializer)
-keep class **$$serializer { *; }

-keepnames class com.francotte.section.SectionRoute
-keepnames class com.francotte.detail.DetailRecipeRoute
-keepnames class com.francotte.categories.CategoryNavigationRoute
