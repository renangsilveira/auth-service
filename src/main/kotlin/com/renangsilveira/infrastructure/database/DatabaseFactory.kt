package com.renangsilveira.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    fun init(application: Application) {
        val config = application.environment.config

        val host = config.property("database.host").getString()
        val port = config.property("database.port").getString()
        val name = config.property("database.name").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$host:$port/$name"
            username = user
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
            idleTimeout = 300_000
            connectionTimeout = 20_000
            validationTimeout = 5_000
        }

        Database.connect(HikariDataSource(hikariConfig))
        application.log.info("Database connection pool initialized")
    }
}
