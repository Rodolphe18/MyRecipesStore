# List/Detail paysage `:feature:detail` — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** En paysage, afficher la recette dans un layout list/detail (volet liste des recettes du lot `ids` + détail de la recette sélectionnée), tout en conservant le `HorizontalPager` en portrait.

**Architecture:** `DetailRecipeScreen` branche sur `rememberDeviceMode()` : portrait → pager existant, paysage → `ListDetailPaneScaffold` (directive forcée à 2 volets). Le `DetailRecipeViewModel` charge en eager toutes les recettes du lot (source unique liste + détail) et expose un `selectedIndex` piloté par l'action `OnRecipeSelected`. Le corps d'une recette est extrait dans un composable `RecipeContent` réutilisé par le pager et le detail pane.

**Tech Stack:** Kotlin, Jetpack Compose, MVI (StateFlow + Channel), Hilt (AssistedInject), `androidx.compose.material3.adaptive` (adaptive-layout + adaptive-navigation), JUnit4 + coroutines-test + Robolectric.

**Spec:** `docs/superpowers/specs/2026-06-10-detail-list-detail-landscape-design.md`

---

## File structure

| Fichier | Responsabilité | Action |
|---|---|---|
| `gradle/libs.versions.toml` | Alias des deps adaptive-layout / adaptive-navigation | Modifier |
| `feature/detail/impl/build.gradle.kts` | Deps du module (prod + test) | Modifier |
| `core/testing/src/main/java/com/francotte/testing/FakeUserFullRecipeRepository.kt` | Fake repo pour tester le VM | Créer |
| `feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeViewModel.kt` | VM : eager-load, `selectedIndex`, `OnRecipeSelected`, dep sur l'interface | Modifier |
| `feature/detail/impl/src/test/java/com/francotte/detail/DetailRecipeViewModelTest.kt` | Tests unitaires du VM | Créer |
| `feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeScreen.kt` | Extraction `RecipeContent` + branche portrait/paysage | Modifier |
| `feature/detail/impl/src/main/java/com/francotte/detail/RecipeListPane.kt` | Volet liste (miniature + titre) | Créer |
| `feature/detail/impl/src/main/java/com/francotte/detail/RecipeListDetailLayout.kt` | Câblage `ListDetailPaneScaffold` (paysage) | Créer |

---

## Task 1: Dépendances adaptive-layout / adaptive-navigation + deps de test

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `feature/detail/impl/build.gradle.kts`

- [ ] **Step 1: Ajouter les alias de librairies adaptive**

Dans `gradle/libs.versions.toml`, juste après la ligne `androidx-compose-material3-adaptive = { ... name = "adaptive", version.ref = "material3Adaptive" }` (ligne 176), ajouter :

```toml
androidx-compose-material3-adaptive-layout = { group = "androidx.compose.material3.adaptive", name = "adaptive-layout", version.ref = "material3Adaptive" }
androidx-compose-material3-adaptive-navigation = { group = "androidx.compose.material3.adaptive", name = "adaptive-navigation", version.ref = "material3Adaptive" }
```

(`material3Adaptive` vaut déjà `"1.1.0"`.)

- [ ] **Step 2: Déclarer les deps dans le module detail**

Dans `feature/detail/impl/build.gradle.kts`, dans le bloc `dependencies { ... }`, après la ligne `implementation(libs.androidx.material3)` (ligne 62), ajouter :

```kotlin
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
```

Puis, dans la section test (après la ligne `testImplementation(libs.roborazzi.compose)`, ligne 83), ajouter :

```kotlin
    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlinx.coroutines.test)
```

- [ ] **Step 3: Vérifier la résolution / compilation**

