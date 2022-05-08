package com.su.charging.view.preference

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.su.charging.ChargeAudioManager
import com.su.charging.util.runCatchingOrReport

class SoundPreference(context: Context, val soundPath: String) : Preference(context) {

    private var flagView: TextView? = null

    val audioFlags = mutableSetOf<AudioFlag>()

    companion object {
        private var media: MediaPlayer? = null

        fun releaseMedia() {
            media?.apply {
                stop()
                release()
                media = null
            }
        }
    }

    init {
        if (media == null)
            media = MediaPlayer().apply {
                setOnPreparedListener {
                    start()
                }
            }
    }

    fun playAudio() {
        runCatchingOrReport {
            media?.apply {
                Log.d("播放", soundPath)
                stop()
                reset()
                setDataSource(soundPath)
                prepareAsync()
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder?.apply {
            flagView =
                itemView.findViewWithTag(AudioFlag::name) ?: TextView(context).apply {
                    tag = AudioFlag::name
                    if (itemView is ViewGroup)
                        (itemView as ViewGroup).addView(this)
                }
            syncFlags()
        }
    }

    fun changeFlag(flag: AudioFlag) {
        if (audioFlags.contains(flag))
            audioFlags.remove(flag)
        else
            audioFlags.add(flag)
        ChargeAudioManager.buffFlags(this)
        ChargeAudioManager.INS.launchSyncAudio(this)
    }

    fun syncFlags() {
        if (audioFlags.size > 0) {
            val sb = StringBuilder()
            sb.append("( ")
            audioFlags.forEach {
                sb.append(it.flag)
                sb.append(" ")
            }
            sb.append(")")
            flagView!!.text = sb.toString()
        } else
            flagView!!.text = ""
    }

    enum class AudioFlag(val flag: String) {
        LOW("低电量"), MEDIUM("中电量"), HIGH("高电量"), FULL("充满"), DISCONNECT("拔出")
    }
}