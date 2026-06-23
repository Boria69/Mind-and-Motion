package com.mindandmotion.app.pomodoro

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TimerPhase { WORK, SHORT_BREAK, LONG_BREAK }

enum class TimerStatus { IDLE, RUNNING, PAUSED }

data class TimerConfig(
    val workMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4
)

data class TimerState(
    val phase: TimerPhase,
    val status: TimerStatus,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val completedWorkSessions: Int
)

class TimerEngine(config: TimerConfig = TimerConfig()) {

    private var config: TimerConfig = config

    private val _state = MutableStateFlow(idleState(config))
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _phaseCompletions = MutableSharedFlow<TimerPhase>(extraBufferCapacity = 8)
    val phaseCompletions: SharedFlow<TimerPhase> = _phaseCompletions.asSharedFlow()

    fun start() = _state.update { state ->
        if (state.status == TimerStatus.RUNNING) state
        else state.copy(status = TimerStatus.RUNNING)
    }

    fun pause() = _state.update { state ->
        if (state.status == TimerStatus.RUNNING) state.copy(status = TimerStatus.PAUSED)
        else state
    }

    fun reset() {
        _state.value = idleState(config)
    }

    fun configure(newConfig: TimerConfig) {
        config = newConfig
        reset()
    }

    fun tick() {
        val state = _state.value
        if (state.status != TimerStatus.RUNNING) return

        val remaining = state.remainingSeconds - 1
        if (remaining > 0) {
            _state.value = state.copy(remainingSeconds = remaining)
            return
        }

        _phaseCompletions.tryEmit(state.phase)

        val completedWorkSessions = state.completedWorkSessions +
            if (state.phase == TimerPhase.WORK) 1 else 0
        val next = nextPhase(state.phase, completedWorkSessions)
        val total = durationSeconds(next)
        _state.value = TimerState(
            phase = next,
            status = TimerStatus.RUNNING,
            remainingSeconds = total,
            totalSeconds = total,
            completedWorkSessions = completedWorkSessions
        )
    }

    private fun nextPhase(current: TimerPhase, completedWorkSessions: Int): TimerPhase =
        when (current) {
            TimerPhase.WORK ->
                if (config.sessionsBeforeLongBreak > 0 &&
                    completedWorkSessions % config.sessionsBeforeLongBreak == 0
                ) {
                    TimerPhase.LONG_BREAK
                } else {
                    TimerPhase.SHORT_BREAK
                }

            TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> TimerPhase.WORK
        }

    private fun durationSeconds(phase: TimerPhase): Int = when (phase) {
        TimerPhase.WORK -> config.workMinutes
        TimerPhase.SHORT_BREAK -> config.shortBreakMinutes
        TimerPhase.LONG_BREAK -> config.longBreakMinutes
    } * 60

    private fun idleState(config: TimerConfig): TimerState {
        val total = config.workMinutes * 60
        return TimerState(
            phase = TimerPhase.WORK,
            status = TimerStatus.IDLE,
            remainingSeconds = total,
            totalSeconds = total,
            completedWorkSessions = 0
        )
    }
}