Run: `./gradlew :feature:detail:impl:compileDebugKotlin -q`
Expected: build OK (aucune sortie). Confirme que les artefacts adaptive sont résolus.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml feature/detail/impl/build.gradle.kts
git commit -m "build(detail): add adaptive-layout/navigation + test deps for list/detail"
```

---

## Task 2: ViewModel — eager-load, selectedIndex, OnRecipeSelected (TDD)

**Files:**
- Create: `core/testing/src/main/java/com/francotte/testing/FakeUserFullRecipeRepository.kt`
- Modify: `feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeViewModel.kt`
- Test: `feature/detail/impl/src/test/java/com/francotte/detail/DetailRecipeViewModelTest.kt`

- [ ] **Step 1: Créer le fake repository**

Créer `core/testing/src/main/java/com/francotte/testing/FakeUserFullRecipeRepository.kt` :

```kotlin
package com.francotte.testing

import com.francotte.data.interfaces.UserFullRecipeRepository
import com.francotte.model.LikeableRecipe
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeUserFullRecipeRepository : UserFullRecipeRepository {

    private val flows = mutableMapOf<Long, MutableSharedFlow<Result<LikeableRecipe>>>()

    private fun flowFor(id: Long) = flows.getOrPut(id) {
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    override fun observeFullRecipe(id: Long): Flow<Result<LikeableRecipe>> = flowFor(id)

    /** Pousse une recette pour [id]. replay=1 → rejouée à la souscription du VM. */
    fun emit(id: Long, recipe: LikeableRecipe) {
        flowFor(id).tryEmit(Result.success(recipe))
    }
}
```

- [ ] **Step 2: Écrire le test qui échoue**

Créer `feature/detail/impl/src/test/java/com/francotte/detail/DetailRecipeViewModelTest.kt` :

```kotlin
package com.francotte.detail

import com.francotte.model.FavoriteState
import com.francotte.model.LikeableRecipe
import com.francotte.model.TestRecipe
import com.francotte.testing.FakeUserFullRecipeRepository
import com.francotte.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DetailRecipeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeUserFullRecipeRepository()

    private fun likeable(id: String, name: String) =
        LikeableRecipe(TestRecipe(idMeal = id, strMeal = name), FavoriteState.NotFavorite)

    @Test
    fun `loads every recipe of the batch eagerly`() = runTest {
        repository.emit(1L, likeable("1", "Alpha"))
        repository.emit(2L, likeable("2", "Beta"))

        val viewModel = DetailRecipeViewModel(
            detailRecipeRepository = repository,
            ids = listOf("1", "2"),
            index = 0,
            recipeTitle = "Alpha",
        )

        val state = viewModel.state.value
        assertEquals(2, state.recipes.size)
        assertEquals("Alpha", state.recipes[0]?.recipe?.strMeal)
        assertEquals("Beta", state.recipes[1]?.recipe?.strMeal)
    }

    @Test
    fun `OnRecipeSelected updates selectedIndex and title`() = runTest {
        repository.emit(1L, likeable("1", "Alpha"))
        repository.emit(2L, likeable("2", "Beta"))
        val viewModel = DetailRecipeViewModel(repository, listOf("1", "2"), 0, "Alpha")

        viewModel.onAction(DetailAction.OnRecipeSelected(1))

        assertEquals(1, viewModel.state.value.selectedIndex)
        assertEquals("Beta", viewModel.state.value.title)
    }
}
```

- [ ] **Step 3: Lancer le test → échec attendu**

Run: `./gradlew :feature:detail:impl:testDebugUnitTest --tests "com.francotte.detail.DetailRecipeViewModelTest"`
Expected: FAIL — compilation impossible (`OnRecipeSelected` et `selectedIndex` n'existent pas encore ; le VM dépend du type concret du repo).

- [ ] **Step 4: Implémenter le VM**

Remplacer **tout** le contenu de `feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeViewModel.kt` par :

```kotlin
package com.francotte.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francotte.data.interfaces.UserFullRecipeRepository
import com.francotte.model.LikeableRecipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailRecipeViewModel.Factory::class)
class DetailRecipeViewModel @AssistedInject constructor(
    private val detailRecipeRepository: UserFullRecipeRepository,
    @Assisted val ids: List<String>?,
    @Assisted val index: Int?,
    @Assisted val recipeTitle: String?,
) : ViewModel() {

    private val longIds = ids?.map { it.toLong() } ?: emptyList()

    private val _state = MutableStateFlow(
        DetailState(
            title = recipeTitle ?: "",
            pageCount = longIds.size,
            initialPage = index ?: 0,
            selectedIndex = index ?: 0,
        )
    )
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>()
    val events = _events.receiveAsFlow()

    private var currentPage = index ?: 0

    /** Pages with an active observer — avoids re-subscribing the same id twice. */
    private val observedPages = mutableSetOf<Int>()

    init {
        // Eager-load : on souscrit à toutes les recettes du lot pour alimenter le volet liste.
        longIds.indices.forEach { loadPage(it) }
    }

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.OnPageChanged -> selectRecipe(action.page)
            is DetailAction.OnRecipeSelected -> selectRecipe(action.index)
            DetailAction.OnBackClick -> viewModelScope.launch {
                _events.send(DetailEvent.NavigateBack)
            }
            // Favorite toggling is handled outside the VM (FavoriteManager decoupling).
            is DetailAction.OnToggleFavorite -> Unit
        }
    }

    private fun selectRecipe(page: Int) {
        currentPage = page
        _state.update { state ->
            state.copy(
                selectedIndex = page,
                title = state.recipes[page]?.recipe?.strMeal ?: state.title,
            )
        }
    }

    private fun loadPage(page: Int) {
        if (page in observedPages || page !in longIds.indices) return
        observedPages += page
        viewModelScope.launch {
            detailRecipeRepository.observeFullRecipe(longIds[page]).collectLatest { result ->
                val recipe = result.getOrNull() ?: return@collectLatest
                _state.update { state ->
                    state.copy(
                        recipes = state.recipes + (page to recipe),
                        title = if (page == currentPage) recipe.recipe.strMeal else state.title,
                    )
                }
            }
        }
    }

    /** Deep-link entry: loads a single recipe by id. */
    fun loadDeeplink(id: String) {
        viewModelScope.launch {
            detailRecipeRepository.observeFullRecipe(id.toLong()).collectLatest { result ->
                val recipe = result.getOrNull() ?: return@collectLatest
                _state.update { it.copy(deeplinkRecipe = recipe, title = recipe.recipe.strMeal) }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(ids: List<String>?, index: Int?, recipeTitle: String?): DetailRecipeViewModel
    }
}

data class DetailState(
    val title: String = "",
    val pageCount: Int = 0,
    val initialPage: Int = 0,
    val selectedIndex: Int = 0,
    val recipes: Map<Int, LikeableRecipe> = emptyMap(),
    val deeplinkRecipe: LikeableRecipe? = null,
)

sealed interface DetailAction {
    data class OnPageChanged(val page: Int) : DetailAction
    data class OnRecipeSelected(val index: Int) : DetailAction
    data class OnToggleFavorite(val recipe: LikeableRecipe) : DetailAction
    data object OnBackClick : DetailAction
}

sealed interface DetailEvent {
    data object NavigateBack : DetailEvent
}
```

Note : le type du paramètre `detailRecipeRepository` passe du concret `CompositeUserFullRecipeRepository` à l'interface `UserFullRecipeRepository`. Hilt fournit déjà ce binding (`RepositoryModule.bindUserFullRecipeRepository`), donc l'injection reste valide.

- [ ] **Step 5: Lancer le test → succès attendu**

Run: `./gradlew :feature:detail:impl:testDebugUnitTest --tests "com.francotte.detail.DetailRecipeViewModelTest"`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add core/testing/src/main/java/com/francotte/testing/FakeUserFullRecipeRepository.kt feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeViewModel.kt feature/detail/impl/src/test/java/com/francotte/detail/DetailRecipeViewModelTest.kt
git commit -m "feat(detail): eager-load batch + selectedIndex/OnRecipeSelected in ViewModel"
```

---

## Task 3: Extraire `RecipeContent` (réutilisable pager + detail pane)

**Files:**
- Modify: `feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeScreen.kt`

Refactor pur : on factorise le corps d'une recette (aujourd'hui dupliqué entre la branche deeplink et la branche pager) dans un composable `internal RecipeContent`, plus un helper `rememberIngredients`. Aucune nouvelle logique.

