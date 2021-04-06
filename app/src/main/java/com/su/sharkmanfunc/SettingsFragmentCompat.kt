package com.su.sharkmanfunc

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.preference.*

class SettingsFragmentCompat : PreferenceFragmentCompat() {

    companion object {
        var isEnableAudio = false
        private const val SOUNDS_PATH = "sounds"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pre_settings, rootKey)

        findPreference<SwitchPreference>(getString(R.string.charge_service))?.apply {
            isChecked = SharkManChargeService.isOpen
            onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (it is SwitchPreference) {
                        //Log.d("测速", "守护服务${it.isChecked}")
                        val intent = Intent(requireContext(), SharkManChargeService::class.java)
                        if (it.isChecked)
                            requireActivity().startService(intent)
                        else
                            requireActivity().stopService(intent)
                    }
                    true
                }
        }

        findPreference<SwitchPreference>(getString(R.string.charge_is_audio))?.apply {
            isEnableAudio = isChecked
            onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (it is SwitchPreference)
                        isEnableAudio = it.isChecked
                    true
                }
        }

        initSounds()
    }

    private fun initSounds() {
        findPreference<PreferenceCategory>(getString(R.string.charge_sounds))?.apply {
            try {
                val am = context.assets
                val list = am.list(SOUNDS_PATH)
                var index = 1
                list?.forEach {
                    Log.d("文件", it)
                    val pre = SoundPreference(requireContext(), "${SOUNDS_PATH}/$it").apply {
                        title = "音频${index++}"
                        isIconSpaceReserved = false
                    }
                    addPreference(pre)
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun traversAssets(context: Context, path: String) {
        val am = context.assets

    }

    class SoundPreference(context: Context, soundPath: String) : Preference(context) {

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
            setOnPreferenceClickListener {
                media?.apply {
                    Log.d("播放", soundPath)
                    val am = context.assets.openFd(soundPath)
                    stop()
                    reset()
                    setDataSource(am.fileDescriptor, am.startOffset, am.length)
                    prepareAsync()
                }
                true
            }
        }
    }

}