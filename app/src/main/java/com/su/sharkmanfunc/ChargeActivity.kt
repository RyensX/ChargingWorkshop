package com.su.sharkmanfunc

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout

class ChargeActivity : AppCompatActivity(), BatteryBroadCastReceiver.BatteryListener {

    private lateinit var video: VideoView
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge)

        findViewById<ConstraintLayout>(R.id.charge_top).setOnClickListener {
            if (SettingsFragmentCompat.isClickClose)
                onPowerDisconnected()
        }

        video = findViewById(R.id.video)
        info = findViewById(R.id.charge_info)

        chargeData()
        chargeConfig()
    }

    private fun chargeData() {
        //充电信息
        val tf = Typeface.createFromAsset(assets, "fonts/bshark_bold.ttf")
        info.typeface = tf

        info.text = "${BatteryBroadCastReceiver.firstBattery}%"
        //视频
        video.apply {
            setVideoURI(Uri.parse("android.resource://${packageName}/${R.raw.wired_quick_charge_video}"))
            setOnCompletionListener {
                start()
            }
            start()
        }
        //音频
        ChargeAudioManager.INS.play(applicationContext, BatteryBroadCastReceiver.firstBattery)
    }

    private fun chargeConfig() {
        BatteryBroadCastReceiver.addBatteryListener(this)

        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        window.apply {
            addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    override fun onStop() {
        super.onStop()
        ChargeAudioManager.INS.stopMedia()
        BatteryBroadCastReceiver.removeBatteryListener()
        finish()
    }

    override fun onPowerConnected() {

    }

    override fun onPowerDisconnected() {
        finish()
    }

    override fun onPowerChange(battery: Int) {
        info.text = "$battery%"
        if (battery > 99)
            ChargeAudioManager.INS.playCompleted(this)
    }
}