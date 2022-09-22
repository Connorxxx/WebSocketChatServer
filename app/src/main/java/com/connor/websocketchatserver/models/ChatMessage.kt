package com.connor.websocketchatserver.models

data class ChatMessage(
    val msg: String,
    val name: String? = "Jon"
)
