package com.mindandmotion.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Single-Activity navigation shell. A [Scaffold] hosts the [BottomBar] and a
 * [NavHost] whose start destination is the Tasks tab.
 *
 * Each top-level route currently shows a [PlaceholderScreen]; the real screens
 * replace these in their feature tickets (Tasks MM-12, Journal MM-22,
 * Pomodoro MM-32, Settings MM-40).
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.TASKS.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.TASKS.route) { PlaceholderScreen("Tasks") }
            composable(TopLevelDestination.JOURNAL.route) { PlaceholderScreen("Journal") }
            composable(TopLevelDestination.POMODORO.route) { PlaceholderScreen("Pomodoro") }
            composable(TopLevelDestination.SETTINGS.route) { PlaceholderScreen("Settings") }
        }
    }
}

/** Temporary content for a tab whose real screen has not been built yet. */
@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name, style = MaterialTheme.typography.headlineMedium)
    }
}
