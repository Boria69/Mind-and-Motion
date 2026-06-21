package com.mindandmotion.app.di

import android.content.Context

/**
 * Manual dependency-injection container, created once by [com.mindandmotion.app.MindAndMotionApp]
 * and held for the whole process lifetime.
 *
 * It is the single place that instantiates app-wide singletons — the Room
 * database and the repositories — and hands them to the ViewModels. Those
 * dependencies are wired in here as the feature data layers land:
 *  - `AppDatabase` + `TaskRepository`    → MM-10 / MM-11
 *  - `JournalRepository`                 → MM-20 / MM-21
 *
 * [context] is the application context, kept here because the Room database
 * builder will need it.
 */
class AppContainer(val context: Context)
