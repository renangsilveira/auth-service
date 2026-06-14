package com.renangsilveira.features.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)
