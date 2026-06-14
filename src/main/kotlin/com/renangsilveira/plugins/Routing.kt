package com.renangsilveira.plugins

import com.renangsilveira.domain.token.RefreshTokenRepository
import com.renangsilveira.domain.user.UserRepository
import com.renangsilveira.features.auth.AuthRequest
import com.renangsilveira.features.auth.AuthService
import com.renangsilveira.features.auth.ErrorResponse
import com.renangsilveira.features.auth.TokenResponse
import com.renangsilveira.features.auth.UserResponse
import com.renangsilveira.infrastructure.security.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class RefreshRequest(val refreshToken: String)

fun Application.configureRouting(
    authService: AuthService,
    jwtService: JwtService,
    userRepository: UserRepository,
    refreshTokenRepository: RefreshTokenRepository
) {
    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }

        route("/api/v1/auth") {
            post("/register") {
                val request = call.receive<AuthRequest.RegisterRequest>()

                when (val result = authService.register(request.email, request.password)) {
                    is AuthService.AuthResult.Success -> call.respond(
                        HttpStatusCode.Created,
                        UserResponse(id = result.user.id.toString(), email = result.user.email)
                    )
                    is AuthService.AuthResult.EmailAlreadyExists -> call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("CONFLICT", "Email already registered")
                    )
                    is AuthService.AuthResult.Error -> call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", result.message)
                    )
                    else -> call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", "Unexpected error")
                    )
                }
            }

            post("/login") {
                val request = call.receive<AuthRequest.LoginRequest>()

                when (val result = authService.login(request.email, request.password)) {
                    is AuthService.AuthResult.Success -> {
                        val user         = result.user
                        val accessToken  = jwtService.generateAccessToken(user.id, user.email)
                        val refreshToken = jwtService.generateRefreshToken(user.id)
                        val expiresAt    = LocalDateTime.now()
                            .plusSeconds(jwtService.refreshTokenExpirationMs / 1000)

                        refreshTokenRepository.create(user.id, refreshToken, expiresAt)

                        call.respond(
                            HttpStatusCode.OK,
                            TokenResponse(
                                accessToken  = accessToken,
                                refreshToken = refreshToken,
                                expiresIn    = jwtService.accessTokenExpirationMs / 1000
                            )
                        )
                    }
                    is AuthService.AuthResult.InvalidCredentials -> call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("UNAUTHORIZED", "Invalid email or password")
                    )
                    is AuthService.AuthResult.Error -> call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", result.message)
                    )
                    else -> call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", "Unexpected error")
                    )
                }
            }

            post("/refresh") {
                val request = call.receive<RefreshRequest>()

                when (val result = authService.refresh(request.refreshToken)) {
                    is AuthService.AuthResult.TokenPair -> call.respond(
                        HttpStatusCode.OK,
                        TokenResponse(
                            accessToken  = result.accessToken,
                            refreshToken = result.refreshToken,
                            expiresIn    = jwtService.accessTokenExpirationMs / 1000
                        )
                    )
                    is AuthService.AuthResult.InvalidToken -> call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("UNAUTHORIZED", "Invalid or expired refresh token")
                    )
                    else -> call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", "Unexpected error")
                    )
                }
            }

            post("/logout") {
                val authHeader = call.request.headers["Authorization"]
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("UNAUTHORIZED", "Missing Authorization header")
                    )

                val token = authHeader.removePrefix("Bearer ").trim()

                when (authService.logout(token)) {
                    is AuthService.AuthResult.LoggedOut -> call.respond(HttpStatusCode.NoContent)
                    is AuthService.AuthResult.InvalidToken -> call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("UNAUTHORIZED", "Invalid or expired token")
                    )
                    else -> call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", "Unexpected error")
                    )
                }
            }

            authenticate("jwt-auth") {
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId    = UUID.fromString(principal.payload.subject)
                    val user      = userRepository.findById(userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_FOUND", "User not found")
                        )

                    call.respond(
                        HttpStatusCode.OK,
                        UserResponse(id = user.id.toString(), email = user.email)
                    )
                }
            }
        }
    }
}
