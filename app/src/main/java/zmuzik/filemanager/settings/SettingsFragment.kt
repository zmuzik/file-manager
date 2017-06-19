package zmuzik.filemanager.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
import zmuzik.filemanager.R
import zmuzik.filemanager.common.Keys
import zmuzik.filemanager.common.PrefsHelper


class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(activity.applicationContext) }
    val prefsHelper by lazy { PrefsHelper(sharedPreferences) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        val defaultFolderPref = findPreference(Keys.DEFAULT_FOLDER)
        defaultFolderPref.summary = prefsHelper.defaultFolder.absolutePath
        defaultFolderPref.setDefaultValue(prefsHelper.defaultFolder.absolutePath)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (Keys.DEFAULT_FOLDER == key) {
            findPreference(key).summary = sharedPreferences.getString(Keys.DEFAULT_FOLDER, "")
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}