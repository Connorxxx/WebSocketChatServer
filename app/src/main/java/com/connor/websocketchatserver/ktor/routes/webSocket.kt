package com.connor.websocketchatserver.ktor.routes

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.webSocket() {
    route("/chat") {
        webSocket {
            send("You are connected!")
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
            }
        }
    }
}