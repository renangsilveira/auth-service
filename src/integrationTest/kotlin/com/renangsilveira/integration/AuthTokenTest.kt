package com.renangsilveira.integration

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals

class AuthTokenTest : AbstractIntegrationTest() {

    private suspend fun io.ktor.server.testing.ApplicationTestBuilder.registerAndLogin(
        email: String,
        password: String = "password123"
    ): JsonObject {
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"$password"}""")
        }
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"$password"}""")
        }
        return Json.parseToJsonElement(response.bodyAsText()).jsonObject
    }

    @Test
    fun `refresh returns new token pair`() = integrationTest {
        val tokens = registerAndLogin("refresh1@test.com")
        val refreshToken = tokens["refreshToken"]?.jsonPrimitive?.content!!

        val response = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":"$refreshToken"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertNotNull(body["accessToken"])
        assertNotNull(body["refreshToken"])
        assertNotEquals(refreshToken, body["refreshToken"]?.jsonPrimitive?.content)
    }

    @Test
    fun `refresh invalidates old refresh token`() = integrationTest {
        val tokens = registerAndLogin("refresh2@test.com")
        val refreshToken = tokens["refreshToken"]?.jsonPrimitive?.content!!

        client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":"$refreshToken"}""")
        }

        val response = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":"$refreshToken"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `refresh returns 401 for invalid token`() = integrationTest {
        val response = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("""{"refreshToken":"invalid-token"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `logout invalidates access token`() = integrationTest {
        val tokens = registerAndLogin("logout1@test.com")
        val accessToken = tokens["accessToken"]?.jsonPrimitive?.content!!

        val logoutResponse = client.post("/api/v1/auth/logout") {
            bearerAuth(accessToken)
        }
        assertEquals(HttpStatusCode.NoContent, logoutResponse.status)

        val meResponse = client.get("/api/v1/auth/me") {
            bearerAuth(accessToken)
        }
        assertEquals(HttpStatusCode.Unauthorized, meResponse.status)
    }

    @Test
    fun `logout returns 401 without token`() = integrationTest {
        val response = client.post("/api/v1/auth/logout")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
