package com.su.sharkmanfunc

import android.app.KeyguardManager
import android.content.Context

fun checkIsClock(context: Context): Boolean {
    (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).apply {
        return isKeyguardLocked
    }
}