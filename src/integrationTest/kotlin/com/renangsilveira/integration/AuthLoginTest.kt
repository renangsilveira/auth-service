package com.renangsilveira.integration

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthLoginTest : AbstractIntegrationTest() {

    private suspend fun io.ktor.server.testing.ApplicationTestBuilder.registerUser(
        email: String,
        password: String
    ) {
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"$password"}""")
        }
    }

    @Test
    fun `login returns 200 with token pair`() = integrationTest {
        registerUser("login1@test.com", "password123")

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"login1@test.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertNotNull(body["accessToken"])
        assertNotNull(body["refreshToken"])
        assertEquals("Bearer", body["tokenType"]?.jsonPrimitive?.content)
        assertNotNull(body["expiresIn"])
    }

    @Test
    fun `login returns 401 for wrong password`() = integrationTest {
        registerUser("login2@test.com", "password123")

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"login2@test.com","password":"wrongpassword"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("UNAUTHORIZED", body["error"]?.jsonPrimitive?.content)
    }

    @Test
    fun `login returns 401 for non-existent user`() = integrationTest {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"ghost@test.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("UNAUTHORIZED", body["error"]?.jsonPrimitive?.content)
    }

    @Test
    fun `login returns valid JWT that grants access to protected route`() = integrationTest {
        registerUser("login3@test.com", "password123")

        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"login3@test.com","password":"password123"}""")
        }

        val loginBody = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject
        val accessToken = loginBody["accessToken"]?.jsonPrimitive?.content!!

        val meResponse = client.get("/api/v1/auth/me") {
            bearerAuth(accessToken)
        }

        assertEquals(HttpStatusCode.OK, meResponse.status)
        val meBody = Json.parseToJsonElement(meResponse.bodyAsText()).jsonObject
        assertEquals("login3@test.com", meBody["email"]?.jsonPrimitive?.content)
    }
}
