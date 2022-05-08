package com.su.charging.view.fragment

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.su.charging.*
import com.su.charging.R
import com.su.charging.util.PhoneUtils
import com.su.charging.view.preference.SoundPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingsFragmentCompat : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    companion object {
        var isEnableAudio = false
        var isClickClose = true
        var isOpenOnClock = false
        var isKeepShow = false
        var isNotOpenOnFull = true
        var isForegroundService = true
    }

    private val scope = MainScope()

    private var foregroundService: SwitchPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pre_settings, rootKey)

        findPreference<Preference>("version")?.summary =
            requireContext().run { packageManager.getPackageInfo(packageName, 0).versionName }
        findPreference<Preference>("video")?.summary = getString(
            R.string.video_res_path_format,
            Charging.normalChargingVideo.absolutePath,
            Charging.quickChargingVideo.absolutePath
        )
        findPreference<Preference>("clear_audio_flags")?.onPreferenceClickListener = this
        findPreference<Preference>("audio")?.summary = Charging.audioResPath.absolutePath
        initPre()
        initSounds()
    }

    private inline fun setTaskRecent(context: Context, isClear: Boolean) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.appTasks?.get(0)?.setExcludeFromRecents(isClear)
    }

    private inline fun bindPre(
        pre: SwitchPreference,
        init: SwitchPreference. () -> Unit,
        crossinline click: SwitchPreference.() -> Unit
    ) {
        pre.apply {
            init()
            onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (it is SwitchPreference)
                        click()
                    true
                }
        }
    }

    private fun initPre() {
        scope.launch {
            class PreInit(
                @StringRes val id: Int,
                val initMethod: SwitchPreference. () -> Unit,
                val clickMethod: SwitchPreference.() -> Unit
            )

            var datas: Array<PreInit>?
            withContext(Dispatchers.IO) {
                val pres = mutableListOf<SwitchPreference>()
                datas = arrayOf(
                    //以前台服务方式启动
                    PreInit(
                        R.string.charge_foreground_service,
                        { isEnabled = !ChargingService.isOpen }) {
                        isForegroundService = isChecked
                    },
                    //充电守护服务
                    PreInit(
                        R.string.charge_service,
                        { isChecked = ChargingService.isOpen }) {
                        runCatching {
                            if (!Charging.normalChargingVideo.exists() || !Charging.quickChargingVideo.exists())
                                throw RuntimeException("未添加动画文件，请根据路径放入动画文件")

                            val intent = Intent(App.globalContext, ChargingService::class.java)
                            if (isChecked) {
                                if (ChargeAudioManager.INS.checkIsEmptyAudio() || !isEnableAudio)
                                    Toast.makeText(
                                        App.globalContext,
                                        "服务已启动，但未开启语音或尚未设置音频(拉到下方设置)，因此无论什么状态都不会发声。",
                                        Toast.LENGTH_LONG
                                    ).show()
                                requireActivity().startService(intent)
                            } else
                                requireActivity().stopService(intent)
                            //锁定前台服务设置
                            foregroundService?.isEnabled = !isChecked
                        }.onFailure {
                            isChecked = false
                            Toast.makeText(App.globalContext, it.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    //是否播放音频
                    PreInit(
                        R.string.charge_is_audio,
                        { isEnableAudio = isChecked }) {
                        runCatching {
                            if (!Charging.audioResPath.exists())
                                throw RuntimeException("未添加音频文件，请根据路径放入音频文件")
                            isEnableAudio = isChecked
                        }.onFailure {
                            isChecked = false
                            Toast.makeText(App.globalContext, it.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    //亮屏守护
                    PreInit(R.string.charge_screen_on_open, { isOpenOnClock = isChecked }) {
                        isOpenOnClock = isChecked
                    },
                    //守护常亮
                    PreInit(
                        R.string.charge_keep_show,
                        { isKeepShow = isChecked }) {
                        isKeepShow = isChecked
                    },
                    //隐藏最近任务
                    PreInit(
                        R.string.is_clear_recent,
                        { setTaskRecent(requireContext(), isChecked) }) {
                        setTaskRecent(requireContext(), isChecked)
                    },
                    //勿扰模式
                    PreInit(
                        R.string.is_not_open_on_full,
                        { isNotOpenOnFull = isChecked }) {
                        isNotOpenOnFull = isChecked
                        if (!isNotOpenOnFull)
                            PhoneUtils.removeCheckFullView(requireContext())
                        else if (ChargingService.isOpen)
                            PhoneUtils.checkIsOnFullScreen(requireContext())
                    }
                )
                //查找
                datas?.forEach { pi ->
                    findPreference<SwitchPreference>(getString(pi.id))?.also {
                        pres.add(it)
                    }
                }
                pres
            }.also {
                //绑定
                if (datas != null)
                    for (i in it.indices) {
                        val data = datas!![i]
                        bindPre(it[i], data.initMethod, data.clickMethod)
                    }
            }
        }
    }

    private fun initSounds() {
        findPreference<PreferenceCategory>(getString(R.string.charge_sounds))?.apply {
            scope.launch {
                var effectiveNumber = 0
                flow<Preference> {
                    val flagData = ChargeAudioManager.getFlags(context)
                    Charging.audioResPath.listFiles()?.also { list ->
                        val flags = SoundPreference.AudioFlag.values()
                        try {
                            val suffixRegex = Regex("\\..+")
                            list.forEach {
                                if (it.name.endsWith(".mp3")) {
                                    //创建音频Preference
                                    Log.d("文件(${Thread.currentThread()})", it.absolutePath)
                                    val pre =
                                        SoundPreference(
                                            requireContext(),
                                            it.absolutePath
                                        ).apply {
                                            title = it.name.replace(suffixRegex, "")
                                            isIconSpaceReserved = false
                                        }
                                    pre.onPreferenceClickListener = this@SettingsFragmentCompat
                                    //读取音频Flag设置
                                    flagData?.get(pre.title)?.forEach { flag ->
                                        pre.changeFlag(flags[flag])
                                    } ?: ChargeAudioManager.INS.launchSyncAudio(pre)

                                    emit(pre)
                                    effectiveNumber++
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                    .catch {}
                    .flowOn(Dispatchers.IO)
                    .onCompletion {
                        findPreference<PreferenceCategory>(getString(R.string.sound_list))?.title =
                            "${getString(R.string.sound_list)}(${effectiveNumber})"
                    }
                    .collect {
                        Log.d("添加音频(${Thread.currentThread()})", it.title.toString())
                        addPreference(it)
                    }
            }
        }
    }

    private fun traversAssets(context: Context, path: String) {
        val am = context.assets

    }

    private var lastClickPreference: SoundPreference? = null
    private val chooseDialog by lazy {
        val items =
            arrayOf(
                "播放",
                *SoundPreference.AudioFlag.values().map { "设置为\"${it.flag}\"音频" }.toTypedArray()
            )
        val listDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        listDialog.setTitle("操作")
        listDialog.setItems(
            items
        ) { _, which ->
            lastClickPreference?.apply {
                if (which == 0)
                    playAudio()
                else {
                    changeFlag(SoundPreference.AudioFlag.values()[which - 1])
                    syncFlags()
                }
            }
            ChargeAudioManager.saveFlags(requireContext())
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "clear_audio_flags" -> {
                //清空flags
                val context = requireContext()
                val sp = PhoneUtils.getDefaultSharedPreferences(context)
                sp.edit().apply {
                    remove(context.getString(R.string.audio_flags))
                    apply()
                }
                requireActivity().recreate()
            }
            else -> {
                lastClickPreference = preference as SoundPreference?
                chooseDialog.show()
            }
        }
        return true
    }

}