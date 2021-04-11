package com.su.sharkmanfunc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action.also { result ->
            if (result == Intent.ACTION_BOOT_COMPLETED) {
                context?.apply {
                    val sp = PhoneUtils.getDefaultSharedPreferences(context)
                    if (sp.getBoolean(getString(R.string.charge_boot_start), false))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            startForegroundService(Intent(this, SharkManChargeService::class.java))
                        else
                            startService(Intent(this, SharkManChargeService::class.java))
                }
            }
        }
    }
}