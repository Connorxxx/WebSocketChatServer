package com.connor.websocketchatserver

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.connor.websocketchatserver.databinding.ActivityMainBinding
import com.connor.websocketchatserver.models.ChatMessage
import com.connor.websocketchatserver.service.KtorService
import com.connor.websocketchatserver.tools.showSnackBar
import com.connor.websocketchatserver.vm.MainViewModel
import com.drake.brv.utils.addModels
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.serialize.serialize.serial
import com.drake.serialize.serialize.serialLazy
import com.drake.softinput.hideSoftInput
import com.drake.softinput.setWindowSoftInput
import org.koin.androidx.viewmodel.ext.android.getStateViewModel
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by stateViewModel()

    private var content: String? by serialLazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.materialToolbar)
        binding.materialToolbar.subtitle = "${viewModel.getIpAddressInLocalNetwork()}:19980"
        if (!viewModel.getOpenWebSocket()) {
            Log.d("openWebSocket", "onCreate: ${viewModel.getOpenWebSocket()}")
            startWebSocketService()
            viewModel.setOpenWebSocket(true)
        }
        setWindowSoftInput(float = binding.llInput, setPadding = true)
        binding.rvChat.setup {
            addType<ChatMessage> {
                if (isMine()) R.layout.item_msg_right else R.layout.item_msg_left
            }
        }
        receiveEvent<String>("receivedText") {
            binding.rvChat.addModels(viewModel.getMsg(2, it))
            binding.rvChat.scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
        }
        onClick()
        viewModel.openWebSocketLiveData.value?.let {
            binding.etMsg.setText(it)
        }
        content?.let {
            binding.etMsg.setText(it)
        }
            binding.etMsg.setText(content)

        Log.d("openWebSocket", "onCreate: con ${content}")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.start -> {
                if (!viewModel.getOpenWebSocket()) {
                    startWebSocketService()
                    viewModel.setOpenWebSocket(true)
                }
            }
            R.id.stop -> {
                if (viewModel.getOpenWebSocket()) {
                    val stopService = Intent(this, KtorService::class.java)
                    stopService(stopService)
                    viewModel.setOpenWebSocket(false)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startWebSocketService() {
        val intent = Intent(this, KtorService::class.java)
        startService(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onClick() {
        binding.etMsg.addTextChangedListener {
            content = it.toString()
            viewModel.openWebSocketLiveData.value = it.toString()
            if (it.isNullOrEmpty()) binding.btnSend.visibility = View.GONE
            else binding.btnSend.visibility = View.VISIBLE
        }
        binding.btnSend.setOnClickListener {
            if (viewModel.getOpenWebSocket()) {
                content = null
                val msg = binding.etMsg.text.toString()
                    sendEvent(msg, "sendText")
                binding.rvChat.apply {
                    addModels(listOf(ChatMessage(msg, 1)))
                    scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
                }
                binding.etMsg.setText("")
            } else it.showSnackBar("Please open server first")
        }
        binding.rvChat.setOnTouchListener { view, _ ->
            view.clearFocus()
            hideSoftInput()
            false
        }
    }
}