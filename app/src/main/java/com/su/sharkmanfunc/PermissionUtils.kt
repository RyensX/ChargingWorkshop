package com.su.sharkmanfunc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings


class PermissionUtils {

    companion object {
        val INS by lazy { PermissionUtils() }

        const val WINDOW_REQUEST_CODE = 6666
    }

    fun checkWindowPermission(context: Context) =
        Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context)

    fun requestWindowPermission(context: Activity) {
        if (!checkWindowPermission(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivityForResult(intent, WINDOW_REQUEST_CODE)
        }
    }
}