package com.mindandmotion.app.pomodoro

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.mindandmotion.app.MindAndMotionApp
import com.mindandmotion.app.util.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PomodoroService : Service() {

    private val container by lazy { (application as MindAndMotionApp).container }
    private val engine: TimerEngine by lazy { container.timerEngine }
    private val prefs: Prefs by lazy { container.prefs }

    private lateinit var notifications: PomodoroNotifications
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var ticker: Job? = null
    private var observersStarted = false

    override fun onCreate() {
        super.onCreate()
        notifications = PomodoroNotifications(this)
        notifications.ensureChannels()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()

        when (intent?.action) {
            ACTION_PAUSE -> handlePause()
            ACTION_RESET -> {
                handleReset()
                return START_NOT_STICKY
            }
            else -> handleStart()
        }

        startObservers()
        return START_STICKY
    }

    private fun startForeground() {
        ServiceCompat.startForeground(
            this,
            PomodoroNotifications.ONGOING_NOTIFICATION_ID,
            notifications.buildOngoing(engine.state.value),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )
    }

    private fun handleStart() {
        scope.launch {
            if (engine.state.value.status == TimerStatus.IDLE) {
                val config = prefs.pomodoroPrefs.first()
                engine.configure(
                    TimerConfig(
                        workMinutes = config.workMinutes,
                        shortBreakMinutes = config.breakMinutes,
                        longBreakMinutes = config.longBreakMinutes
                    )
                )
            }
            engine.start()
            startTicker()
            notifications.updateOngoing(engine.state.value)
        }
    }

    private fun handlePause() {
        engine.pause()
        stopTicker()
        notifications.updateOngoing(engine.state.value)
    }

    private fun handleReset() {
        stopTicker()
        engine.reset()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                delay(1000)
                engine.tick()
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun startObservers() {
        if (observersStarted) return
        observersStarted = true
        scope.launch {
            engine.state.collect { state ->
                if (state.status != TimerStatus.IDLE) {
                    notifications.updateOngoing(state)
                }
            }
        }
        scope.launch {
            engine.phaseCompletions.collect { finished ->
                notifications.notifyPhaseCompleted(finished)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.mindandmotion.app.pomodoro.START"
        const val ACTION_PAUSE = "com.mindandmotion.app.pomodoro.PAUSE"
        const val ACTION_RESET = "com.mindandmotion.app.pomodoro.RESET"

        fun start(context: Context) = sendCommand(context, ACTION_START)
        fun pause(context: Context) = sendCommand(context, ACTION_PAUSE)
        fun reset(context: Context) = sendCommand(context, ACTION_RESET)

        private fun sendCommand(context: Context, action: String) {
            val intent = Intent(context, PomodoroService::class.java).setAction(action)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
