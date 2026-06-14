package com.renangsilveira.domain.token

import com.renangsilveira.domain.user.UserTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object RefreshTokenTable : Table("refresh_tokens") {
    val id        = uuid("id").autoGenerate()
    val userId    = uuid("user_id").references(UserTable.id)
    val token     = varchar("token", 512).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val revoked   = bool("revoked").default(false)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
