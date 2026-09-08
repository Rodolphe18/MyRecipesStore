package com.francotte.data.sync

/**
 * The synchronisations the app can request.
 *
 * Each kind maps to exactly one unique work name in the sync module, so asking twice for the same
 * kind while it is already running is a no-op rather than a duplicated network round trip.
 */
sealed interface SyncKind {
    /** Latest recipes and the home area sections. */
    data object Home : SyncKind

    /** The meal categories list. */
    data object Categories : SyncKind

    /** The areas and ingredients used by the search filters. */
    data object AreasAndIngredients : SyncKind

    /** Fills the full text search index, one batch of stale categories at a time. */
    data object SearchIndex : SyncKind

    /** Pushes the pending favorites to the server, and reconciles on login. */
    data class Favorites(val reason: FavoritesSyncReason) : SyncKind
}

/**
 * Why a favorites sync was requested.
 *
 * A [Login] reconciles the whole list with the server and prefetches the recipe details that are
 * missing locally; a [Toggle] only pushes what the user just changed.
 */
enum class FavoritesSyncReason { Login, Toggle }
