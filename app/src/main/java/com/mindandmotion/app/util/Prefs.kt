package com.mindandmotion.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "mind_and_motion_prefs")

enum class AppTheme { SYSTEM, LIGHT, DARK }

data class PomodoroPrefs(
    val workMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val longBreakMinutes: Int = 15
)

data class Session(
    val userId: Long,
    val email: String
)

/**
 * Wrapper peste DataStore pentru preferințele aplicației.
 *
 * NOTĂ pentru [TU]: PomodoroViewModel / PomodoroService (Epic 3) ar trebui să citească
 * [pomodoroPrefs] în loc să hardcodeze duratele de 25/5/15 minute.
 */
class Prefs(private val context: Context) {

    private object Keys {
        val WORK_MINUTES = intPreferencesKey("work_minutes")
        val BREAK_MINUTES = intPreferencesKey("break_minutes")
        val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
        val THEME = stringPreferencesKey("theme")
        val SESSION_USER_ID = longPreferencesKey("session_user_id")
        val SESSION_EMAIL = stringPreferencesKey("session_email")
    }

    val session: Flow<Session?> = context.dataStore.data.map { prefs ->
        val userId = prefs[Keys.SESSION_USER_ID]
        val email = prefs[Keys.SESSION_EMAIL]
        if (userId != null && email != null) Session(userId, email) else null
    }

    val pomodoroPrefs: Flow<PomodoroPrefs> = context.dataStore.data.map { prefs ->
        PomodoroPrefs(
            workMinutes = prefs[Keys.WORK_MINUTES] ?: 25,
            breakMinutes = prefs[Keys.BREAK_MINUTES] ?: 5,
            longBreakMinutes = prefs[Keys.LONG_BREAK_MINUTES] ?: 15
        )
    }

    val theme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.THEME]) {
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            AppTheme.DARK.name -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    suspend fun setWorkMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.WORK_MINUTES] = minutes }
    }

    suspend fun setBreakMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.BREAK_MINUTES] = minutes }
    }

    suspend fun setLongBreakMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.LONG_BREAK_MINUTES] = minutes }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setSession(userId: Long, email: String) {
        context.dataStore.edit {
            it[Keys.SESSION_USER_ID] = userId
            it[Keys.SESSION_EMAIL] = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(Keys.SESSION_USER_ID)
            it.remove(Keys.SESSION_EMAIL)
        }
    }
}
