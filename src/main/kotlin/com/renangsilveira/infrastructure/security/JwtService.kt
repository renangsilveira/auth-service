package com.renangsilveira.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.ktor.server.application.*
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

class JwtService(application: Application) {

    private val secret: SecretKey
    private val issuer: String
    private val audience: String
    val accessTokenExpirationMs: Long
    val refreshTokenExpirationMs: Long

    init {
        val config = application.environment.config
        secret = Keys.hmacShaKeyFor(
            config.property("jwt.secret").getString().toByteArray()
        )
        issuer = config.property("jwt.issuer").getString()
        audience = config.property("jwt.audience").getString()
        accessTokenExpirationMs = config.property("jwt.accessTokenExpirationMs").getString().toLong()
        refreshTokenExpirationMs = config.property("jwt.refreshTokenExpirationMs").getString().toLong()
    }

    fun generateAccessToken(userId: UUID, email: String): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .audience().add(audience).and()
            .claim("email", email)
            .claim("type", "access")
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiration(Date(now.time + accessTokenExpirationMs))
            .signWith(secret)
            .compact()
    }

    fun generateRefreshToken(userId: UUID): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(issuer)
            .audience().add(audience).and()
            .claim("type", "refresh")
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiration(Date(now.time + refreshTokenExpirationMs))
            .signWith(secret)
            .compact()
    }

    fun validateToken(token: String): Claims? = runCatching {
        Jwts.parser()
            .verifyWith(secret)
            .build()
            .parseSignedClaims(token)
            .payload
    }.getOrNull()

    fun getUserIdFromToken(token: String): UUID? =
        validateToken(token)?.subject?.let { UUID.fromString(it) }
}
