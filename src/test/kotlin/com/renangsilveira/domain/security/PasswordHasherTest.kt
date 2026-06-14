package com.renangsilveira.domain.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    @Test
    fun `hash returns bcrypt string`() {
        val hash = PasswordHasher.hash("secret123")
        assertTrue(hash.startsWith("\$2"))
    }

    @Test
    fun `verify returns true for correct password`() {
        val hash = PasswordHasher.hash("secret123")
        assertTrue(PasswordHasher.verify("secret123", hash))
    }

    @Test
    fun `verify returns false for wrong password`() {
        val hash = PasswordHasher.hash("secret123")
        assertFalse(PasswordHasher.verify("wrong", hash))
    }

    @Test
    fun `same password produces different hashes`() {
        val hash1 = PasswordHasher.hash("secret123")
        val hash2 = PasswordHasher.hash("secret123")
        assertNotEquals(hash1, hash2)
    }
}
