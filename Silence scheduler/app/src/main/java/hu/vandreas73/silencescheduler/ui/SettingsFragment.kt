package hu.vandreas73.silencescheduler.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import hu.vandreas73.silencescheduler.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}