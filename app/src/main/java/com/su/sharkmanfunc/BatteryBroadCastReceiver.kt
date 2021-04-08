package com.su.sharkmanfunc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import android.widget.Toast

class BatteryBroadCastReceiver : BroadcastReceiver() {

    companion object {
        private var listener: BatteryListener? = null

        var firstBattery = 0

        fun checkIsCharging(context: Context): Boolean {
            val bc = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            return bc?.let { it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0 } ?: false
        }

        fun addBatteryListener(listener: BatteryListener) {
            this.listener = listener
        }

        fun removeBatteryListener() {
            listener = null
        }
    }

    fun register(context: Context) {
        IntentFilter().apply {
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
        }.also {
            context.registerReceiver(this, it)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.also {
            intent?.action?.also { action ->
                when (action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        listener?.onPowerConnected()
                        openChargeAnim(context)
                        //Toast.makeText(context, "插入充电器", Toast.LENGTH_LONG).show()
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        listener?.onPowerDisconnected()
                        //Toast.makeText(context,"拔出充电器",Toast.LENGTH_LONG).show()
                    }
                    Intent.ACTION_BATTERY_CHANGED -> {
                        intent.extras?.getInt(BatteryManager.EXTRA_LEVEL)?.also { cur ->
                            intent.extras?.getInt(BatteryManager.EXTRA_SCALE)
                                ?.also { total ->
                                    firstBattery = cur
                                    listener?.onPowerChange(firstBattery)
                                    //Toast.makeText(context,"数据:$cur $total",Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        if (SettingsFragmentCompat.isOpenOnClock &&
                            checkIsClock(context) &&
                            checkIsCharging(context)
                        )
                            openChargeAnim(context)
                    }
                }
            }
        }
    }

    private inline fun openChargeAnim(context: Context) {
        Intent(context, ChargeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }.also { context.startActivity(it) }
    }

    interface BatteryListener {
        fun onPowerConnected()
        fun onPowerDisconnected()
        fun onPowerChange(battery: Int)
    }
}