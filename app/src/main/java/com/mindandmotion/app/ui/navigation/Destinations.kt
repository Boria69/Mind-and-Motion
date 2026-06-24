package com.mindandmotion.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.mindandmotion.app.R

/**
 * The top-level destinations shown in the [BottomBar]. Each maps to a
 * single navigation route, a label and an icon. Detail/edit routes that are
 * pushed on top of a tab live in [Routes].
 */
enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    TASKS("tasks", R.string.nav_tasks, Icons.Filled.CheckCircle),
    JOURNAL("journal", R.string.nav_journal, Icons.AutoMirrored.Filled.MenuBook),
    POMODORO("pomodoro", R.string.nav_pomodoro, Icons.Filled.Timer),
    INSPIRATION("inspiration", R.string.nav_inspiration, Icons.Filled.FormatQuote),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings),
}

/**
 * Routes pushed on top of a tab (not part of the bottom bar). Screens for
 * these are added in their feature tickets; the nav shell only declares them.
 */
object Routes {
    /** Create/edit a task. Optional `id` argument: absent = create. (MM-13) */
    const val TASK_EDIT = "task_edit"

    /** View/edit the journal entry for a given `date`. (MM-23) */
    const val JOURNAL_ENTRY = "journal_entry"

    /** About screen, reached from Settings. (MM-41) */
    const val ABOUT = "about"
}