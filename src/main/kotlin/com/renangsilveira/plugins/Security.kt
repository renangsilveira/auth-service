package com.renangsilveira.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.renangsilveira.features.auth.AuthService
import com.renangsilveira.features.auth.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity(authService: AuthService? = null) {
    val config = environment.config

    val secret   = config.property("jwt.secret").getString()
    val issuer   = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()

    install(Authentication) {
        jwt("jwt-auth") {
            realm = "auth-service"
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { credential ->
                val tokenType = credential.payload.getClaim("type").asString()
                if (tokenType != "access") return@validate null

                val token = request.headers["Authorization"]
                    ?.removePrefix("Bearer ")
                    ?.trim()
                    ?: return@validate null

                if (authService?.isTokenBlacklisted(token) == true) return@validate null

                JWTPrincipal(credential.payload)
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "Token is invalid or expired")
                )
            }
        }
    }
}
