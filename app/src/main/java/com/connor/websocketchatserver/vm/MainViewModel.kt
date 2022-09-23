package com.connor.websocketchatserver.vm

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.connor.websocketchatserver.models.ChatMessage
import java.net.NetworkInterface

class MainViewModel(val handle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY = "keyName"
        private var openWebSocket = false
    }

    val openWebSocketLiveData = handle.getLiveData<String>(KEY)

    fun getOpenWebSocket() = openWebSocket

    fun setOpenWebSocket(open: Boolean) {
        openWebSocket = open
    }

    fun getMsg(userId: Int, msg: String): List<ChatMessage> {
        return listOf(ChatMessage(msg, userId))
    }

    fun getIpAddressInLocalNetwork(): String? {
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

    override fun onCleared() {
        Log.d("openWebSocket", "onCleared: ")
        super.onCleared()
    }
}