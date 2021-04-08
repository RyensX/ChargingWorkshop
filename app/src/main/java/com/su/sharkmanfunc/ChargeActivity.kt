package com.su.sharkmanfunc

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.View
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

        setBattery(BatteryBroadCastReceiver.firstBattery)
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
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        (if (SettingsFragmentCompat.isKeepShow) WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON else 0)
            )
            decorView.also {
                it.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
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
        setBattery(battery)
        if (battery > 99)
            ChargeAudioManager.INS.playCompleted(this)
    }

    private fun setBattery(battery: Int) {
        val res = "$battery%"
        val sp = SpannableString(res).apply {
            setSpan(
                AbsoluteSizeSpan(15, true),
                res.length - 1,
                res.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        info.text = sp
    }
}