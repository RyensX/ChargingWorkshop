package com.su.sharkmanfunc

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

object PhoneUtils {
    fun checkIsCharging(context: Context): Boolean {
        val bc = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return bc?.let { it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0 } ?: false
    }

    /**
     * 获取电压(mV)
     */
    fun getChargeVoltage(context: Context): Int {
        val bc = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return bc?.getIntExtra(
            BatteryManager.EXTRA_VOLTAGE,
            BatteryManager.BATTERY_HEALTH_UNKNOWN
        ) ?: -1
    }
}