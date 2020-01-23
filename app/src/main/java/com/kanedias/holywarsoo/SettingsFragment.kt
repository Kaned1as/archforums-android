package com.kanedias.holywarsoo

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import com.kanedias.holywarsoo.misc.resolveAttr


/**
 * Fragment for showing and managing global preferences
 *
 * @author Kanedias
 *
 * Created on 2018-04-26
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.global_prefs)
        val color = requireContext().resolveAttr(R.attr.colorPrimary)
        tintIcons(preferenceScreen, color)
    }

    private fun tintIcons(preference: Preference, color: Int) {
        if (preference is PreferenceGroup) {
            for (i in 0 until preference.preferenceCount) {
                tintIcons(preference.getPreference(i), color)
            }
        } else {
            preference.icon.setTint(color)
        }
    }
}