- [ ] **Step 1: Ajouter `RecipeContent` et `rememberIngredients`**

Dans `DetailRecipeScreen.kt`, ajouter ces deux composables (par ex. juste avant `private fun DetailVideoScreen`, vers la ligne 250). Ajouter aussi l'import `import androidx.compose.ui.unit.Dp` en tête de fichier.

```kotlin
@Composable
internal fun RecipeContent(
    likeableRecipe: LikeableRecipe,
    onToggleFavorite: (LikeableRecipe) -> Unit,
    topPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val localBannerProvider = LocalBannerProvider.current
    val context = LocalContext.current
    val ingredients = rememberIngredients(likeableRecipe)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = topPadding, bottom = 12.dp),
    ) {
        DetailVideoScreen(likeableRecipe)
        Spacer(modifier = Modifier.height(16.dp))
        Column {
            DetailScreenMainSectionTitle(likeableRecipe, onToggleFavorite)
            BannerAd(
                horizontalPadding = 12.dp,
                placement = BannerPlacement.RECIPE_POS_1,
                provider = localBannerProvider,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailScreenSectionTitle(R.string.ingredients)
            IngredientRow(ingredients)
            DetailRecipeShareRecipeButton(likeableRecipe, ingredients, context)
            BannerAd(
                horizontalPadding = 12.dp,
                placement = BannerPlacement.RECIPE_POS_2,
                provider = localBannerProvider,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailScreenSectionTitle(R.string.instructions)
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = (likeableRecipe.recipe as Recipe).strInstructions.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun rememberIngredients(likeableRecipe: LikeableRecipe): List<Pair<String, String>> =
    remember(likeableRecipe.recipe) {
        (1..20).mapNotNull { i ->
            val recipe = likeableRecipe.recipe as? Recipe ?: return@mapNotNull null
            val ingredient = recipe.javaClass.getDeclaredField("strIngredient$i")
                .apply { isAccessible = true }.get(recipe) as? String
            val measure = recipe.javaClass.getDeclaredField("strMeasure$i")
                .apply { isAccessible = true }.get(recipe) as? String
            if (!ingredient.isNullOrBlank()) ingredient to (measure ?: "") else null
        }
    }
```

