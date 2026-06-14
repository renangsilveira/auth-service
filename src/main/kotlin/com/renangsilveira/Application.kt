package com.renangsilveira

import com.renangsilveira.infrastructure.database.DatabaseFactory
import com.renangsilveira.plugins.configureHTTP
import com.renangsilveira.plugins.configureRouting
import com.renangsilveira.plugins.configureSecurity
import com.renangsilveira.plugins.configureSerialization
import com.renangsilveira.plugins.configureStatusPages
import io.ktor.server.application.*

fun Application.module() {
    module(connectDatabase = true)
}

fun Application.module(connectDatabase: Boolean = true) {
    if (connectDatabase) {
        DatabaseFactory.init(this)
    }
    configureHTTP()
    configureSerialization()
    configureStatusPages()
    configureSecurity()
    configureRouting()
}
