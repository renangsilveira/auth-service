package com.renangsilveira.domain.user

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UserTable : Table("users") {
    val id        = uuid("id").autoGenerate()
    val email     = varchar("email", 255).uniqueIndex()
    val password  = varchar("password", 255)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}