- [ ] **Step 2: Remplacer la branche deeplink par `RecipeContent`**

Dans `DetailRecipeScreen`, remplacer **tout** le bloc `if (deepLink != null) { ... }` (lignes ~107-167, la version actuelle avec `deepLink?.also { link -> ... }` et le `Column` à réflexion) par :

```kotlin
        if (deepLink != null) {
            RecipeContent(
                likeableRecipe = deepLink,
                onToggleFavorite = onToggleFavorite,
                topPadding = padding.calculateTopPadding() + 12.dp,
                modifier = Modifier
                    .testTag("full_detail_screen")
                    .semantics { contentDescription = "full_detail_screen" },
            )
        } else {
```

(Le `else {` ouvre la branche existante traitée au Step 3.)

- [ ] **Step 3: Remplacer le corps de page du pager par `RecipeContent`**

Toujours dans `DetailRecipeScreen`, dans le `HorizontalPager { index -> ... }`, remplacer le bloc `state.recipes[index]?.let { likeableRecipe -> ... }` (le gros `Column` à réflexion, lignes ~181-244) par :

```kotlin
                state.recipes[index]?.let { likeableRecipe ->
                    RecipeContent(
                        likeableRecipe = likeableRecipe,
                        onToggleFavorite = onToggleFavorite,
                        topPadding = padding.calculateTopPadding() + 12.dp,
                    )
                }
```

