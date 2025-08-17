########## LOGS LISIBLES EN PROD
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

########## 1) TON CAS : RÉFLEXION SUR LES CHAMPS DE Recipe
-keepclassmembers class com.francotte.myrecipesstore.domain.model.Recipe {
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

########## 5) (OPTIONNEL) DATASTORE + PROTOBUF LITE
# Normalement pas nécessaire. Si tu vois des warnings/problèmes côté proto :
 -dontwarn com.google.protobuf.**
 -keep class com.google.protobuf.** { *; }

########## 6) ANDROID COMPONENTS (par sûreté)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider