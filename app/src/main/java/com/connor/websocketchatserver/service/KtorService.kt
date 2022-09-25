package com.connor.websocketchatserver.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.connor.websocketchatserver.MainActivity
import com.connor.websocketchatserver.R
import com.drake.channel.sendTag
import io.ktor.server.cio.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class KtorService : Service() {

    private val configServer: CIOApplicationEngine by inject()
    private val ioDispatcher: CoroutineDispatcher by inject()

    private val job = Job()
    private val scope = CoroutineScope(job)


    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ktor_server", "Ktor Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, 0)
        val notification = NotificationCompat.Builder(this, "ktor_server")
            .setContentTitle("Ktor server is running")
            .setContentText("You could disable it notification")
            .setSmallIcon(R.drawable.outline_rss_feed_24)
            .setContentIntent(pi)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch(ioDispatcher) {
            configServer.start(wait = true)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        scope.launch {
            kotlin.runCatching {
                configServer.stop(2_000, 3_000)
                sendTag("serverStop")
                job.cancelAndJoin()
            }.onFailure {
                Log.d("onFailure", "onDestroy: ${it.localizedMessage}")
            }
        }
        super.onDestroy()
    }
}