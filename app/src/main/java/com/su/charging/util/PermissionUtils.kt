package com.su.charging.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.Fragment


class PermissionUtils {

    companion object {
        val INS by lazy { PermissionUtils() }

        const val WINDOW_REQUEST_CODE = 6666
    }

    fun checkWindowPermission(context: Context) =
        Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context)

    fun requestWindowPermission(context: Fragment) {
        if (!checkWindowPermission(context.requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.requireContext().packageName}")
            )
            context.startActivityForResult(intent, WINDOW_REQUEST_CODE)
        }
    }
}