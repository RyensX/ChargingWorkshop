package com.su.charging

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import com.su.charging.util.PhoneUtils
import com.su.charging.util.printChargeState
import com.su.charging.util.runCatchingOrReport
import com.su.charging.view.preference.SoundPreference
import com.su.charging.view.fragment.SettingsFragmentCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChargeAudioManager {

    companion object {
        val INS by lazy { ChargeAudioManager() }

        private var isClockPlay = false

        private val flagBuffer by lazy { HashMap<String, String>() }

        @Synchronized
        private fun setData(key: String, data: String) {
            flagBuffer[key] = data
        }

        @Synchronized
        private fun removeData(key: String) {
            flagBuffer.remove(key)
        }

        fun buffFlags(pre: SoundPreference) {
            MainScope().launch {
                withContext(Dispatchers.IO) {
                    if (pre.audioFlags.size > 0) {
                        val sb = StringBuilder()
                        pre.audioFlags.forEach {
                            sb.append(it.ordinal).append(" ")
                        }
                        sb.removeSuffix(" ")
                        val data = sb.toString()
                        setData(pre.title.toString(), data)
                    } else
                        removeData(pre.title.toString())
                }
            }
        }

        fun saveFlags(context: Context) {
            MainScope().launch {
                withContext(Dispatchers.IO) {
                    //构建数据
                    val sb = StringBuilder()
                    flagBuffer.forEach {
                        sb.append(it.key)
                            .append(" ")
                            .append(it.value)
                            .append("\n")
                    }
                    sb.removeSuffix("\n")
                    //保持到SP
                    val sp = PhoneUtils.getDefaultSharedPreferences(context)
                    sp.edit().apply {
                        putString(context.getString(R.string.audio_flags), sb.toString())
                        apply()
                    }
                }
            }
        }

        fun getFlags(context: Context): HashMap<String, IntArray>? {
            PhoneUtils.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.audio_flags), "")
                ?.also { data ->
                    Log.d("Flag数据", data)
                    val result = HashMap<String, IntArray>()
                    data.reader().readLines().forEach {
                        val dataArray = it.split(" ")
                        val flags = mutableListOf<Int>()
                        for (i in 1 until dataArray.size) {
                            val temp = dataArray[i].trim()
                            if (temp != "")
                                flags.add(temp.toInt())
                        }
                        result[dataArray[0]] = flags.toIntArray()
                    }
                    return result
                }
            return null
        }

        fun checkChargeState(battery: Int): SoundPreference.AudioFlag = when (battery) {
            in 0..20 -> SoundPreference.AudioFlag.LOW
            in 21..70 -> SoundPreference.AudioFlag.MEDIUM
            in 71..99 -> SoundPreference.AudioFlag.HIGH
            else -> SoundPreference.AudioFlag.FULL
        }

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

    fun printAudioMap() {
        println("########测试数据#########")
        audioMap.forEach { data ->
            Log.d("Flag=", data.key.name)
            val sb = StringBuilder()
            data.value.forEach {
                sb.append(it).append(" ")
            }
            Log.d("数据", sb.toString())
        }
        println("#####################")
    }

    fun checkIsEmptyAudio(): Boolean {
        audioMap.forEach {
            if (it.value.isNotEmpty())
                return false
        }
        return true
    }

    fun launchSyncAudio(pre: SoundPreference) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                syncAudio(pre)
            }
        }
    }

    @Synchronized
    fun syncAudio(pre: SoundPreference) {
        //移除
        audioMap.forEach { audio ->
            var isEx = false
            audio.value.forEach { path ->
                if (path == pre.soundPath)
                    isEx = true
            }
            if (isEx && !pre.audioFlags.contains(audio.key))
                audio.value.remove(pre.soundPath)
        }
        //添加
        pre.audioFlags.forEach { flag ->
            audioMap[flag]?.add(pre.soundPath)
        }
    }

    private fun getMediaIns() = MediaPlayer().apply {
        setOnPreparedListener {
            start()
        }
        setOnCompletionListener {
            isClockPlay = false
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
        isClockPlay = true
        playAudio(context, SoundPreference.AudioFlag.DISCONNECT)
    }

    private fun playAudio(context: Context, flag: SoundPreference.AudioFlag) {
        media?.checkApply {
            audioMap[flag]?.also {
                if (it.size > 0) {
                    reset()
                    val file = it[it.indices.random()]
                    setDataSource(file)
                    prepareAsync()
                    if (BuildConfig.DEBUG) {
                        val audio = file.substring(file.indexOf("/") + 1, file.indexOf("."))
                        if (flag != SoundPreference.AudioFlag.DISCONNECT)
                            flag.printChargeState(
                                context,
                                "插入充电器 ",
                                " 播放:${audio}"
                            )
                        else
                            SoundPreference.AudioFlag.DISCONNECT.printChargeState(
                                context,
                                suf = "充电器  播放:$audio"
                            )
                    }
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
        if (!isClockPlay)
            media?.stop()
    }

    inline fun <T> T.checkApply(block: T.() -> Unit): T {
        runCatchingOrReport {
            if (SettingsFragmentCompat.isEnableAudio) {
                block()
            }
        }
        return this
    }

}