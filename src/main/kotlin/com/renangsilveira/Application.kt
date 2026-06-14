package com.renangsilveira

import com.renangsilveira.plugins.configureHTTP
import com.renangsilveira.plugins.configureRouting
import com.renangsilveira.plugins.configureSecurity
import com.renangsilveira.plugins.configureSerialization
import io.ktor.server.application.*

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
