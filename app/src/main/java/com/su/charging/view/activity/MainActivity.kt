package com.su.charging.view.activity

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.su.charging.view.fragment.PermissionBottomSheetFragment
import com.su.charging.util.PermissionUtils
import com.su.charging.R
import com.su.charging.ChargingService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!PermissionUtils.INS.checkWindowPermission(this))
            PermissionBottomSheetFragment.open(this)
    }

    fun Activity.tip(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (ChargingService.isOpen) {
            moveTaskToBack(true)
        } else
            super.onBackPressed()
    }

}