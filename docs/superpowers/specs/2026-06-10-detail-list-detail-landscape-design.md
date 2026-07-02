# List/Detail paysage dans `:feature:detail` — Design

Date : 2026-06-10
Module : `feature/detail/impl`

## Objectif

En mode paysage, afficher la recette dans un layout **list/detail** : un volet liste
(les recettes du lot `ids` actuellement navigué) à gauche, et le détail de la recette
sélectionnée à droite. En portrait, conserver à l'identique le `HorizontalPager` actuel
(swipe entre recettes).

La fonctionnalité est entièrement contenue dans `:feature:detail` : la navigation
(`DetailRecipeNavKey`, `navigateToDetail`) et les call sites ne changent pas.

## Décisions actées

1. **Contenu du volet liste** : les recettes du lot `ids` déjà passé au détail.
   Sélectionner un item remplace le détail à droite.
2. **Implémentation du split** : `ListDetailPaneScaffold` officiel
   (`androidx.compose.material3.adaptive:adaptive-layout` + `adaptive-navigation`).
3. **Portrait** : on garde le `HorizontalPager` plein écran (option 1). Le scaffold
   n'agit qu'en paysage.
4. **Données** : approche A — eager-load de tous les `ids` dans le ViewModel
   (lectures locales offline-first), source unique pour la liste ET le détail.

## Architecture

### 1. Branche de rendu (`DetailRecipeScreen`)

`DetailRecipeScreen` choisit la branche selon `rememberDeviceMode()` (déjà présent dans
`:core:ui`) :

- **Portrait** (`PhonePortrait` / `TabletPortrait`) → `HorizontalPager` actuel, inchangé.
- **Paysage** (`PhoneLandscape` / `TabletLandscape`) → `ListDetailPaneScaffold` 2 volets.

Le cas `deeplinkRecipe` (recette unique sans liste) reste plein écran dans les deux
orientations.

### 2. Forcer 2 volets en paysage

Par défaut, `ListDetailPaneScaffold` n'affiche deux volets côte à côte qu'à partir d'une
largeur **EXPANDED (≥ 840dp)**. Un téléphone en paysage « MEDIUM » (600–840dp)
retomberait en mono-volet. On fournit donc une **directive custom**
(`PaneScaffoldDirective` avec `maxHorizontalPartitions = 2`) dès que
`rememberDeviceMode()` est en paysage — même logique que l'override nav rail de
`FoodApp.kt`, plutôt que l'heuristique ≥ 840 par défaut.

### 3. ViewModel / State (`DetailRecipeViewModel`)

- `init` : souscrire à `observeFullRecipe` pour **tous** les `ids` (eager) au lieu de la
  seule page courante. La `Map<Int, LikeableRecipe>` existante devient la source unique :
  - volet liste = ses valeurs (triées par index),
  - volet détail = `recipes[selectedIndex]`.
- Ajout `selectedIndex: Int` au `DetailState` (init = `index ?: 0`).
- Nouvelle action `OnRecipeSelected(index: Int)` (clic dans la liste) → met à jour
  `selectedIndex` + `title`.
- Le pager portrait continue d'émettre `OnPageChanged` ; les deux convergent vers
  `selectedIndex` pour rester synchronisés en cas de rotation.
- Le toggle favori reste hors du VM (découplage `FavoriteManager`), comme aujourd'hui.

State résultant :

```kotlin
data class DetailState(
    val title: String = "",
    val pageCount: Int = 0,
    val initialPage: Int = 0,
    val selectedIndex: Int = 0,          // +
    val recipes: Map<Int, LikeableRecipe> = emptyMap(),
    val deeplinkRecipe: LikeableRecipe? = null,
)

sealed interface DetailAction {
    data class OnPageChanged(val page: Int) : DetailAction
    data class OnRecipeSelected(val index: Int) : DetailAction   // +
    data class OnToggleFavorite(val recipe: LikeableRecipe) : DetailAction
    data object OnBackClick : DetailAction
}
```

### 4. Décomposition des composables

- **`RecipeContent(likeableRecipe, onToggleFavorite, …)`** : extraction du corps d'une
  recette (vidéo/image + titre/fav + ingrédients + bannières + instructions),
  aujourd'hui dupliqué entre la branche pager et la branche deeplink. Réutilisé par les
  pages du pager (portrait) ET le detail pane (paysage). L'extraction des ingrédients
  (réflexion `strIngredient$i` / `strMeasure$i`) est centralisée ici, supprimant la
  duplication actuelle.
- **`RecipeListPane(recipes, selectedIndex, onSelect)`** : `LazyColumn` d'items.
  Chaque item :
  - **miniature** `recipe.strMealThumb` (depuis `AbstractRecipe`) à **gauche**,
  - **titre** `recipe.strMeal` à **droite**, sur **2 lignes max**, **centré
    verticalement** (ellipsis au-delà),
  - item sélectionné (`index == selectedIndex`) mis en évidence (fond/teinte).
- **`DetailRecipeScreen`** : orchestre la branche portrait/paysage et câble le scaffold
  (`rememberListDetailPaneScaffoldNavigator`, `contentKey = index`, directive custom).

### 5. Câblage du scaffold (paysage)

```kotlin
val navigator = rememberListDetailPaneScaffoldNavigator<Int>(
    scaffoldDirective = twoPaneDirective(), // maxHorizontalPartitions = 2
)
ListDetailPaneScaffold(
    directive = navigator.scaffoldDirective,
    value = navigator.scaffoldValue,
    listPane = {
        AnimatedPane {
            RecipeListPane(
                recipes = state.recipes,
                selectedIndex = state.selectedIndex,
                onSelect = { index ->
                    onAction(DetailAction.OnRecipeSelected(index))
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, index) }
                },
            )
        }
    },
    detailPane = {
        AnimatedPane {
            state.recipes[state.selectedIndex]?.let { RecipeContent(it, onToggleFavorite) }
        }
    },
)
```

## Dépendances

Ajout dans `gradle/libs.versions.toml` (groupe `androidx.compose.material3.adaptive`,
version `material3Adaptive = "1.1.0"` déjà épinglée) :

- `androidx-compose-material3-adaptive-layout` → `adaptive-layout`
- `androidx-compose-material3-adaptive-navigation` → `adaptive-navigation`

Déclarés dans `feature/detail/impl/build.gradle.kts`.

## Tests

- **VM** : tous les `ids` sont chargés (eager) ; `OnRecipeSelected(i)` met à jour
  `selectedIndex` et `title`.
- **UI (optionnel)** : branche paysage affiche les deux volets ; branche portrait affiche
  le pager.

## Hors périmètre (YAGNI)

- Pas de changement de `DetailRecipeNavKey` ni des call sites.
- Pas de méthode repository batch/« light » (les lectures locales suffisent).
- Pas de list/detail au-delà du lot `ids` courant (ex : favoris/catégorie globaux).
