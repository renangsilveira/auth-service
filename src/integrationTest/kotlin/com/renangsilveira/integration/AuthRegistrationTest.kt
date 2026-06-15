package com.renangsilveira.integration

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthRegistrationTest : AbstractIntegrationTest() {

    @Test
    fun `register returns 201 with user id and email`() = integrationTest {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"user1@test.com","password":"password123"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertNotNull(body["id"])
        assertEquals("user1@test.com", body["email"]?.jsonPrimitive?.content)
    }

    @Test
    fun `register returns 409 when email already exists`() = integrationTest {
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"duplicate@test.com","password":"password123"}""")
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"duplicate@test.com","password":"password123"}""")
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
            setBody("""{"email":"user2@test.com","password":"short"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("VALIDATION_ERROR", body["error"]?.jsonPrimitive?.content)
    }
}
