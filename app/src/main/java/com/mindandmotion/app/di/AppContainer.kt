package com.mindandmotion.app.di

import android.content.Context
import com.mindandmotion.app.data.AppDatabase
import com.mindandmotion.app.data.journal.JournalRepository
import com.mindandmotion.app.data.task.TaskRepository
import com.mindandmotion.app.pomodoro.TimerEngine
import com.mindandmotion.app.util.Prefs

class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy { AppDatabase.build(context) }

    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }

    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalDao()) }

    val prefs: Prefs by lazy { Prefs(context) }

    val timerEngine: TimerEngine by lazy { TimerEngine() }
}
