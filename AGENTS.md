# MyRecipesStore — Guide for AI Agents

MyRecipesStore is a native Android app written in Kotlin that provides recipes from around the world. Users can create an account to save recipes in a favorites list.

---

## Architecture

Modern Android app following Google's official architecture guidance: reactive, single-activity, clean architecture.

- **UI:** Jetpack Compose + Material 3
- **State:** UDF with Kotlin Coroutines and `Flow`. ViewModels expose `StateFlow<UiState>` via `SharingStarted.WhileSubscribed(5_000)`
- **DI:** Hilt (`@HiltViewModel`, `@Inject`, module-based providers)
- **Navigation:** Navigation3 for Compose — type-safe, declarative
- **Data:** Repository pattern aggregating Room (local) + Retrofit/OkHttp (remote)
- **Background:** WorkManager for deferrable tasks
- **Persistence:** Room for structured data, Proto DataStore for key-value encrypted storage

Layers: **UI (Screen/ViewModel) → Domain (Use Cases) → Data (Repositories → Network/Database)**

---

## Module Structure

### App
| Module | Role |
|--------|------|
| `:app` | Application entry point, DI setup, top-level navigation |

### Core modules (`core/`)
| Module | Role |
|--------|------|
| `:core:model` | Domain data models shared across modules |
| `:core:network` | Retrofit + OkHttp setup, API service interfaces, Kotlinx Serialization |
| `:core:database` | Room database, DAOs, migrations, schema files |
| `:core:data` | Repositories — aggregate network and database sources |
| `:core:datastore` | Proto DataStore for encrypted key-value storage |
| `:core:datastore-proto` | Protobuf definitions for DataStore |
| `:core:domain` | Use cases and business logic |
| `:core:common` | Shared utilities, extensions, helpers |
| `:core:ui` | Shared Composables not part of the design system |
| `:core:designsystem` | Material3 theme, typography, colors, design tokens |
| `:core:auth` | Authentication logic (Google Sign-In, Facebook SDK) |
| `:core:billing` | Google Play Billing integration |
| `:core:notifications` | Firebase Cloud Messaging + notification handling |
| `:core:navigation` | Type-safe navigation state management |
| `:core:ads` | Ad network integration (Admob, Prebid, Criteo, APS) |
| `:core:premium` | Premium/subscription features |
| `:core:web` | WebView integration |
| `:core:shared-prefs` | SharedPreferences helpers |
| `:core:inapp-update` | Google Play In-App Update |
| `:core:inapp-rating` | Google Play In-App Review |
| `:core:cmp` | Consent management (Didomi SDK) |
| `:core:testing` | Shared test utilities and fixtures |
| `:core:screenshot-testing` | Roborazzi screenshot test utilities |

### Feature modules (`feature/`)

Each feature follows a two-module pattern:
- **`api`** — public interface: type-safe `NavKey`, navigation extension function
- **`impl`** — implementation: Screen, Route, ViewModel, delegates, DI module

| Feature | Role |
|---------|------|
| `:feature:home` | Home screen: latest recipes, Japanese, English, Areas sections |
| `:feature:search` | Recipe search |
| `:feature:detail` | Recipe detail view |
| `:feature:add_recipe` | User recipe creation |
| `:feature:favorites` | Saved/favorited recipes |
| `:feature:login` | Authentication UI |
| `:feature:register` | New user registration |
| `:feature:profile` | User profile management |
| `:feature:reset` | Password reset flow |
| `:feature:categories` | Browse by category |
| `:feature:section` | Browse by cuisine/area |
| `:feature:video` | Recipe video content |
| `:feature:settings` | App settings |
| `:feature:notifications` | Notification display |
| `:feature:inapp-update` | In-app update prompts |
| `:feature:inapp-rating` | In-app rating prompts |
| `:feature:ads` | Ad display |

### Other
| Module | Role |
|--------|------|
| `:benchmark` | Baseline profile + performance benchmarks |
| `:sync` | Background data sync |

---

## Feature Module Internal Structure

