package com.mindandmotion.app.di

import android.content.Context
import com.mindandmotion.app.data.AppDatabase
import com.mindandmotion.app.data.journal.JournalRepository
import com.mindandmotion.app.data.task.TaskRepository

/**
 * Manual dependency-injection container, created once by [com.mindandmotion.app.MindAndMotionApp]
 * and held for the whole process lifetime.
 *
 * It is the single place that instantiates app-wide singletons — the Room
 * database and the repositories — and hands them to the ViewModels.
 *  - `AppDatabase` + `TaskRepository`    → MM-10 / MM-11
 *  - `JournalRepository`                 → MM-20 / MM-21
 *
 * Totul e `lazy`: nimic nu se construiește până la prima folosire.
 */
class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy { AppDatabase.build(context) }

    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }

    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalDao()) }
}
