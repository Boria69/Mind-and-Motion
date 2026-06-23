package com.mindandmotion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mindandmotion.app.ui.navigation.AppNavHost
import com.mindandmotion.app.ui.theme.MindAndMotionTheme
import com.mindandmotion.app.util.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = (applicationContext as MindAndMotionApp).container
            val theme by container.prefs.theme.collectAsState(initial = AppTheme.SYSTEM)

            MindAndMotionTheme(
                darkTheme = when (theme) {
                    AppTheme.DARK -> true
                    AppTheme.LIGHT -> false
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                AppNavHost()
            }
        }
    }
}