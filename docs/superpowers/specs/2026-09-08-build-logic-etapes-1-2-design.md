# build-logic — étapes 1 & 2 : scaffolding + premier convention plugin

Date : 2026-09-08
Statut : approuvé

## Contexte

Le projet compte 58 fichiers `build.gradle.kts` pour 3628 lignes. La configuration
Android y est copiée-collée module par module :

| Bloc dupliqué | Nb de fichiers |
|---|---|
| `compileSdk = 36` | 57 |
| `minSdk = 26` | 56 |
| `compileOptions { VERSION_17 }` | 56 |
| `testInstrumentationRunner` | 55 |
| `buildTypes { release { … } }` | 53 |
| bloc Hilt (plugin + ksp + deps) | 37 |
| bloc Compose | 24 |

Changer `compileSdk` demande aujourd'hui d'éditer 57 fichiers.

## Objectif

Deux objectifs, à poids égal :

1. **Technique** — mutualiser la configuration de build dans des *convention plugins*,
   sur le modèle de [Now in Android](https://github.com/android/nowinandroid).
2. **Pédagogique** — monter en compétence sur Gradle. Chaque étape introduit un
   nombre limité de notions, expliquées au moment où elles servent.

Le second objectif contraint le premier : on avance par petits pas vérifiables,
même quand un pas plus grand serait techniquement possible.

## Périmètre de cette itération

Étapes 1 et 2 uniquement.

- **Étape 1** — créer le build inclus `build-logic` et le brancher.
- **Étape 2** — écrire `AndroidLibraryConventionPlugin` et l'appliquer à **un seul
  module pilote**, `:core:model`.

Hors périmètre (itérations suivantes) : déploiement sur les ~50 autres modules,
plugins Compose / Hilt / feature / application, migration du bloc `subprojects { }`
de la racine.

## Architecture

```
MyRecipesStore/
├── settings.gradle.kts          ← + includeBuild("build-logic")
├── gradle/libs.versions.toml    ← + [libraries] AGP/Kotlin, + [plugins] alias
└── build-logic/                 ← build Gradle autonome (included build)
    ├── settings.gradle.kts      ← include(":convention") + catalogue partagé
    └── convention/
        ├── build.gradle.kts     ← module Kotlin/JVM produisant les plugins
        └── src/main/kotlin/
            └── AndroidLibraryConventionPlugin.kt
```

`build-logic` est un **included build** et non un module du projet : Gradle doit le
compiler pendant la phase d'initialisation, avant de configurer les modules qui
consomment ses plugins. Un module ordinaire ne serait compilé qu'en phase d'exécution,
donc trop tard.

`build-logic/settings.gradle.kts` déclare ses propres `pluginManagement` et
`dependencyResolutionManagement` — un build séparé n'hérite pas de ceux du parent. Il
lit `../gradle/libs.versions.toml` pour éviter de dupliquer les versions.

## Le plugin `myrecipesstore.android.library`

Contrat : reproduire **à l'identique** ce que les modules library répètent aujourd'hui.

Le plugin applique :
- les plugins `com.android.library` et `org.jetbrains.kotlin.android` ;
- `compileSdk = 36` ;
- `defaultConfig` : `minSdk = 26`, `testInstrumentationRunner`,
  `consumerProguardFiles("consumer-rules.pro")` ;
- `buildTypes.release` : `isMinifyEnabled = false` + les deux `proguardFiles` ;
- `compileOptions` : source et target `VERSION_17` ;
- `kotlin.compilerOptions.jvmTarget = JVM_17`.

Restent dans chaque `build.gradle.kts` de module :
- `namespace` — unique par module, ne peut pas être mutualisé tel quel ;
- le bloc `dependencies { }` — spécifique au module.

## Décisions de design

**Pas de helper générique `configureKotlinAndroid(CommonExtension)`.** NiA factorise
la configuration commune dans une fonction prenant un `CommonExtension`, appelée à la
fois par son plugin `library` et par son plugin `application`. Ce projet a lui aussi un
module application (`:app`) et un module test (`:benchmark`) — mais leurs plugins ne
sont pas dans le périmètre de cette itération. Extraire le helper maintenant créerait
une fonction à **un seul appelant** : de l'indirection gratuite. On l'extraira à
l'étape 7, quand `AndroidApplicationConventionPlugin` existera et que la duplication
sera réelle.

Ce report a un bénéfice annexe. Sur AGP 8.13.1, `CommonExtension` prend six paramètres
de type ; sur AGP 9.3.2 (version de NiA) il n'en prend aucun. Le code de NiA ne
compilerait donc pas tel quel ici, et le helper devrait s'écrire
`CommonExtension<*, *, *, *, *, *>`. À l'inverse, `LibraryExtension` a la même signature
dans les deux versions — le plugin écrit à l'étape 2 restera valide après une éventuelle
migration AGP 9.

**`:app` et `:benchmark` ne sont pas touchés.** Ils conservent leur `build.gradle.kts`
actuel et ne dépendent pas de `build-logic`. Aucune régression possible de ce côté.

**Ordre vis-à-vis d'une migration AGP 9.** `build-logic` d'abord, migration ensuite :
une fois `compileSdk` et consorts centralisés, la migration se fait dans un fichier au
lieu de 57. Faire les deux en même temps rendrait indiscernables les ruptures AGP des
erreurs de conception des plugins.

**`compileOnly` pour les dépendances AGP / Kotlin Gradle Plugin.** Le plugin a besoin
des classes d'AGP pour compiler, mais AGP est déjà présent dans le classpath du build
consommateur au moment de l'exécution. Le déclarer en `implementation` risquerait
d'embarquer une seconde copie d'AGP et de provoquer des conflits de version.

**Comportement inchangé, y compris les imperfections.** 27 des ~53 modules déclarent
`consumerProguardFiles("consumer-rules.pro")` sans que le fichier existe. On reproduit
ce comportement à l'identique : ce refactoring ne doit rien changer d'autre que la
localisation du code.

**Nommage `myrecipesstore.*`.** Aligné sur la convention NiA (`nowinandroid.*`).

**On ne touche pas au bloc `subprojects { }` de la racine** (ktlint, detekt, jvmTarget).
Il fait doublon avec le `jvmTarget` du plugin, mais le supprimer affecterait les 57
modules — hors périmètre.

## Vérification

- `./gradlew :core:model:assembleDebug` passe.
- `./gradlew :core:model:dependencies` produit le même résultat qu'avant migration
  (comparaison avant/après sur fichier).
- `core/model/build.gradle.kts` passe de 40 à ~12 lignes.

## Notions Gradle couvertes

Phases init / configuration / exécution · `Project` · `Plugin<Project>` · included
build · `pluginManagement` · `kotlin-dsl` · bloc `gradlePlugin { }` · `extensions` et
`extensions.configure<T>` · `LibraryExtension` · `compileOnly` vs `implementation` ·
version catalog partagé entre builds.
