package com.renangsilveira

import com.renangsilveira.domain.token.RefreshTokenRepository
import com.renangsilveira.domain.user.UserRepository
import com.renangsilveira.features.auth.AuthService
import com.renangsilveira.infrastructure.database.DatabaseFactory
import com.renangsilveira.infrastructure.redis.RedisClient
import com.renangsilveira.infrastructure.security.JwtService
import com.renangsilveira.plugins.configureHTTP
import com.renangsilveira.plugins.configureRouting
import com.renangsilveira.plugins.configureSecurity
import com.renangsilveira.plugins.configureSerialization
import com.renangsilveira.plugins.configureStatusPages
import io.ktor.server.application.*

fun Application.module() {
    module(connectDatabase = true, connectRedis = true)
}

fun Application.module(connectDatabase: Boolean = true, connectRedis: Boolean = true) {
    if (connectDatabase) DatabaseFactory.init(this)
    if (connectRedis) RedisClient.init(this)

    val userRepository         = UserRepository()
    val refreshTokenRepository = RefreshTokenRepository()
    val jwtService             = JwtService(this)
    val authService            = AuthService(userRepository, refreshTokenRepository, jwtService)

    configureHTTP()
    configureSerialization()
    configureStatusPages()
    configureSecurity(authService)
    configureRouting(authService, jwtService, userRepository, refreshTokenRepository)
}
