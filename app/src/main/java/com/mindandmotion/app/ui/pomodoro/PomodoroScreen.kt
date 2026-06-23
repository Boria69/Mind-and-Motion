package com.mindandmotion.app.ui.pomodoro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mindandmotion.app.pomodoro.TimerPhase
import com.mindandmotion.app.ui.components.AppTopBar

@Composable
fun PomodoroScreen(viewModel: PomodoroViewModel) {
    val state by viewModel.uiState.collectAsState()

    RequestNotificationPermission()

    Scaffold(topBar = { AppTopBar(title = "Pomodoro") }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = phaseLabel(state.phase),
                style = MaterialTheme.typography.titleLarge
            )

            TimerDial(progress = state.progress, timeText = state.timeText)

            Text(
                text = "Sesiuni terminate: ${state.completedWorkSessions}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                OutlinedIconButton(
                    onClick = viewModel::onReset,
                    enabled = !state.isIdle,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                }

                FilledIconButton(
                    onClick = { if (state.isRunning) viewModel.onPause() else viewModel.onStart() },
                    modifier = Modifier.size(80.dp)
                ) {
                    if (state.isRunning) {
                        Icon(Icons.Filled.Pause, contentDescription = "Pauză")
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerDial(progress: Float, timeText: String) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer-progress")
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(240.dp),
            strokeWidth = 12.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = timeText,
            style = MaterialTheme.typography.displayMedium
        )
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private fun phaseLabel(phase: TimerPhase): String = when (phase) {
    TimerPhase.WORK -> "Concentrare"
    TimerPhase.SHORT_BREAK -> "Pauză scurtă"
    TimerPhase.LONG_BREAK -> "Pauză lungă"
}
