package com.mindandmotion.app.di

import android.content.Context
import com.mindandmotion.app.data.AppDatabase
import com.mindandmotion.app.data.journal.JournalRepository
import com.mindandmotion.app.data.task.TaskRepository

class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy { AppDatabase.build(context) }

    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }

    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalDao()) }
}
