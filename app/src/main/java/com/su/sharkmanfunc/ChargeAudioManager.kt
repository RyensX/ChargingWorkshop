package com.su.sharkmanfunc

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

class ChargeAudioManager {

    companion object {
        val INS by lazy { ChargeAudioManager() }
    }

    private var media: MediaPlayer? = null
        get() {
            if (field == null)
                field = getMediaIns()
            return field
        }

    private val audioMap =
        mutableMapOf(
            SoundPreference.AudioFlag.LOW to mutableListOf<String>(),
            SoundPreference.AudioFlag.MEDIUM to mutableListOf(),
            SoundPreference.AudioFlag.HIGH to mutableListOf(),
            SoundPreference.AudioFlag.FULL to mutableListOf(),
            SoundPreference.AudioFlag.DISCONNECT to mutableListOf()
        )

    fun syncAudio(pre: SoundPreference) {
        pre.audioFlags.forEach { flag ->
            audioMap[flag]?.apply {
                remove(pre.soundPath)
                add(pre.soundPath)
            }
        }
    }

    private fun getMediaIns() = MediaPlayer().apply {
        setOnPreparedListener {
            start()
        }
    }

    fun play(context: Context, battery: Int) {
        media?.also {
            when (battery) {
                in 0..20 -> playLow(context)
                in 21..70 -> playMedium(context)
                in 71..99 -> playHigh(context)
                else -> playFull(context)
            }
        }
    }

    fun playLow(context: Context) {
        playAudio(context, SoundPreference.AudioFlag.LOW)
    }

    fun playMedium(context: Context) {
        playAudio(context, SoundPreference.AudioFlag.MEDIUM)
    }

    fun playHigh(context: Context) {
        playAudio(context, SoundPreference.AudioFlag.HIGH)
    }

    fun playFull(context: Context) {
        playAudio(context, SoundPreference.AudioFlag.FULL)
    }

    fun playDisconnect(context: Context) {
        playAudio(context, SoundPreference.AudioFlag.DISCONNECT)
    }

    private fun playAudio(context: Context, flag: SoundPreference.AudioFlag) {
        media?.checkApply {
            audioMap[flag]?.also {
                if (it.size > 0) {
                    reset()
                    val am = context.assets.openFd(it[it.indices.random()])
                    setDataSource(am.fileDescriptor, am.startOffset, am.length)
                    prepareAsync()
                }
            }
        }
    }

    fun release() {
        media?.checkApply {
            stopMedia()
            release()
            media = null
        }
    }

    fun stopMedia() {
        media?.stop()
    }

    inline fun <T> T.checkApply(block: T.() -> Unit): T {
        if (SettingsFragmentCompat.isEnableAudio) {
            block()
        }
        return this
    }

}