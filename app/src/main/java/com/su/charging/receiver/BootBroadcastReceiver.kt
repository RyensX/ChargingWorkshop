package com.su.charging.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.su.charging.R
import com.su.charging.ChargingService
import com.su.charging.util.PhoneUtils

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action.also { result ->
            if (result == Intent.ACTION_BOOT_COMPLETED) {
                context?.apply {
                    val sp = PhoneUtils.getDefaultSharedPreferences(context)
                    if (sp.getBoolean(getString(R.string.charge_boot_start), false))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            startForegroundService(Intent(this, ChargingService::class.java))
                        else
                            startService(Intent(this, ChargingService::class.java))
                }
            }
        }
    }
}