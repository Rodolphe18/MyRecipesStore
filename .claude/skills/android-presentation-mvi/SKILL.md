---
name: android-presentation-mvi
description: |
  Use when creating or reviewing a feature ViewModel, defining screen State/Action/Event, structuring Route/Screen composables, building Ui models, or handling process death with SavedStateHandle in this project's feature/ modules. Trigger on "add a ViewModel", "create a screen", "MVI", "migrate to MVI", "state", "action", "event", "screen composable", "SavedStateHandle", or "UI model".
---

# Android Presentation Layer (MVI)

This project's `feature/` modules use **MVI** for the presentation layer. Each
feature is a two-module pair (`api` + `impl`); presentation code below lives in
`impl`. DI is **Hilt** (`@HiltViewModel`, `hiltViewModel()`).

## Overview

Every screen has:
1. **State** — a single data class holding all UI state fields.
2. **Action** (Intent) — a sealed interface of all user-triggered actions.
3. **Event** — a sealed interface of one-time side effects (navigation, snackbar).
4. **ViewModel** — holds `StateFlow<State>`, processes `Action`, emits `Event` via a `Channel`.

---

## State

```kotlin
data class CategoryState(
    val recipes: List<AbstractRecipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

Always update state with `.update { }` — never replace the entire flow:
```kotlin
_state.update { it.copy(isLoading = true) }
```

**Several mutually-exclusive content states?** Prefer flags, but when a screen has
genuinely distinct states that don't combine (e.g. `SearchNotReady` / `EmptyQuery`
/ `Loading` / `Success` / `LoadFailed`), keep them as a **sealed content type** and
wrap it in the State data class alongside the other fields:
```kotlin
data class SearchState(val query: String = "", val result: SearchResultUiState = SearchResultUiState.Loading)
```

### Initial load vs. user refresh — separate flags, separate functions

Keep two distinct flags: `isLoading` (first load on screen entry) and
`isRefreshing` (a user-triggered pull-to-refresh / retry). Do **not** reuse one
for both, or the initial load shows the pull-to-refresh spinner on top of the
loading indicator.

Drive them from **two named functions** — not a `refresh(showIndicator: Boolean)`
flag (a boolean parameter hides intent at the call site):

```kotlin
private fun refresh() {                        // OnReload action (pull / retry)
    viewModelScope.launch {
        _state.update { it.copy(isRefreshing = true) }
        repository.refresh(...)
        _state.update { it.copy(isRefreshing = false) }
    }
}
```

### Offline-first: a DB flow's empty first emission is not an error

With the offline-first pattern (`refresh()` writes the network result to the DB,
a Room `Flow` observes the DB), the flow **replays its latest value on
subscription** — initially **empty**, before the refresh populates it. Two traps:

1. If the flow's collector flips `isLoading`/`isError` from that empty emission,
   the screen flashes an error before the real data arrives.
2. Even if the collector only updates `recipes`, ending the load the instant
   `refresh()` *returns* still flashes: `refresh()` returns when the write
   commits, but the Room flow re-emits a few ms later. In that window
   `recipes == [] && !isLoading` → error.

Fix — end the load **atomically with the committed data**, via a one-shot read:

```kotlin
init {
    observeRecipes()        // live updates: keeps `recipes` in sync, never touches isLoading
    loadInitialRecipes()    // owns isLoading
}

private fun observeRecipes() {
    repository.observe(...)
        .onEach { recipes -> _state.update { it.copy(recipes = recipes) } }
        .launchIn(viewModelScope)
}

