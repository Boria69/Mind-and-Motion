package com.mindandmotion.app.pomodoro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mindandmotion.app.MainActivity
import com.mindandmotion.app.R

class PomodoroNotifications(private val context: Context) {

    private val manager = context.getSystemService(NotificationManager::class.java)

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.createNotificationChannel(
            NotificationChannel(
                ONGOING_CHANNEL_ID,
                "Cronometru Pomodoro",
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alerte Pomodoro",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    fun buildOngoing(state: TimerState): Notification {
        val builder = NotificationCompat.Builder(context, ONGOING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pomodoro)
            .setContentTitle(phaseTitle(state.phase))
            .setContentText(ongoingText(state))
            .setContentIntent(openAppIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (state.status == TimerStatus.RUNNING) {
            builder.addAction(
                android.R.drawable.ic_media_pause,
                "Pauză",
                command(PomodoroService.ACTION_PAUSE)
            )
        } else {
            builder.addAction(
                android.R.drawable.ic_media_play,
                "Continuă",
                command(PomodoroService.ACTION_START)
            )
        }
        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Reset",
            command(PomodoroService.ACTION_RESET)
        )
        return builder.build()
    }

    fun updateOngoing(state: TimerState) {
        manager.notify(ONGOING_NOTIFICATION_ID, buildOngoing(state))
    }

    fun notifyPhaseCompleted(finished: TimerPhase) {
        val title: String
        val text: String
        when (finished) {
            TimerPhase.WORK -> {
                title = "Sesiune terminată"
                text = "Ia o pauză."
            }
            TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> {
                title = "Pauză terminată"
                text = "Înapoi la concentrare."
            }
        }
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pomodoro)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(ALERT_NOTIFICATION_ID, notification)
    }

    private fun ongoingText(state: TimerState): String {
        val time = "%02d:%02d".format(state.remainingSeconds / 60, state.remainingSeconds % 60)
        return if (state.status == TimerStatus.PAUSED) "$time · în pauză" else time
    }

    private fun phaseTitle(phase: TimerPhase): String = when (phase) {
        TimerPhase.WORK -> "Concentrare"
        TimerPhase.SHORT_BREAK -> "Pauză scurtă"
        TimerPhase.LONG_BREAK -> "Pauză lungă"
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun command(action: String): PendingIntent {
        val intent = Intent(context, PomodoroService::class.java).setAction(action)
        return PendingIntent.getService(
            context, action.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val ONGOING_CHANNEL_ID = "pomodoro_ongoing"
        const val ALERT_CHANNEL_ID = "pomodoro_alerts"
        const val ONGOING_NOTIFICATION_ID = 1001
        const val ALERT_NOTIFICATION_ID = 1002
    }
}
