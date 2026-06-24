package com.mindandmotion.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.mindandmotion.app.ui.auth.AuthViewModel
import com.mindandmotion.app.ui.auth.AuthViewModelFactory
import com.mindandmotion.app.ui.auth.LoginScreen
import com.mindandmotion.app.ui.auth.RegisterScreen
import com.mindandmotion.app.ui.auth.SessionState
import com.mindandmotion.app.ui.inspiration.InspirationScreen
import com.mindandmotion.app.ui.inspiration.QuotesViewModel
import com.mindandmotion.app.ui.inspiration.QuotesViewModelFactory
import com.mindandmotion.app.ui.journal.JournalCalendarScreen
import com.mindandmotion.app.ui.journal.JournalEntryScreen
import com.mindandmotion.app.ui.journal.JournalViewModel
import com.mindandmotion.app.ui.journal.JournalViewModelFactory
import com.mindandmotion.app.ui.pomodoro.PomodoroScreen
import com.mindandmotion.app.ui.pomodoro.PomodoroViewModel
import com.mindandmotion.app.ui.pomodoro.PomodoroViewModelFactory
import com.mindandmotion.app.ui.settings.AboutScreen
import com.mindandmotion.app.ui.settings.SettingsScreen
import com.mindandmotion.app.ui.tasks.TaskEditScreen
import com.mindandmotion.app.ui.tasks.TaskListScreen
import com.mindandmotion.app.ui.tasks.TaskViewModel
import com.mindandmotion.app.ui.tasks.TaskViewModelFactory

@Composable
fun AppNavHost() {
    val container = (LocalContext.current.applicationContext as MindAndMotionApp).container

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(container.authRepository, container.prefs)
    )
    val sessionState by authViewModel.sessionState.collectAsState()

    when (val session = sessionState) {
        SessionState.Loading -> LoadingScreen()
        SessionState.LoggedOut -> AuthNavHost(authViewModel)
        is SessionState.LoggedIn -> MainNavHost(
            userEmail = session.email,
            onLogout = authViewModel::logout
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AuthNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainNavHost(userEmail: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val container = (LocalContext.current.applicationContext as MindAndMotionApp).container
    val app = LocalContext.current.applicationContext as Application

    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(container.taskRepository)
    )
    val pomodoroViewModel: PomodoroViewModel = viewModel(
        factory = PomodoroViewModelFactory(app, container.timerEngine, container.prefs)
    )
    val journalViewModel: JournalViewModel = viewModel(
        factory = JournalViewModelFactory(container.journalRepository)
    )
    val quotesViewModel: QuotesViewModel = viewModel(
        factory = QuotesViewModelFactory(container.quotesRepository)
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
            composable(TopLevelDestination.JOURNAL.route) {
                JournalCalendarScreen(
                    viewModel = journalViewModel,
                    onOpenDay = { navController.navigate(Routes.JOURNAL_ENTRY) }
                )
            }
            composable(Routes.JOURNAL_ENTRY) {
                JournalEntryScreen(
                    viewModel = journalViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(TopLevelDestination.POMODORO.route) {
                PomodoroScreen(viewModel = pomodoroViewModel)
            }
            composable(TopLevelDestination.INSPIRATION.route) {
                InspirationScreen(viewModel = quotesViewModel)
            }
            composable(TopLevelDestination.SETTINGS.route) {
                SettingsScreen(
                    prefs = container.prefs,
                    userEmail = userEmail,
                    onLogout = onLogout,
                    onNavigateToAbout = { navController.navigate(Routes.ABOUT) }
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
