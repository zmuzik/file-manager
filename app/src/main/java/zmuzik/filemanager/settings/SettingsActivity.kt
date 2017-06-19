package zmuzik.filemanager.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import zmuzik.filemanager.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, SettingsFragment())
                .commit()
    }
}