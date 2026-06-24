package com.mindandmotion.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindandmotion.app.data.auth.AuthError
import com.mindandmotion.app.data.auth.AuthRepository
import com.mindandmotion.app.data.auth.AuthResult
import com.mindandmotion.app.util.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val email: String) : SessionState
}

data class AuthFormState(
    val isSubmitting: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val prefs: Prefs
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = prefs.session
        .map { session ->
            if (session == null) SessionState.LoggedOut
            else SessionState.LoggedIn(session.email)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    fun login(email: String, password: String) {
        val validation = validateLogin(email, password)
        if (validation != null) {
            _formState.value = AuthFormState(error = validation)
            return
        }
        _formState.value = AuthFormState(isSubmitting = true)
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    prefs.setSession(result.userId, result.email)
                    _formState.value = AuthFormState()
                }
                is AuthResult.Failure -> {
                    _formState.value = AuthFormState(error = result.error.message())
                }
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        val validation = validateRegister(email, password, confirmPassword)
        if (validation != null) {
            _formState.value = AuthFormState(error = validation)
            return
        }
        _formState.value = AuthFormState(isSubmitting = true)
        viewModelScope.launch {
            when (val result = authRepository.register(email, password)) {
                is AuthResult.Success -> {
                    prefs.setSession(result.userId, result.email)
                    _formState.value = AuthFormState()
                }
                is AuthResult.Failure -> {
                    _formState.value = AuthFormState(error = result.error.message())
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { prefs.clearSession() }
    }

    fun clearError() {
        _formState.update { it.copy(error = null) }
    }

    private fun validateLogin(email: String, password: String): String? = when {
        email.isBlank() || password.isBlank() -> "Completează emailul și parola."
        !isValidEmail(email) -> "Adresa de email nu este validă."
        else -> null
    }

    private fun validateRegister(email: String, password: String, confirmPassword: String): String? = when {
        email.isBlank() || password.isBlank() -> "Completează toate câmpurile."
        !isValidEmail(email) -> "Adresa de email nu este validă."
        password.length < 6 -> "Parola trebuie să aibă cel puțin 6 caractere."
        password != confirmPassword -> "Parolele nu coincid."
        else -> null
    }

    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
}

private fun AuthError.message(): String = when (this) {
    AuthError.EMAIL_TAKEN -> "Există deja un cont cu acest email."
    AuthError.INVALID_CREDENTIALS -> "Email sau parolă incorecte."
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val prefs: Prefs
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
