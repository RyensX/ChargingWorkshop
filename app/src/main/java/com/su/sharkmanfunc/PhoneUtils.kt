package com.su.sharkmanfunc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager

@SuppressLint("StaticFieldLeak")
object PhoneUtils {

    inline fun getDefaultSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE
        )

    private var checkFullView: View? = null

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

    fun closeFullCheckView(context: Context) {
        checkFullView?.also {
            (context.getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)
            checkFullView = null
        }
    }

    fun removeCheckFullView(context: Context) {
        checkFullView?.apply {
            val windowManager =
                context.applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(this)
            checkFullView = null
        }
    }

    fun checkIsOnFullScreen(context: Context): Boolean {
        if (checkFullView == null) {
            val ac = context.applicationContext
            checkFullView = View(ac).apply {
                setBackgroundColor(Color.RED)
            }
            val windowManager = ac.getSystemService(WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams().apply {
                //异形屏
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
                //不焦点不触摸
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                //放左上角
                gravity = Gravity.START or Gravity.TOP
                height = 0
                width = 0

                type =
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else WindowManager.LayoutParams.TYPE_TOAST)
            }

            try {
                windowManager.addView(checkFullView, params)
            } catch (e: Exception) {
                Log.d("全屏检测出现错误", e.message ?: "null")
                checkFullView = null
            }
        }
        val local = IntArray(2)
        checkFullView!!.getLocationOnScreen(local)
        local.forEach { Log.d("数据", it.toString()) }
        val rect = Rect()
        checkFullView!!.getGlobalVisibleRect(rect)
        Log.d("数据2", "${rect.left} ${rect.right}")
        return local[1] == 0
    }
}