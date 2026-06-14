package com.renangsilveira.domain.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {

    private const val COST = 12

    fun hash(plainText: String): String =
        BCrypt.withDefaults().hashToString(COST, plainText.toCharArray())

    fun verify(plainText: String, hashed: String): Boolean =
        BCrypt.verifyer().verify(plainText.toCharArray(), hashed).verified
}
