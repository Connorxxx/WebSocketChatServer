package com.connor.websocketchatserver

import android.app.Application
import android.content.Context
import com.connor.websocketchatserver.di.appModule
import com.drake.brv.utils.BRV
import com.tencent.mmkv.MMKV
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        BRV.modelId = BR.m
        MMKV.initialize(this)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }
}