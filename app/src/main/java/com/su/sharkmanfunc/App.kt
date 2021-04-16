package com.su.sharkmanfunc

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.su.appcrashhandler.AppCatchException
import com.su.appcrashhandler.CrashActivityImpl

class App : Application() {


    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var globalContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        globalContext = applicationContext
        //初始化全局异常处理
        CrashActivityImpl.apply {
            KUAN_URL = getString(R.string.b_video_url)
        }
        AppCatchException.bindCrashHandler(this) {
            !BuildConfig.DEBUG
            true
        }
    }
}