package com.su.sharkmanfunc

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!PermissionUtils.INS.checkWindowPermission(this))
            PermissionBottomSheetFragment.open(this)
        title = "$title(${packageManager.getPackageInfo(packageName, 0).versionName})"
    }

    fun Activity.tip(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (SharkManChargeService.isOpen) {
            moveTaskToBack(true)
        } else
            super.onBackPressed()
    }

}