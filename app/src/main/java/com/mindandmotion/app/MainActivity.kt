package com.mindandmotion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mindandmotion.app.ui.navigation.AppNavHost
import com.mindandmotion.app.ui.theme.MindAndMotionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindAndMotionTheme {
                AppNavHost()
            }
        }
    }
}
