package com.renangsilveira.infrastructure.redis

import io.lettuce.core.RedisClient as LettuceRedisClient
import io.lettuce.core.api.sync.RedisCommands
import io.ktor.server.application.*

object RedisClient {

    private lateinit var commands: RedisCommands<String, String>

    fun init(application: Application) {
        val config = application.environment.config
        val host = config.property("redis.host").getString()
        val port = config.property("redis.port").getString()

        val client = LettuceRedisClient.create("redis://$host:$port")
        val connection = client.connect()
        commands = connection.sync()
        application.log.info("Redis connection initialized")
    }

    fun set(key: String, value: String, ttlSeconds: Long) {
        commands.setex(key, ttlSeconds, value)
    }

    fun get(key: String): String? = commands.get(key)

    fun exists(key: String): Boolean = (commands.exists(key) ?: 0L) > 0

    fun delete(key: String) {
        commands.del(key)
    }

    fun increment(key: String): Long = commands.incr(key)
}
