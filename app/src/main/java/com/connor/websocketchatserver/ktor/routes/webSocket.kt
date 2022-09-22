package com.connor.websocketchatserver.ktor.routes

import com.drake.channel.receiveEventHandler
import com.drake.channel.sendEvent
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow

fun Route.webSocket() {
    route("/chat") {
        webSocket {
            send("You are connected!")
            receiveEventHandler<String>("sendText") {
                outgoing.send(Frame.Text(it))
            }
            incoming.receiveAsFlow().filterIsInstance<Frame.Text>().collect {
                val receivedText = it.readText()
                sendEvent(receivedText, "receivedText")
            }
        }
    }
}