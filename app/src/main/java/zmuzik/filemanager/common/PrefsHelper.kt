package zmuzik.filemanager.common

import android.content.SharedPreferences
import android.os.Environment
import java.io.File

class PrefsHelper constructor(private val preferences: SharedPreferences) {

    init {
        if (preferences.getString(Keys.DEFAULT_FOLDER, null) == null) {
            preferences.edit().putString(Keys.DEFAULT_FOLDER, factoryResetFolder.absolutePath).apply()
        }
    }

    var defaultFolder: File
        get() = File(preferences.getString(Keys.DEFAULT_FOLDER, factoryResetFolder.absolutePath))
        set(file) = preferences.edit().putString(Keys.DEFAULT_FOLDER, file.absolutePath).apply()

    companion object {

        val factoryResetFolder
            get(): File {
                var folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                if (folder != null) {
                    folder = folder.parentFile ?: folder
                } else {
                    folder = Environment.getRootDirectory()
                }
                return folder
            }
    }
}
