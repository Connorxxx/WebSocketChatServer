package com.connor.websocketchatserver.ktor

import com.connor.websocketchatserver.ktor.routes.webSocket
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.time.Duration

fun Application.configureRouting() {
    install(Compression)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket()
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}