```
feature/<name>/impl/src/main/java/com/francotte/<name>/
├── <Name>Route.kt          # Composable entry point, obtains hiltViewModel()
├── <Name>Screen.kt         # UI composable (previews here)
├── <Name>ViewModel.kt      # @HiltViewModel, exposes StateFlow<UiState>
├── delegate/               # Delegates for complex logic separation (if needed)
│   └── <Topic>Delegate.kt
└── di/
    └── <Name>Module.kt     # Hilt DI module

feature/<name>/api/src/main/java/com/francotte/feature/<name>/api/
└── <Name>NavKey.kt         # @Serializable NavKey + fun Navigator.navigateTo<Name>()
```

---

## Navigation

- Uses **Navigation3** (latest `androidx.navigation3`)
- Each feature's API module exposes a `@Serializable` `NavKey` object
- Extension function pattern: `fun Navigator.navigateToHome()` in the API module
- Navigation state managed via a custom `Navigator` class using multiple back stacks (root, top-level, per-feature sub-stacks)

---

## Build System

| Property | Value |
|----------|-------|
| Gradle wrapper | 8.13.1 |
| Android Gradle Plugin | 8.13.1 |
| Kotlin | 2.0.20 |
| Compose BOM | 2025.09.01 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 |
| Java | VERSION_17 |

Key Gradle flags (in `gradle.properties`):
- `org.gradle.parallel=true`
- `org.gradle.caching=true`
- `org.gradle.configuration-cache=true`
- JVM: `-Xmx6g -XX:MaxMetaspaceSize=1g`

---

## Common Build Commands

```bash
./gradlew assembleDebug                   # Debug APK
./gradlew assembleRelease                 # Signed release APK
./gradlew bundleRelease                   # Signed AAB for Play Store
./gradlew test                            # Unit tests
./gradlew connectedAndroidTest            # Instrumented tests
./gradlew check                           # Lint + ktlint + detekt + tests
./gradlew clean                           # Clean build outputs
./gradlew :core:database:kspDebugKotlin   # Regenerate Room/Hilt KSP code
```

---

## CI/CD

File: `.github/workflows/main.yml`

Triggers on push to `master` or manual dispatch. Steps:
1. Checkout + JDK 17 (Temurin)
2. Decode keystore from GitHub Secret `ANDROID_KEYSTORE_BASE64`
3. Run `./gradlew :app:bundleRelease`
4. Upload AAB as artifact

Signing env vars required in CI: `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`

---

## Key Config Files

| File | Purpose |
|------|---------|
| `gradle/libs.versions.toml` | Centralized dependency versions and plugin aliases |
| `build.gradle.kts` (root) | Root plugins: ktlint, detekt |
| `settings.gradle.kts` | Module includes, repository config |
| `gradle.properties` | JVM args, parallel/caching flags |
| `config/detekt/detekt.yml` | Static analysis rules |
| `app/src/main/AndroidManifest.xml` | Permissions, activity, feature declarations |
| `.github/workflows/main.yml` | CI/CD pipeline |
| `keystore.properties` | Local signing config (gitignored) |

---

## Code Quality

- **Ktlint** — formatting, applied project-wide (`ignoreFailures = true`)
- **Detekt** — static analysis, rules in `config/detekt/detekt.yml`
  - LongMethod: 200 lines
  - LongParameterList: 8 params
  - CyclomaticComplexMethod: 20

---

## Testing

- **Unit tests:** JUnit + MockK
- **Instrumented tests:** AndroidJUnit4 + Espresso, with test orchestrator (`ANDROIDX_TEST_ORCHESTRATOR`)
- **Screenshot tests:** Roborazzi (Compose)
- Test utilities shared in `:core:testing`

---

## Important Rules

- **Room schema files** (`core/database/schemas/`) must always be committed to git. They are required by KSP for auto-migration generation. Never add them to `.gitignore`.
- **`keystore.properties`** is gitignored — never commit it. CI uses GitHub Secrets instead.
- Release signing is done exclusively via environment variables in CI or local `keystore.properties`.

---

## Repository

GitHub: https://github.com/Rodolphe18/MyRecipesStore