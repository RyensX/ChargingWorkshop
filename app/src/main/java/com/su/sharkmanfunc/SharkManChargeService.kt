package com.su.sharkmanfunc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat


class SharkManChargeService : Service() {

    companion object {
        const val CHANNEL_NAME = "鲨鲨酱充电守护服务"
        const val CHANNEL_ID = "CHANNEL_ID"
        var isOpen = false
    }

    private lateinit var bbcr: BatteryBroadCastReceiver

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nor = NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
            setContentTitle(CHANNEL_NAME)
        }.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        startForeground(110, nor)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        configBattery()
        isOpen = true
        PhoneUtils.checkIsOnFullScreen(this)
    }

    private fun configBattery() {
        bbcr = BatteryBroadCastReceiver()
        bbcr.register(this)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        unregisterReceiver(bbcr)
        isOpen = false

        ChargeAudioManager.INS.release()

        SoundPreference.releaseMedia()

        PhoneUtils.closeFullCheckView(this)
        super.onDestroy()
    }
}