package com.mindandmotion.app

import android.app.Application
import com.mindandmotion.app.di.AppContainer

/**
 * Application entry point. Creates the [AppContainer] that holds the app-wide
 * dependencies (database, repositories) and keeps it alive for the whole
 * process lifetime.
 *
 * Registered in the manifest via `android:name=".MindAndMotionApp"`.
 */
class MindAndMotionApp : Application() {

    /** App-wide dependency container, available to ViewModels via the Application. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
