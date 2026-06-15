package com.renangsilveira.integration

import com.renangsilveira.module
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

abstract class AbstractIntegrationTest {

    companion object {
        private val calimaSocket = "${System.getProperty("user.home")}/.colima/default/docker.sock"

        init {
            System.setProperty("DOCKER_HOST", "unix://$calimaSocket")
            System.setProperty("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", calimaSocket)
        }

        private val postgres: PostgreSQLContainer<*> by lazy {
            PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine")).apply {
                withDatabaseName("auth_service_test")
                withUsername("test_user")
                withPassword("test_pass")
                start()
            }
        }

        private val redis: GenericContainer<*> by lazy {
            GenericContainer(DockerImageName.parse("redis:7-alpine")).apply {
                withExposedPorts(6379)
                waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
                start()
            }
        }

        val testConfig: MapApplicationConfig by lazy {
            MapApplicationConfig(
                "database.host" to postgres.host,
                "database.port" to postgres.firstMappedPort.toString(),
                "database.name" to postgres.databaseName,
                "database.user" to postgres.username,
                "database.password" to postgres.password,
                "redis.host" to redis.host,
                "redis.port" to redis.firstMappedPort.toString(),
                "jwt.secret" to "integration-test-secret-must-be-long-enough!!",
                "jwt.issuer" to "auth-service",
                "jwt.audience" to "auth-service-users",
                "jwt.accessTokenExpirationMs" to "900000",
                "jwt.refreshTokenExpirationMs" to "604800000"
            )
        }
    }

    fun integrationTest(block: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            environment { config = testConfig }
            application { module(connectDatabase = true, connectRedis = true) }
            block()
        }
    }
}
