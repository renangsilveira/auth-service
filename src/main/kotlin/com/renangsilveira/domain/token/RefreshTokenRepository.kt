package com.renangsilveira.domain.token

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class RefreshTokenRepository {

    fun create(userId: UUID, token: String, expiresAt: LocalDateTime): RefreshToken = transaction {
        val now = LocalDateTime.now()
        val id = RefreshTokenTable.insert {
            it[RefreshTokenTable.userId]    = userId
            it[RefreshTokenTable.token]     = token
            it[RefreshTokenTable.expiresAt] = expiresAt
            it[RefreshTokenTable.revoked]   = false
            it[RefreshTokenTable.createdAt] = now
        }[RefreshTokenTable.id]

        RefreshToken(
            id        = id,
            userId    = userId,
            token     = token,
            expiresAt = expiresAt,
            revoked   = false,
            createdAt = now
        )
    }

    fun findByToken(token: String): RefreshToken? = transaction {
        RefreshTokenTable
            .selectAll()
            .where { RefreshTokenTable.token eq token }
            .map { it.toRefreshToken() }
            .singleOrNull()
    }

    fun revokeByToken(token: String): Boolean = transaction {
        RefreshTokenTable.update(
            where = { RefreshTokenTable.token eq token }
        ) {
            it[revoked] = true
        } > 0
    }

    fun revokeAllByUserId(userId: UUID): Int = transaction {
        RefreshTokenTable.update(
            where = { RefreshTokenTable.userId eq userId }
        ) {
            it[revoked] = true
        }
    }

    private fun ResultRow.toRefreshToken() = RefreshToken(
        id        = this[RefreshTokenTable.id],
        userId    = this[RefreshTokenTable.userId],
        token     = this[RefreshTokenTable.token],
        expiresAt = this[RefreshTokenTable.expiresAt],
        revoked   = this[RefreshTokenTable.revoked],
        createdAt = this[RefreshTokenTable.createdAt]
    )
}
