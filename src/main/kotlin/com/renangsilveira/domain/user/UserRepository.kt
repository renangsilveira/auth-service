package com.renangsilveira.domain.user

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class UserRepository {

    fun findByEmail(email: String): User? = transaction {
        UserTable
            .selectAll()
            .where { UserTable.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun findById(id: UUID): User? = transaction {
        UserTable
            .selectAll()
            .where { UserTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun create(email: String, hashedPassword: String): User = transaction {
        val now = LocalDateTime.now()
        val id = UserTable.insert {
            it[UserTable.email]    = email
            it[UserTable.password] = hashedPassword
            it[createdAt]          = now
            it[updatedAt]          = now
        }[UserTable.id]

        User(
            id        = id,
            email     = email,
            password  = hashedPassword,
            createdAt = now,
            updatedAt = now
        )
    }

    fun existsByEmail(email: String): Boolean = transaction {
        UserTable
            .selectAll()
            .where { UserTable.email eq email }
            .count() > 0
    }

    private fun ResultRow.toUser() = User(
        id        = this[UserTable.id],
        email     = this[UserTable.email],
        password  = this[UserTable.password],
        createdAt = this[UserTable.createdAt],
        updatedAt = this[UserTable.updatedAt]
    )
}
