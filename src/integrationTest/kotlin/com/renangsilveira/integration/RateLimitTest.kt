package com.renangsilveira.integration

import com.renangsilveira.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RateLimitTest : AbstractIntegrationTest() {

    private fun rateLimitConfig() = MapApplicationConfig(
        "database.host" to testConfig.property("database.host").getString(),
        "database.port" to testConfig.property("database.port").getString(),
        "database.name" to testConfig.property("database.name").getString(),
        "database.user" to testConfig.property("database.user").getString(),
        "database.password" to testConfig.property("database.password").getString(),
        "redis.host" to testConfig.property("redis.host").getString(),
        "redis.port" to testConfig.property("redis.port").getString(),
        "jwt.secret" to "integration-test-secret-must-be-long-enough!!",
        "jwt.issuer" to "auth-service",
        "jwt.audience" to "auth-service-users",
        "jwt.accessTokenExpirationMs" to "900000",
        "jwt.refreshTokenExpirationMs" to "604800000",
        "rateLimit.enabled" to "true"
    )

    @Test
    fun `rate limit returns 429 after exceeding threshold`() = testApplication {
        environment { config = rateLimitConfig() }
        application { module(connectDatabase = true, connectRedis = true) }

        val email = "ratelimit+${UUID.randomUUID()}@test.com"

        repeat(10) {
            client.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"ratelimit-flood+${UUID.randomUUID()}@test.com","password":"password123"}""")
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.TooManyRequests, response.status)
        assertNotNull(response.headers["Retry-After"])
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("TOO_MANY_REQUESTS", body["error"]?.jsonPrimitive?.content)
    }
}