- [ ] **Step 4: Compiler**

Run: `./gradlew :feature:detail:impl:compileDebugKotlin -q`
Expected: build OK. Les imports devenus inutiles (`Arrangement`, `BoxWithConstraints` restent utilisés par DetailVideoScreen ; ne rien supprimer d'autre que ce qui casse). Si le compilateur signale un import inutilisé, c'est un warning, pas une erreur.

- [ ] **Step 5: Commit**

```bash
git add feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeScreen.kt
git commit -m "refactor(detail): extract reusable RecipeContent composable"
```

---

## Task 4: Composable `RecipeListPane` (volet liste)

**Files:**
- Create: `feature/detail/impl/src/main/java/com/francotte/detail/RecipeListPane.kt`

- [ ] **Step 1: Créer le fichier**

Créer `feature/detail/impl/src/main/java/com/francotte/detail/RecipeListPane.kt` :

```kotlin
package com.francotte.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francotte.designsystem.component.DesignAsyncImage
import com.francotte.model.AbstractRecipe
import com.francotte.model.LikeableRecipe

@Composable
internal fun RecipeListPane(
    recipes: Map<Int, LikeableRecipe>,
    count: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(count) { index ->
            RecipeListItem(
                recipe = recipes[index]?.recipe,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun RecipeListItem(
    recipe: AbstractRecipe?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background =
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesignAsyncImage(
            model = recipe?.strMealThumb,
            contentDescription = recipe?.strMeal,
            width = 72.dp,
            height = 72.dp,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Text(
            text = recipe?.strMeal.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        )
    }
}
```

- [ ] **Step 2: Compiler**

Run: `./gradlew :feature:detail:impl:compileDebugKotlin -q`
Expected: build OK.

- [ ] **Step 3: Commit**

```bash
git add feature/detail/impl/src/main/java/com/francotte/detail/RecipeListPane.kt
git commit -m "feat(detail): add RecipeListPane (thumbnail + 2-line title)"
```

---

## Task 5: Câbler le `ListDetailPaneScaffold` en paysage

**Files:**
- Create: `feature/detail/impl/src/main/java/com/francotte/detail/RecipeListDetailLayout.kt`
- Modify: `feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeScreen.kt`

- [ ] **Step 1: Créer le layout list/detail**

Créer `feature/detail/impl/src/main/java/com/francotte/detail/RecipeListDetailLayout.kt` :

```kotlin
package com.francotte.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.francotte.model.LikeableRecipe

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun RecipeListDetailLayout(
    state: DetailState,
    contentPadding: PaddingValues,
    onAction: (DetailAction) -> Unit,
) {
    val onToggleFavorite: (LikeableRecipe) -> Unit = {
        onAction(DetailAction.OnToggleFavorite(it))
    }

    // On force 2 volets même en largeur MEDIUM (téléphone en paysage), au lieu de
    // l'heuristique >= 840dp par défaut.
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
        .copy(maxHorizontalPartitions = 2)
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>(scaffoldDirective = directive)

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                RecipeListPane(
                    recipes = state.recipes,
                    count = state.pageCount,
                    selectedIndex = state.selectedIndex,
                    onSelect = { onAction(DetailAction.OnRecipeSelected(it)) },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                state.recipes[state.selectedIndex]?.let { recipe ->
                    RecipeContent(
                        likeableRecipe = recipe,
                        onToggleFavorite = onToggleFavorite,
                        topPadding = contentPadding.calculateTopPadding() + 12.dp,
                    )
                }
            }
        },
    )
}
```

- [ ] **Step 2: Brancher portrait/paysage dans `DetailRecipeScreen`**

Dans `DetailRecipeScreen.kt`, ajouter l'import `import com.francotte.ui.DeviceMode` en tête de fichier (`rememberDeviceMode` est déjà importé).

Dans le `else { ... }` ouvert au Task 3 Step 2 (la branche non-deeplink), envelopper le contenu existant ainsi. Le `LaunchedEffect(pagerState) { ... }` (synchronisation du pager) **et** le `HorizontalPager(...)` ne doivent s'exécuter que dans la branche portrait :

```kotlin
        } else {
            val isLandscape = mode == DeviceMode.PhoneLandscape || mode == DeviceMode.TabletLandscape
            if (isLandscape) {
                RecipeListDetailLayout(
                    state = state,
                    contentPadding = padding,
                    onAction = onAction,
                )
            } else {
                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.settledPage }
                        .distinctUntilChanged()
                        .collectLatest { newPage ->
                            onAction(DetailAction.OnPageChanged(newPage))
                        }
                }
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier.fillMaxSize(),
                ) { index ->
                    state.recipes[index]?.let { likeableRecipe ->
                        RecipeContent(
                            likeableRecipe = likeableRecipe,
                            onToggleFavorite = onToggleFavorite,
                            topPadding = padding.calculateTopPadding() + 12.dp,
                        )
                    }
                }
            }
        }
```

- [ ] **Step 3: Compiler**

Run: `./gradlew :feature:detail:impl:compileDebugKotlin -q`
Expected: build OK.

- [ ] **Step 4: Lancer les tests du module (non-régression)**

Run: `./gradlew :feature:detail:impl:testDebugUnitTest -q`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add feature/detail/impl/src/main/java/com/francotte/detail/RecipeListDetailLayout.kt feature/detail/impl/src/main/java/com/francotte/detail/DetailRecipeScreen.kt
git commit -m "feat(detail): list/detail landscape via ListDetailPaneScaffold"
```

---

## Task 6: Vérification manuelle (émulateur)

**Files:** aucun (validation).

- [ ] **Step 1: Build de l'app**

Run: `./gradlew :app:assembleDebug -q`
Expected: build OK.

- [ ] **Step 2: Vérifier le comportement**

Ouvrir une recette depuis une liste (favoris / catégorie / section) :
- **Portrait** : `HorizontalPager` plein écran, swipe entre recettes — inchangé.
- **Paysage** : volet liste à gauche (miniature + titre 2 lignes), détail à droite ; taper un item met à jour le détail et surligne l'item ; le titre du TopAppBar suit la sélection.
- **Rotation** : portrait→paysage conserve la recette courante (l'index est synchronisé via `selectedIndex`).

- [ ] **Step 3: Commit éventuel**

Si un ajustement visuel est nécessaire (taille miniature, ratio des volets), l'appliquer puis :

```bash
git add -A
git commit -m "polish(detail): landscape list/detail visual tweaks"
```

---

## Self-review notes

- **Spec coverage** : branche portrait/paysage (Task 5) ✓ ; directive 2 volets forcée (Task 5) ✓ ; eager-load approche A (Task 2) ✓ ; `selectedIndex`/`OnRecipeSelected` (Task 2) ✓ ; `RecipeContent` réutilisable (Task 3) ✓ ; `RecipeListPane` miniature+titre 2 lignes (Task 4) ✓ ; deps (Task 1) ✓ ; tests VM (Task 2) ✓.
- **Type consistency** : `DetailState.selectedIndex`, `DetailAction.OnRecipeSelected(index)`, `RecipeContent(likeableRecipe, onToggleFavorite, topPadding, modifier)`, `RecipeListPane(recipes, count, selectedIndex, onSelect, modifier)` — cohérents entre Tasks 2/3/4/5.
- **Hors périmètre** : `DetailRecipeNavKey` et call sites inchangés ; pas de méthode repo batch ; `deepLinkRecipeScreen` (navigation NavController legacy) non modifiée.
