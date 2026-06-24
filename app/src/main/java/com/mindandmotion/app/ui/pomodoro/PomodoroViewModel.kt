package com.mindandmotion.app.ui.pomodoro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindandmotion.app.pomodoro.PomodoroService
import com.mindandmotion.app.pomodoro.TimerConfig
import com.mindandmotion.app.pomodoro.TimerEngine
import com.mindandmotion.app.pomodoro.TimerPhase
import com.mindandmotion.app.pomodoro.TimerStatus
import com.mindandmotion.app.util.Prefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PomodoroUiState(
    val phase: TimerPhase,
    val status: TimerStatus,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val completedWorkSessions: Int
) {
    val isRunning: Boolean get() = status == TimerStatus.RUNNING
    val isIdle: Boolean get() = status == TimerStatus.IDLE

    val progress: Float
        get() = if (totalSeconds <= 0) 0f else (totalSeconds - remainingSeconds).toFloat() / totalSeconds

    val timeText: String
        get() = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60)
}

class PomodoroViewModel(
    application: Application,
    private val engine: TimerEngine,
    private val prefs: Prefs
) : AndroidViewModel(application) {

    val uiState: StateFlow<PomodoroUiState> = engine.state
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, engine.state.value.toUiState())

    init {
        viewModelScope.launch {
            prefs.pomodoroPrefs.collect { p ->
                if (engine.state.value.status == TimerStatus.IDLE) {
                    engine.configure(
                        TimerConfig(
                            workMinutes = p.workMinutes,
                            shortBreakMinutes = p.breakMinutes,
                            longBreakMinutes = p.longBreakMinutes
                        )
                    )
                }
            }
        }
    }

    fun onStart() {
        PomodoroService.start(getApplication())
    }

    fun onPause() {
        if (engine.state.value.status == TimerStatus.RUNNING) {
            PomodoroService.pause(getApplication())
        }
    }

    fun onReset() {
        if (engine.state.value.status != TimerStatus.IDLE) {
            PomodoroService.reset(getApplication())
        }
    }
}

private fun com.mindandmotion.app.pomodoro.TimerState.toUiState() = PomodoroUiState(
    phase = phase,
    status = status,
    remainingSeconds = remainingSeconds,
    totalSeconds = totalSeconds,
    completedWorkSessions = completedWorkSessions
)

class PomodoroViewModelFactory(
    private val application: Application,
    private val engine: TimerEngine,
    private val prefs: Prefs
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            return PomodoroViewModel(application, engine, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
