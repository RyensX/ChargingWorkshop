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
        findViewById<TextView>(R.id.version).apply {
            val tf = Typeface.createFromAsset(assets, "fonts/bshark_bold.ttf")
            typeface = tf
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 0)
            }
        }
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

    override fun onDestroy() {
        ChargeAudioManager.INS.release()
        super.onDestroy()
    }

}