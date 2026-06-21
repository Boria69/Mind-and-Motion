package com.mindandmotion.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindandmotion.app.ui.components.AppTopBar
import com.mindandmotion.app.ui.components.SectionCard
import com.mindandmotion.app.util.AppTheme
import com.mindandmotion.app.util.Prefs
import com.mindandmotion.app.util.PomodoroPrefs
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefs: Prefs,
    onNavigateToAbout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val pomodoroPrefs by prefs.pomodoroPrefs.collectAsState(initial = PomodoroPrefs())
    val theme by prefs.theme.collectAsState(initial = AppTheme.SYSTEM)

    Scaffold(topBar = { AppTopBar(title = "Setări", onBack = onBack) }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "Durate Pomodoro") {
                DurationStepper(
                    label = "Sesiune de lucru (min)",
                    value = pomodoroPrefs.workMinutes,
                    onValueChange = { scope.launch { prefs.setWorkMinutes(it) } }
                )
                DurationStepper(
                    label = "Pauză scurtă (min)",
                    value = pomodoroPrefs.breakMinutes,
                    onValueChange = { scope.launch { prefs.setBreakMinutes(it) } }
                )
                DurationStepper(
                    label = "Pauză lungă (min)",
                    value = pomodoroPrefs.longBreakMinutes,
                    onValueChange = { scope.launch { prefs.setLongBreakMinutes(it) } }
                )
            }

            SectionCard(title = "Temă") {
                Column {
                    AppTheme.entries.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = theme == option,
                                onClick = { scope.launch { prefs.setTheme(option) } }
                            )
                            Text(option.label())
                        }
                    }
                }
            }

            TextButton(onClick = onNavigateToAbout) { Text("Despre aplicație") }
        }
    }
}

@Composable
private fun DurationStepper(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 1) onValueChange(value - 1) }) {
                Icon(Icons.Filled.Remove, contentDescription = "Micșorează")
            }
            Text("$value")
            IconButton(onClick = { onValueChange(value + 1) }) {
                Icon(Icons.Filled.Add, contentDescription = "Mărește")
            }
        }
    }
}

private fun AppTheme.label(): String = when (this) {
    AppTheme.SYSTEM -> "Sistem"
    AppTheme.LIGHT -> "Luminos"
    AppTheme.DARK -> "Întunecat"
}
