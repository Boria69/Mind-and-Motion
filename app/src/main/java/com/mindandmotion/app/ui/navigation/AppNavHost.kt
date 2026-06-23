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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindandmotion.app.MindAndMotionApp
import com.mindandmotion.app.ui.tasks.TaskEditScreen
import com.mindandmotion.app.ui.tasks.TaskListScreen
import com.mindandmotion.app.ui.tasks.TaskViewModel
import com.mindandmotion.app.ui.tasks.TaskViewModelFactory

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val container = (LocalContext.current.applicationContext as MindAndMotionApp).container

    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(container.taskRepository)
    )

    Scaffold(
        bottomBar = { BottomBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.TASKS.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.TASKS.route) {
                TaskListScreen(
                    viewModel = taskViewModel,
                    onAddTask = { navController.navigate("${Routes.TASK_EDIT}?taskId=-1") },
                    onEditTask = { id -> navController.navigate("${Routes.TASK_EDIT}?taskId=$id") }
                )
            }
            composable(
                route = "${Routes.TASK_EDIT}?taskId={taskId}",
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val rawId = backStackEntry.arguments?.getLong("taskId") ?: -1L
                TaskEditScreen(
                    viewModel = taskViewModel,
                    taskId = rawId.takeIf { it >= 0 },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(TopLevelDestination.JOURNAL.route) { PlaceholderScreen("Journal") }
            composable(TopLevelDestination.POMODORO.route) { PlaceholderScreen("Pomodoro") }
            composable(TopLevelDestination.SETTINGS.route) { PlaceholderScreen("Settings") }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name, style = MaterialTheme.typography.headlineMedium)
    }
}
