package com.mindandmotion.app.data.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

enum class AuthError {
    EMAIL_TAKEN,
    INVALID_CREDENTIALS
}

sealed interface AuthResult {
    data class Success(val userId: Long, val email: String) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

class AuthRepository(private val userDao: UserDao) {

    suspend fun register(email: String, password: String): AuthResult {
        val normalized = email.trim().lowercase()
        if (userDao.countByEmail(normalized) > 0) {
            return AuthResult.Failure(AuthError.EMAIL_TAKEN)
        }
        val salt = newSalt()
        val user = UserEntity(
            email = normalized,
            passwordHash = hash(password, salt),
            salt = salt
        )
        val id = userDao.insert(user)
        return AuthResult.Success(id, normalized)
    }

    suspend fun login(email: String, password: String): AuthResult {
        val normalized = email.trim().lowercase()
        val user = userDao.findByEmail(normalized)
            ?: return AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
        return if (hash(password, user.salt) == user.passwordHash) {
            AuthResult.Success(user.id, user.email)
        } else {
            AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
        }
    }

    private fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + password).toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
