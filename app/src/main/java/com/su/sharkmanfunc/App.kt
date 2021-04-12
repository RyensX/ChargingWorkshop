package com.su.sharkmanfunc

import android.app.Application
import com.su.appcrashhandler.AppCatchException
import com.su.appcrashhandler.CrashActivityImpl

class App : Application() {
    override fun onCreate() {
        super.onCreate()
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