private fun loadInitialRecipes() {
    viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        repository.refresh(...)                       // suspends until DB write commits / fails
        val data = repository.observe(...).first()    // read the committed value one-shot
        _state.update { it.copy(recipes = data, isLoading = false) }  // atomic: data + done
    }
}
```

This is correct for every case with no flash and no hang: cached data, fresh
data, network failure (shows cached or error), and a genuinely empty result
(error, not an infinite spinner).

Render **content-first** so cached data shows immediately while a refresh runs:

```kotlin
when {
    state.recipes.isNotEmpty() -> Content(state.recipes)        // cache or fresh data
    state.isLoading            -> LoadingIndicator()
    else                       -> SectionErrorScreen(onRetry = { onAction(OnReload) })
}
```

### Choosing a data strategy: cached (observe) vs one-shot network

Two strategies, pick per screen:

- **Cached / reactive (offline-first, above).** For **frequently-used** screens,
  whose data is pre-fetched (e.g. in `Application`) so the DB is already filled.
  The VM just **observes** the DB; `refresh()` only refreshes. Keeps favorites and
  other live updates reactive. Use this when caching pays off. When the screen is
  pre-filled there is **no initial network call** — `init` only observes, and the
  collector ends `isLoading` on the first emission:

  ```kotlin
  private fun observe() {
      repository.observe(arg)
          .onEach { items -> _state.update { it.copy(items = items, isLoading = false,
              error = if (items.isEmpty()) LOAD_ERROR else null) } }
          .catch { _state.update { it.copy(isLoading = false, error = LOAD_ERROR) } }  // DB read failure
          .launchIn(viewModelScope)
  }
  ```
  Always `.catch` the observe flow: a Room flow can **throw** (DB read failure). Without
  it `onEach` never fires, `isLoading` stays true, and the screen hangs on the spinner forever.
- **One-shot network (no DB).** For **infrequently-visited** screens where caching
  is overkill. A single `suspend` repo call returns the data directly. **Simpler,
  and the flash race is structurally impossible** — `recipes` and `isLoading` come
  from the *same* result. Trade-off: the favorite state is frozen at fetch time and
  there's no offline cache.

  **Restore favorite reactivity** without re-fetching: observe `userData` and
  recompute each item's favorite state in place (the toggle is still handled
  outside the VM; this only refreshes the displayed heart):

  ```kotlin
  private fun observeFavorites() {
      userDataRepository.userData
          .onEach { userData ->
              _state.update { it.copy(recipes = it.recipes.map { r -> LikeableRecipe(r.recipe, userData) }) }
          }
          .launchIn(viewModelScope)
  }
  ```

One-shot pattern (repo returns `DataResult<List<…>>`; use `onSuccess`/`onFailure`):

```kotlin
init { loadData() }   // no observe()

private fun loadData() {                       // initial: drives isLoading + error
    viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        repository.getX(arg)
            .onSuccess { data -> _state.update { it.copy(items = data, isLoading = false) } }
            .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.userMessage()) } }
    }
}

private fun refresh() {                        // pull/retry: drives isRefreshing
    viewModelScope.launch {
        _state.update { it.copy(isRefreshing = true) }
        repository.getX(arg)
            .onSuccess { data -> _state.update { it.copy(items = data, error = null) } }
            .onFailure { e -> _events.send(Event.ShowSnackbar(e.userMessage())) }  // keep content
        _state.update { it.copy(isRefreshing = false) }
    }
}
```

Initial failure → `error` set → full-screen error (`FullErrorScreen`, retry by
pulling). Refresh failure → keep the list, signal via a `ShowSnackbar` event. Map
errors to text with `AppError.userMessage()`.

---

## Action (Intent)

```kotlin
sealed interface CategoryAction {
    data object OnRefreshClick : CategoryAction
    data class OnRecipeClick(val recipeId: String) : CategoryAction
    data class OnToggleFavorite(val recipeId: String) : CategoryAction
}
```

---

## Event (one-time side effects)

```kotlin
sealed interface CategoryEvent {
    data class NavigateToDetail(val recipeId: String) : CategoryEvent
    data class ShowSnackbar(val message: String) : CategoryEvent
}
```

---

## ViewModel

```kotlin
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state = _state.asStateFlow()

    private val _events = Channel<CategoryEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CategoryAction) {
        when (action) {
            is CategoryAction.OnRefreshClick -> loadRecipes()
            is CategoryAction.OnRecipeClick -> viewModelScope.launch {
                _events.send(CategoryEvent.NavigateToDetail(action.recipeId))
            }
            is CategoryAction.OnToggleFavorite -> { /* ... */ }
        }
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            recipeRepository.getRecipes()
                .onSuccess { recipes ->
                    _state.update { it.copy(recipes = recipes, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(CategoryEvent.ShowSnackbar(error.message.orEmpty()))
                }
        }
    }
}
```

> Use a `Channel` (capacity `RENDEZVOUS`) + `receiveAsFlow()` for events, not a
> `MutableSharedFlow` with `tryEmit`: `tryEmit` silently drops events when there
> is no active collector (e.g. during config change), whereas a `Channel`
> buffers until collection resumes.

---

## Action vs Event — never duplicate the payload

**The Action carries the minimal user intent; the ViewModel derives the full
side-effect payload from its `State`.** Never copy the same fields into both the
Action and the Event — that makes the ViewModel a pure pass-through and signals
the Action is over-specified.

If a click needs data the screen already holds (a list, an index), pass only
what the *user* expressed (e.g. the clicked index) and let the ViewModel read
the rest from `state.value`:

```kotlin
// ❌ Redundant: same payload in Action and Event, VM just copies it.
data class OnRecipeClick(val ids: List<String>, val index: Int, val title: String) : CategoryAction
data class NavigateToRecipe(val ids: List<String>, val index: Int, val title: String) : CategoryEvent

