package com.connor.websocketchatserver.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.connor.websocketchatserver.models.ChatMessage
import com.drake.serialize.serialize.serialLazy
import java.net.NetworkInterface

class MainViewModel(val handle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY = "keyName"
        private var openWebSocket = false
    }

    val liveData = MutableLiveData<Boolean>()

    val contentData = handle.getStateFlow(KEY, "")

    var content: String? by serialLazy()

    fun setQuery(query: String) {
        handle[KEY] = query
    }

    fun getOpenWebSocket() = openWebSocket

    fun setOpenWebSocket(open: Boolean) {
        openWebSocket = open
        liveData.value = open
    }

    fun getMsg(userId: Int, msg: String): List<ChatMessage> {
        return listOf(ChatMessage(msg, userId))
    }

    fun getNet(b: Boolean) =
        if (b) "ws://${getIpAddressInLocalNetwork()}:19980/chat" else "WebSocket server closed"

    private fun getIpAddressInLocalNetwork(): String? {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces().iterator().asSequence()
        val localAddresses = networkInterfaces.flatMap {
            it.inetAddresses.asSequence()
                .filter { inetAddress ->
                    inetAddress.isSiteLocalAddress && !inetAddress.hostAddress!!.contains(":") &&
                            inetAddress.hostAddress != "127.0.0.1"
                }
                .map { inetAddress -> inetAddress.hostAddress }
        }
        return localAddresses.firstOrNull()
    }
}