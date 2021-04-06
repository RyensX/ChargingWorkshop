package com.su.sharkmanfunc

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class ChargeAudioManager {

    companion object {
        val INS by lazy { ChargeAudioManager() }
    }

    private var media: MediaPlayer? = MediaPlayer().checkApply {
        setOnPreparedListener {
            start()
        }
    }

    private val lowBattery = arrayOf(R.raw.low_1, R.raw.low_2)
    private val normalBattery = arrayOf(R.raw.charging_1, R.raw.charging_2)
    private val offBattery = R.raw.off
    private val completeCharge = R.raw.completed

    fun play(context: Context, battery: Int) {
        media?.also {
            it.reset()
            when {
                battery <= 5 -> playOff(context)
                battery in 6..20 -> playLow(context)
                battery in 21..99 -> playNormal(context)
                else -> playCompleted(context)
            }
        }
    }

    fun playLow(context: Context) {
        media?.checkApply {
            setDataSource(context, getRandomAudio(context, lowBattery))
            prepare()
            start()
        }
    }

    fun playNormal(context: Context) {
        media?.checkApply {
            setDataSource(context, getRandomAudio(context, normalBattery))
            prepareAsync()
        }

    }

    fun playOff(context: Context) {
        media?.checkApply {
            setDataSource(context, getRawFileUrl(context, offBattery))
            prepareAsync()
        }
    }

    fun playCompleted(context: Context) {
        media?.checkApply {
            setDataSource(context, getRawFileUrl(context, completeCharge))
            prepareAsync()
        }
    }

    private inline fun getRandomAudio(context: Context, array: Array<Int>) =
        getRawFileUrl(context, array[(array.indices).random()])

    private inline fun getRawFileUrl(context: Context, id: Int) =
        Uri.parse("android.resource://${context.packageName}/${id}")

    fun release() {
        media?.checkApply {
            stop()
            release()
            media = null
        }
    }

    inline fun <T> T.checkApply(block: T.() -> Unit): T {
        if (SettingsFragmentCompat.isEnableAudio)
            block()
        return this
    }

}