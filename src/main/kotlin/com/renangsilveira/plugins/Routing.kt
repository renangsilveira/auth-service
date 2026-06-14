package com.renangsilveira.plugins

import com.renangsilveira.domain.user.UserRepository
import com.renangsilveira.features.auth.AuthRequest
import com.renangsilveira.features.auth.AuthService
import com.renangsilveira.features.auth.ErrorResponse
import com.renangsilveira.features.auth.UserResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val authService = AuthService(userRepository)

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
                        UserResponse(
                            id    = result.user.id.toString(),
                            email = result.user.email
                        )
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
        }
    }
}
