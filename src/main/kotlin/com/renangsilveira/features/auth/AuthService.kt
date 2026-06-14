package com.renangsilveira.features.auth

import com.renangsilveira.domain.security.PasswordHasher
import com.renangsilveira.domain.token.RefreshTokenRepository
import com.renangsilveira.domain.user.User
import com.renangsilveira.domain.user.UserRepository
import com.renangsilveira.infrastructure.security.JwtService
import java.time.LocalDateTime

class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService
) {

    sealed class AuthResult {
        data class Success(val user: User) : AuthResult()
        data class TokenPair(val accessToken: String, val refreshToken: String) : AuthResult()
        data object EmailAlreadyExists : AuthResult()
        data object InvalidCredentials : AuthResult()
        data object InvalidToken : AuthResult()
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

    fun refresh(refreshToken: String): AuthResult {
        val stored = refreshTokenRepository.findByToken(refreshToken)
            ?: return AuthResult.InvalidToken

        if (stored.revoked) return AuthResult.InvalidToken
        if (stored.expiresAt.isBefore(LocalDateTime.now())) return AuthResult.InvalidToken

        val user = userRepository.findById(stored.userId)
            ?: return AuthResult.InvalidToken

        refreshTokenRepository.revokeByToken(refreshToken)

        val newAccessToken  = jwtService.generateAccessToken(user.id, user.email)
        val newRefreshToken = jwtService.generateRefreshToken(user.id)
        val expiresAt = LocalDateTime.now()
            .plusSeconds(jwtService.refreshTokenExpirationMs / 1000)

        refreshTokenRepository.create(user.id, newRefreshToken, expiresAt)

        return AuthResult.TokenPair(newAccessToken, newRefreshToken)
    }

    private fun isValidEmail(email: String): Boolean =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
}
