package com.mindandmotion.app.pomodoro

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TimerEngineTest {

    private fun TimerEngine.tick(times: Int) = repeat(times) { tick() }

    @Test
    fun `starts idle on the work phase with the full work duration`() {
        val engine = TimerEngine(TimerConfig(workMinutes = 25))

        val state = engine.state.value
        assertEquals(TimerPhase.WORK, state.phase)
        assertEquals(TimerStatus.IDLE, state.status)
        assertEquals(25 * 60, state.remainingSeconds)
        assertEquals(25 * 60, state.totalSeconds)
        assertEquals(0, state.completedWorkSessions)
    }

    @Test
    fun `start sets the status to running`() {
        val engine = TimerEngine()

        engine.start()

        assertEquals(TimerStatus.RUNNING, engine.state.value.status)
    }

    @Test
    fun `tick is ignored while idle`() {
        val engine = TimerEngine(TimerConfig(workMinutes = 1))

        engine.tick()

        assertEquals(60, engine.state.value.remainingSeconds)
        assertEquals(TimerStatus.IDLE, engine.state.value.status)
    }

    @Test
    fun `tick counts down one second while running`() {
        val engine = TimerEngine(TimerConfig(workMinutes = 1))

        engine.start()
        engine.tick(10)

        assertEquals(50, engine.state.value.remainingSeconds)
    }

    @Test
    fun `pause freezes the countdown until started again`() {
        val engine = TimerEngine(TimerConfig(workMinutes = 1))

        engine.start()
        engine.tick(5)
        engine.pause()
        engine.tick(5)

        assertEquals(55, engine.state.value.remainingSeconds)
        assertEquals(TimerStatus.PAUSED, engine.state.value.status)

        engine.start()
        engine.tick(5)

        assertEquals(50, engine.state.value.remainingSeconds)
    }

    @Test
    fun `finishing work moves to a short break and counts the session`() {
        val engine = TimerEngine(TimerConfig(workMinutes = 1, shortBreakMinutes = 1))

        engine.start()
        engine.tick(60)

        val state = engine.state.value
        assertEquals(TimerPhase.SHORT_BREAK, state.phase)
        assertEquals(TimerStatus.RUNNING, state.status)
        assertEquals(60, state.remainingSeconds)
        assertEquals(1, state.completedWorkSessions)
    }

    @Test
    fun `finishing a break returns to work`() {
        val engine = TimerEngine(TimerConfig(workMinutes = 1, shortBreakMinutes = 1))

        engine.start()
        engine.tick(60)
        engine.tick(60)

        assertEquals(TimerPhase.WORK, engine.state.value.phase)
    }

    @Test
    fun `a long break starts after the configured number of work sessions`() {
        val engine = TimerEngine(
            TimerConfig(
                workMinutes = 1,
                shortBreakMinutes = 1,
                longBreakMinutes = 1,
                sessionsBeforeLongBreak = 2
            )
        )

        engine.start()
        engine.tick(60)
        engine.tick(60)
        engine.tick(60)

        val state = engine.state.value
        assertEquals(TimerPhase.LONG_BREAK, state.phase)
        assertEquals(2, state.completedWorkSessions)
    }

    @Test
    fun `reset returns to the idle work state`() {
        val engine = TimerEngine(TimerConfig(workMinutes = 1))

        engine.start()
        engine.tick(30)
        engine.reset()

        val state = engine.state.value
        assertEquals(TimerPhase.WORK, state.phase)
        assertEquals(TimerStatus.IDLE, state.status)
        assertEquals(60, state.remainingSeconds)
        assertEquals(0, state.completedWorkSessions)
    }

    @Test
    fun `configure applies new durations and resets`() {
        val engine = TimerEngine(TimerConfig(workMinutes = 25))

        engine.start()
        engine.configure(TimerConfig(workMinutes = 10))

        val state = engine.state.value
        assertEquals(10 * 60, state.remainingSeconds)
        assertEquals(TimerStatus.IDLE, state.status)
    }

    @Test
    fun `completing a phase emits the finished phase`() = runTest {
        val engine = TimerEngine(TimerConfig(workMinutes = 1, shortBreakMinutes = 1))
        val completed = mutableListOf<TimerPhase>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            engine.phaseCompletions.collect { completed.add(it) }
        }

        engine.start()
        engine.tick(60)
        engine.tick(60)

        assertEquals(listOf(TimerPhase.WORK, TimerPhase.SHORT_BREAK), completed)
    }
}
