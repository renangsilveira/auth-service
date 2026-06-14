package com.renangsilveira.features.auth

import kotlinx.serialization.Serializable

object AuthRequest {
    @Serializable
    data class RegisterRequest(
        val email: String,
        val password: String
    )

    @Serializable
    data class LoginRequest(
        val email: String,
        val password: String
    )
}
