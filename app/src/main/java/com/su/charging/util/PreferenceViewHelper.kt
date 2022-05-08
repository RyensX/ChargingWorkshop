package com.su.charging.util

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup

class PreferenceViewHelper {

    companion object {
        var INS: PreferenceViewHelper? = null
            get() {
                if (field == null)
                    field = PreferenceViewHelper()
                return field
            }
    }

    private val preferenceInsMap by lazy { HashMap<Int, List<Preference>>() }

    fun getPreferenceLocalIndex(pre: Preference): Int {
        var list = preferenceInsMap[pre.hashCode()]
        if (list == null) {
            pre.parent?.apply {
                var targetClass: Class<*> = javaClass
                while (targetClass != PreferenceGroup::class.java)
                    targetClass = targetClass.superclass
                targetClass.getDeclaredField("mPreferences").apply {
                    isAccessible = true
                    list = get(pre.parent) as? List<Preference>
                }
            }
        }
        return list?.indexOf(pre) ?: -1
    }

    fun getPreferenceGlobalIndex(pff: PreferenceFragmentCompat, pre: Preference): Int {
        var add = 0
        for (i in 0 until pff.preferenceScreen.preferenceCount) {
            val pc = pff.preferenceScreen.getPreference(i) as PreferenceGroup
            if (pc == pre.parent)
                return getPreferenceLocalIndex(pre) + add + i + 1
            add += pc.preferenceCount
        }
        return -1
    }

    fun getPreferenceView(pff: PreferenceFragmentCompat, pre: Preference) =
        pff.listView?.layoutManager?.findViewByPosition(getPreferenceGlobalIndex(pff, pre))

    fun release() {
        preferenceInsMap.clear()
        INS = null
    }
}