package com.su.sharkmanfunc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference

class SettingsFragmentCompat : PreferenceFragmentCompat() {

    companion object {
        var isEnableAudio = false
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pre_settings, rootKey)

        findPreference<SwitchPreference>(getString(R.string.charge_service))?.onPreferenceClickListener =
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

        findPreference<SwitchPreference>(getString(R.string.charge_is_audio))?.apply {
            isEnableAudio = isChecked
            onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (it is SwitchPreference)
                        isEnableAudio = it.isChecked
                    true
                }
        }
    }

}