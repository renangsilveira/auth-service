package com.renangsilveira.plugins

import com.renangsilveira.features.auth.ErrorResponse
import com.renangsilveira.infrastructure.redis.RedisClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private const val RATE_LIMIT_REQUESTS = 10L
private const val RATE_LIMIT_WINDOW_SECONDS = 60L

val RateLimitPlugin = createRouteScopedPlugin("RateLimitPlugin") {
    onCall { call ->
        val rateLimitEnabled = call.application.environment.config
            .propertyOrNull("rateLimit.enabled")?.getString()?.toBoolean() ?: true

        if (!rateLimitEnabled) return@onCall

        val ip = call.request.origin.remoteHost
        val key = "rate_limit:$ip"

        val current = RedisClient.get(key)?.toLongOrNull() ?: 0L

        if (current >= RATE_LIMIT_REQUESTS) {
            call.response.headers.append("Retry-After", RATE_LIMIT_WINDOW_SECONDS.toString())
            call.respond(
                HttpStatusCode.TooManyRequests,
                ErrorResponse(
                    "TOO_MANY_REQUESTS",
                    "Rate limit exceeded. Try again in $RATE_LIMIT_WINDOW_SECONDS seconds."
                )
            )
            return@onCall
        }

        if (current == 0L) {
            RedisClient.set(key, "1", RATE_LIMIT_WINDOW_SECONDS)
        } else {
            RedisClient.increment(key)
        }
    }
}

fun Route.withRateLimit(block: Route.() -> Unit): Route {
    return createChild(object : RouteSelector() {
        override suspend fun evaluate(
            context: RoutingResolveContext,
            segmentIndex: Int
        ) = RouteSelectorEvaluation.Transparent
    }).apply {
        install(RateLimitPlugin)
        block()
    }
}
