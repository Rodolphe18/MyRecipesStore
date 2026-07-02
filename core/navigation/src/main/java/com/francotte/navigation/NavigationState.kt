package com.francotte.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * Create a navigation state that persists config changes and process death.
 */
@Composable
fun rememberNavigationState(
    startKey: NavKey,
    defaultTopLevelKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState {
    val rootStack = rememberNavBackStack(startKey)
    val topLevelStack = rememberNavBackStack(defaultTopLevelKey)
    val subStacks = topLevelKeys.associateWith { key -> rememberNavBackStack(key) }

    return remember(startKey, defaultTopLevelKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            rootStack = rootStack,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
}

/**
 * State holder for navigation state.
 *
 * @param startKey - the starting navigation key. The user will exit the app through this key.
 * @param topLevelStack - the top level back stack. It holds only top level keys.
 * @param subStacks - the back stacks for each top level key
 */
class NavigationState(
    val startKey: NavKey,
    val rootStack: NavBackStack<NavKey>,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val topLevelKeys get() = subStacks.keys

    val currentTopLevelKey: NavKey by derivedStateOf {
        // si on est dans root (ex splash), topLevelStack peut rester sur Home par défaut
        topLevelStack.last()
    }

    val isInRoot: Boolean by derivedStateOf { rootStack.isNotEmpty() }

    val currentKey: NavKey by derivedStateOf {
        when {
            isInRoot -> rootStack.last()
            else -> currentSubStack.last()
        }
    }

    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentTopLevelKey]
            ?: error("Sub stack for $currentTopLevelKey does not exist")
}


@Composable
fun NavigationState.toNavEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {

    // Root (Splash, login modal, etc.)
    val rootDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator<NavKey>(),
    )
    val rootEntries = rememberDecoratedNavEntries(
        backStack = rootStack,
        entryDecorators = rootDecorators,
        entryProvider = entryProvider,
    )

    // Top-level substacks
    val decoratedEntries = subStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    val topLevelEntries = topLevelStack.flatMap { decoratedEntries[it].orEmpty() }

    // Si rootStack non vide -> on affiche root au-dessus (ou à la place)
    // Ici: à la place (comme un graph séparé)
    return if (rootStack.isNotEmpty()) {
        rootEntries.toMutableStateList()
    } else {
        topLevelEntries.toMutableStateList()
    }
}
