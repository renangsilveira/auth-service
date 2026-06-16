package com.renangsilveira.integration

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthRegistrationTest : AbstractIntegrationTest() {

    private fun uniqueEmail(prefix: String) = "$prefix+${UUID.randomUUID()}@test.com"

    @Test
    fun `register returns 201 with user id and email`() = integrationTest {
        val email = uniqueEmail("user1")
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertNotNull(body["id"])
        assertEquals(email, body["email"]?.jsonPrimitive?.content)
    }

    @Test
    fun `register returns 409 when email already exists`() = integrationTest {
        val email = uniqueEmail("duplicate")
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"password123"}""")
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"$email","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("CONFLICT", body["error"]?.jsonPrimitive?.content)
    }

    @Test
    fun `register returns 400 for invalid email`() = integrationTest {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"not-an-email","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("VALIDATION_ERROR", body["error"]?.jsonPrimitive?.content)
    }

    @Test
    fun `register returns 400 for short password`() = integrationTest {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"${uniqueEmail("user2")}","password":"short"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("VALIDATION_ERROR", body["error"]?.jsonPrimitive?.content)
    }
}
