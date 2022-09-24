package com.connor.websocketchatserver.ktor.routes

import android.util.Log
import com.drake.channel.receiveEventHandler
import com.drake.channel.sendEvent
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.job

fun Route.webSocket() {
    route("/chat") {
        webSocket {
            send("You are connected!")
            kotlin.runCatching {
                receiveEventHandler<String>("sendText") {
                    outgoing.send(Frame.Text(it))
                }
            }.onFailure {
                Log.e("onFailure", "send: ", )
            }
            kotlin.runCatching {
                incoming.receiveAsFlow().filterIsInstance<Frame.Text>().collect {
                    val receivedText = it.readText()
                    sendEvent(receivedText, "receivedText")
                }
            }.onFailure {
                Log.e("onFailure", "receivedText: ${it.localizedMessage}", )
            }
        }
    }
}