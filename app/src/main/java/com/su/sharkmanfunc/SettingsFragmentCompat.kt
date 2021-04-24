package com.su.sharkmanfunc

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class SettingsFragmentCompat : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    companion object {
        var isEnableAudio = false
        var isClickClose = true
        var isOpenOnClock = false
        var isKeepShow = false
        var isNotOpenOnFull = true
        var isForegroundService = true
        private const val SOUNDS_PATH = "sounds"
    }

    private var foregroundService: SwitchPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pre_settings, rootKey)

        initPre()
        initSounds()
    }

    private inline fun setTaskRecent(context: Context, isClear: Boolean) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.appTasks?.get(0)?.setExcludeFromRecents(isClear)
    }

    private inline fun bindPre(
        @StringRes keyRes: Int,
        init: SwitchPreference. () -> Unit,
        crossinline click: SwitchPreference.() -> Unit
    ): SwitchPreference? {
        return findPreference<SwitchPreference>(getString(keyRes))?.apply {
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
        //以前台服务方式启动
        foregroundService = bindPre(R.string.charge_foreground_service,
            { isEnabled = !SharkManChargeService.isOpen }) {
            isForegroundService = isChecked
        }
        //充电守护服务
        bindPre(R.string.charge_service,
            { isChecked = SharkManChargeService.isOpen }) {
            val intent = Intent(App.globalContext, SharkManChargeService::class.java)
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
        }
        //是否播放音频
        bindPre(R.string.charge_is_audio,
            { isEnableAudio = isChecked }) {
            isEnableAudio = isChecked
        }
        //亮屏守护
        bindPre(R.string.charge_screen_on_open, { isOpenOnClock = isChecked }) {
            isOpenOnClock = isChecked
        }
        //守护常亮
        bindPre(R.string.charge_keep_show,
            { isKeepShow = isChecked }) {
            isKeepShow = isChecked
        }
        //隐藏最近任务
        bindPre(R.string.is_clear_recent,
            { setTaskRecent(requireContext(), isChecked) }) {
            setTaskRecent(requireContext(), isChecked)
        }
        //勿扰模式
        bindPre(R.string.is_not_open_on_full,
            { isNotOpenOnFull = isChecked }) {
            isNotOpenOnFull = isChecked
            if (!isNotOpenOnFull)
                PhoneUtils.removeCheckFullView(requireContext())
            else if (SharkManChargeService.isOpen)
                PhoneUtils.checkIsOnFullScreen(requireContext())
        }
    }

    private fun initSounds() {
        findPreference<PreferenceCategory>(getString(R.string.charge_sounds))?.apply {
            MainScope().launch {
                var effectiveNumber = 0
                flow<Preference> {
                    val flagData = ChargeAudioManager.getFlags(context)
                    context.assets.list(SOUNDS_PATH)?.also { list ->
                        val flags = SoundPreference.AudioFlag.values()
                        try {
                            val suffixRegex = Regex("\\..+")
                            list.forEach {
                                if (it.endsWith(".mp3")) {
                                    //创建音频Preference
                                    Log.d("文件(${Thread.currentThread()})", it)
                                    val pre =
                                        SoundPreference(
                                            requireContext(),
                                            "${SOUNDS_PATH}/$it"
                                        ).apply {
                                            title = it.replace(suffixRegex, "")
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

    override fun onPause() {
        super.onPause()
        ChargeAudioManager.saveFlags(requireContext())
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
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        lastClickPreference = preference as SoundPreference?
        chooseDialog.show()
        return true
    }

}