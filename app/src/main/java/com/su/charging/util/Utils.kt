package com.su.charging.util

import android.app.KeyguardManager
import android.content.Context
import android.widget.Toast
import com.su.charging.BuildConfig
import com.su.charging.view.preference.SoundPreference

fun checkIsClock(context: Context): Boolean {
    (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).apply {
        return isKeyguardLocked
    }
}

fun SoundPreference.AudioFlag.printChargeState(
    context: Context,
    pre: String? = null,
    suf: String? = null
) {
    if (BuildConfig.DEBUG)
        Toast.makeText(context, "[调试]:${pre ?: ""}${flag}${suf ?: ""}", Toast.LENGTH_SHORT).show()
}