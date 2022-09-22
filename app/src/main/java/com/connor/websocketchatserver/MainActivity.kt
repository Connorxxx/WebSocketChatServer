package com.connor.websocketchatserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.connor.websocketchatserver.databinding.ActivityMainBinding
import com.connor.websocketchatserver.models.ChatMessage
import com.connor.websocketchatserver.service.KtorService
import com.connor.websocketchatserver.vm.MainViewModel
import com.drake.brv.utils.addModels
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.materialToolbar)
        binding.materialToolbar.subtitle = "${viewModel.getIpAddressInLocalNetwork()}:19980"
        val intent = Intent(this, KtorService::class.java)
        startService(intent)
        binding.rvChat.setup {
            addType<ChatMessage> {
                if (isMine()) R.layout.item_msg_right else R.layout.item_msg_left
            }
        }
        receiveEvent<String>("receivedText") {
            binding.rvChat.addModels(viewModel.getMsg(2, it))
            binding.rvChat.scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
        }
        binding.btnSend.setOnClickListener {
            Log.d("webSocket", "send: ")
            val msg = binding.etMsg.text ?: ""
            if (msg.isNotEmpty())
                sendEvent(msg.toString(), "sendText")
            binding.rvChat.apply {
                addModels(listOf(ChatMessage(msg.toString(), 1)))
                scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
            }
            binding.etMsg.setText("")
        }
    }
}