// ✅ Action = minimal intent; VM derives the Event payload from State.
data class OnRecipeClick(val index: Int) : CategoryAction
data class NavigateToRecipe(val ids: List<String>, val index: Int, val title: String) : CategoryEvent
```

```kotlin
is CategoryAction.OnRecipeClick -> {
    val recipes = state.value.recipes
    viewModelScope.launch {
        _events.send(
            CategoryEvent.NavigateToRecipe(
                ids = recipes.map { it.recipe.idMeal },
                index = action.index,
                title = recipes[action.index].recipe.strMeal,
            )
        )
    }
}
```

This also keeps derivations (like `recipes.map { it.recipe.idMeal }`) out of the
Screen, where they don't belong.

**Residual pass-through nav (no derivation).** Some navigation has nothing to
derive — `OnBackClick → NavigateBack`, or `OnCategoryClick(category) →
NavigateToCategory(category)`. Keep these as Action→Event anyway: the Screen
keeps a single `onAction` entry point (better previews and testability), and a
one-line `data object`/`data class` is a negligible cost. Routing pure nav
through a separate Route callback is **not** the convention here.

### Aggregated screens with several lists

When one screen shows multiple lists (e.g. a Home feed with sections), the
minimal intent is a **source discriminator + index**, and the VM resolves the
list from `state` before deriving. The shared list components stay dumb — they
emit only the clicked `index`, never pre-computed ids.

```kotlin
sealed interface RecipeSource { data object Latest; data object Japanese; data class Area(val name: String) }
data class OnRecipeClick(val source: RecipeSource, val index: Int) : HomeAction

private fun openRecipe(source: RecipeSource, index: Int) {
    val recipes = when (source) {                       // resolve the list…
        RecipeSource.Latest -> state.value.latest.recipes
        RecipeSource.Japanese -> state.value.japanese.recipes
        is RecipeSource.Area -> state.value.areas.recipes[source.name].orEmpty()
    }
    val clicked = recipes.getOrNull(index) ?: return    // …guard, then derive
    viewModelScope.launch { _events.send(NavigateToRecipe(recipes.map { it.id }, index, clicked.title)) }
}
```

For per-section state, split logic into **delegates** (one per section) that the
ViewModel implements by delegation; the VM aggregates their `StateFlow`s with
`combine` and bridges any delegate events into its own `Channel<Event>`.

---

## UI Model (Presentation Model)

When a domain model needs UI-specific formatting (dates, units, currency), create a dedicated UI model in the presentation layer:

```kotlin
data class RecipeUi(
    val id: String,
    val title: String,
    val formattedDuration: String  // e.g. "45 min"
)

