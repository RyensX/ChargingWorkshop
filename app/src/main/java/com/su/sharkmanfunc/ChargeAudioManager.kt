package com.su.sharkmanfunc

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
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
        println("########测速数据#########")
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

    fun syncAudio(pre: SoundPreference) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                //移除
                audioMap.forEach { audio ->
                    var isEx = false
                    audio.value.forEach { path ->
                        if (path == pre.soundPath)
                            isEx = true
                    }
                    if (isEx && !pre.audioFlags.contains(audio.key))
                        audio.value.removeData(pre.soundPath)
                }
                //添加
                pre.audioFlags.forEach { flag ->
                    audioMap[flag]?.addData(pre.soundPath)
                }
            }.also {
                //printAudioMap()
            }
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
        if (!isClockPlay)
            media?.stop()
    }

    inline fun <T> T.checkApply(block: T.() -> Unit): T {
        if (SettingsFragmentCompat.isEnableAudio) {
            block()
        }
        return this
    }

    @Synchronized
    fun MutableList<String>.addData(data: String) {
        add(data)
    }

    @Synchronized
    fun MutableList<String>.removeData(key: String) {
        remove(key)
    }

}