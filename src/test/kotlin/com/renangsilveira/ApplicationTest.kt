package com.renangsilveira

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `health endpoint returns 200`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.host" to "localhost",
                "database.port" to "5432",
                "database.name" to "auth_service",
                "database.user" to "auth_user",
                "database.password" to "auth_pass",
                "redis.host" to "localhost",
                "redis.port" to "6379",
                "jwt.secret" to "test-secret-must-be-at-least-32-chars!!",
                "jwt.issuer" to "auth-service",
                "jwt.audience" to "auth-service-users",
                "jwt.accessTokenExpirationMs" to "900000",
                "jwt.refreshTokenExpirationMs" to "604800000"
            )
        }
        application { module(connectDatabase = false) }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
