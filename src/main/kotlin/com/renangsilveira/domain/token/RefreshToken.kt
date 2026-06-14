package com.renangsilveira.domain.token

import java.time.LocalDateTime
import java.util.UUID

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val token: String,
    val expiresAt: LocalDateTime,
    val revoked: Boolean,
    val createdAt: LocalDateTime
)
