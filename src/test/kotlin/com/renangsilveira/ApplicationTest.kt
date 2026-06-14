package com.renangsilveira

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `health endpoint returns 200`() = testApplication {
        application { module(connectDatabase = false) }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
