package com.mindandmotion.app.di

import android.content.Context
import com.mindandmotion.app.data.AppDatabase
import com.mindandmotion.app.data.auth.AuthRepository
import com.mindandmotion.app.data.journal.JournalRepository
import com.mindandmotion.app.data.quotes.QuotesApi
import com.mindandmotion.app.data.quotes.QuotesRepository
import com.mindandmotion.app.data.task.TaskRepository
import com.mindandmotion.app.pomodoro.TimerEngine
import com.mindandmotion.app.util.Prefs

class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy { AppDatabase.build(context) }

    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }

    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalDao()) }

    val quotesRepository: QuotesRepository by lazy { QuotesRepository(QuotesApi.service) }
    val authRepository: AuthRepository by lazy { AuthRepository(database.userDao()) }

    val prefs: Prefs by lazy { Prefs(context) }

    val timerEngine: TimerEngine by lazy { TimerEngine() }
}