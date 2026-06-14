package com.renangsilveira.features.auth

import com.renangsilveira.domain.security.PasswordHasher
import com.renangsilveira.domain.user.User
import com.renangsilveira.domain.user.UserRepository

class AuthService(
    private val userRepository: UserRepository
) {

    sealed class AuthResult {
        data class Success(val user: User) : AuthResult()
        data object EmailAlreadyExists : AuthResult()
        data object InvalidCredentials : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    fun register(email: String, password: String): AuthResult {
        if (!isValidEmail(email)) {
            return AuthResult.Error("Invalid email format")
        }
        if (password.length < 8) {
            return AuthResult.Error("Password must be at least 8 characters")
        }
        if (userRepository.existsByEmail(email)) {
            return AuthResult.EmailAlreadyExists
        }
        val hashed = PasswordHasher.hash(password)
        val user = userRepository.create(email, hashed)
        return AuthResult.Success(user)
    }

    fun login(email: String, password: String): AuthResult {
        val user = userRepository.findByEmail(email)
            ?: return AuthResult.InvalidCredentials
        if (!PasswordHasher.verify(password, user.password)) {
            return AuthResult.InvalidCredentials
        }
        return AuthResult.Success(user)
    }

    private fun isValidEmail(email: String): Boolean =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
}
