package com.connor.websocketchatserver

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.connor.websocketchatserver.databinding.ActivityMainBinding
import com.connor.websocketchatserver.models.ChatMessage
import com.connor.websocketchatserver.service.KtorService
import com.connor.websocketchatserver.tools.showSnackBar
import com.connor.websocketchatserver.vm.MainViewModel
import com.drake.brv.utils.addModels
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.channel.receiveTag
import com.drake.channel.sendEvent
import com.drake.softinput.hideSoftInput
import com.drake.softinput.setWindowSoftInput
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by stateViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.materialToolbar)
        binding.materialToolbar.subtitle = viewModel.getNet(viewModel.getOpenWebSocket())
        viewModel.liveData.observe(this) {
            binding.materialToolbar.subtitle = viewModel.getNet(it)
        }
        setWindowSoftInput(float = binding.llInput, setPadding = true)
        if (!viewModel.getOpenWebSocket()) {
            startService<KtorService> {}
            viewModel.setOpenWebSocket(true)
        }
        binding.rvChat.setup {
            addType<ChatMessage> {
                if (isMine()) R.layout.item_msg_right else R.layout.item_msg_left
            }
        }
        receiveEvent<String>("receivedText") {
            binding.rvChat.addModels(viewModel.getMsg(2, it))
            binding.rvChat.scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
        }
        receiveTag("serverStop") {
            viewModel.setOpenWebSocket(false)
        }
        initEditText()
        onClick()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.start -> {
                if (!viewModel.getOpenWebSocket()) {
                    startService<KtorService> {}
                    viewModel.setOpenWebSocket(true)
                }
            }
            R.id.stop -> {
                if (viewModel.getOpenWebSocket()) {
                    stopService<KtorService> {}
                    binding.materialToolbar.subtitle = "Closing..."
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initEditText() {
        viewModel.contentData.value.let {
            binding.etMsg.setText(it)
        }
        viewModel.content?.let {
            binding.etMsg.setText(it)
            binding.btnSend.visibility = View.VISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onClick() {
        binding.etMsg.addTextChangedListener {
            viewModel.content = it.toString()
            //viewModel.setQuery(it.toString())
            if (it.isNullOrEmpty()) binding.btnSend.visibility = View.GONE
            else binding.btnSend.visibility = View.VISIBLE
        }
        binding.btnSend.setOnClickListener {
            if (viewModel.getOpenWebSocket()) {
                viewModel.content = null
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

    private inline fun <reified T> startService(block: Intent.() -> Unit) {
        val intent = Intent(this, T::class.java)
        intent.block()
        startService(intent)
    }

    private inline fun <reified T> stopService(block: Intent.() -> Unit) {
        val stopService = Intent(this, T::class.java)
        stopService.block()
        stopService(stopService)
    }
}