fun Recipe.toRecipeUi(): RecipeUi = RecipeUi(
    id = id,
    title = title,
    formattedDuration = duration.format(...)
)
```

UI models are always suffixed with `Ui` (e.g., `RecipeUi`, `CategoryItemUi`).

---

## Composable Structure

The Route and Screen composable live in **two separated files**
(`CategoryRoute.kt` & `CategoryScreen.kt`), matching the existing feature layout
in AGENTS.md.

### Route Composable (suffixed `Route`)

Obtains the ViewModel via `hiltViewModel()`, collects events in a
`LaunchedEffect`, and passes `state` + `onAction` down. Holds navigation
callbacks.

### Screen Composable (suffixed `Screen`)

Receives only `state` and `onAction`. No ViewModel reference. Previewable.

```kotlin
// CategoryRoute.kt
@Composable
fun CategoryRoute(
    onNavigateToDetail: (String) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = LocalSnackbarHostState.current

    // Collect one-time events. `state` is already lifecycle-aware via
    // collectAsStateWithLifecycle; the event Channel buffers until collected.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CategoryEvent.NavigateToDetail -> onNavigateToDetail(event.recipeId)
                is CategoryEvent.ShowSnackbar -> snackbarHost.showSnackbar(event.message)
            }
        }
    }

    CategoryScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
```

```kotlin
// CategoryScreen.kt
@Composable
fun CategoryScreen(
    state: CategoryState,
    onAction: (CategoryAction) -> Unit
) { ... }

@Preview
@Composable
private fun CategoryScreenPreview() {
    CategoryScreen(state = CategoryState(), onAction = {})
}
```

---

## Process Death

When a screen involves complex forms or critical user input, restore essential fields using `SavedStateHandle`:

```kotlin
@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(
        AddRecipeState(
            title = savedStateHandle["title"] ?: "",
            instructions = savedStateHandle["instructions"] ?: ""
        )
    )

    fun onAction(action: AddRecipeAction) {
        when (action) {
            is AddRecipeAction.OnTitleChange -> {
                savedStateHandle["title"] = action.title
                _state.update { it.copy(title = action.title) }
            }
        }
    }
}
```

Only save what truly matters after process death — not the entire state.

---

## Naming Conventions

| Thing | Convention          | Example             |
|---|---------------------|---------------------|
| ViewModel | `<Screen>ViewModel` | `CategoryViewModel` |
| State | `<Screen>State`     | `CategoryState`     |
| Action | `<Screen>Action`    | `CategoryAction`    |
| Event | `<Screen>Event`     | `CategoryEvent`     |
| Route composable | `<Screen>Route`     | `CategoryRoute`     |
| Screen composable | `<Screen>Screen`    | `CategoryScreen`    |
| UI model | `<Model>Ui`         | `RecipeUi`          |

---

## Checklist: Adding / Migrating a Screen

- [ ] Define `State`, `Action`, `Event` in the feature's `impl` module
- [ ] Implement/convert the `@HiltViewModel` ViewModel: `StateFlow<State>` + `onAction(Action)` + `Channel<Event>`
- [ ] Replace per-callback lambdas with a single `onAction: (Action) -> Unit`
- [ ] Keep Action payloads minimal (user intent only); derive Event payloads from `state.value` in the ViewModel — never duplicate fields between an Action and its Event
- [ ] Split into `<Screen>Route` (holds ViewModel, observes events) and `<Screen>Screen` (pure state + onAction, previewable) files
- [ ] Move one-time side effects (navigation, snackbars) from state into `Event`, collected in a `LaunchedEffect`
- [ ] If the screen has pull-to-refresh, keep `isLoading` (initial) and `isRefreshing` (user refresh) separate, driven by two named functions — never a boolean flag
- [ ] Map domain models to `Ui` models when UI-specific formatting is needed
- [ ] Add `SavedStateHandle` for form fields that must survive process death
- [ ] Verify the preview renders with `<Screen>State()`
