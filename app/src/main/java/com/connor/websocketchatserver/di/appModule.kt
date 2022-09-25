package com.connor.websocketchatserver.di

import com.connor.websocketchatserver.ktor.configureRouting
import com.connor.websocketchatserver.ktor.configureSerialization
import com.connor.websocketchatserver.vm.MainViewModel
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel { MainViewModel(get()) }

    factory { configServer() }

    single { ioDispatcher() }
}

fun configServer() = embeddedServer(CIO, port = 19980, host = "0.0.0.0", configure = {
        connectionGroupSize = 2
        workerGroupSize = 5
        callGroupSize = 10
        connectionIdleTimeoutSeconds = 45
    }) {
        configureRouting()
        configureSerialization()
    }

fun ioDispatcher() = Dispatchers.IO