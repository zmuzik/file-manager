package zmuzik.filemanager.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.PreferenceManager
import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_main.*
import zmuzik.filemanager.R
import zmuzik.filemanager.common.*
import zmuzik.filemanager.common.bus.*
import zmuzik.filemanager.model.FileWrapper
import zmuzik.filemanager.settings.SettingsActivity

class MainActivity : AppCompatActivity(), ActionMode.Callback {

    val REQ_READ_STORAGE = 101
    val REQ_WRITE_STORAGE = 102

    val prefs by lazy { PrefsHelper(PreferenceManager.getDefaultSharedPreferences(applicationContext)) }

    var currentFolder = FileWrapper(Environment.getRootDirectory())
    var selectedFiles: List<FileWrapper> = ArrayList()
    var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setSupportActionBar(toolbar)
        if (savedInstanceState != null) {
            currentFolder = FileWrapper(savedInstanceState.getString(Keys.CURRENT_FOLDER))
        } else {
            currentFolder = FileWrapper(prefs.defaultFolder)
            if (!currentFolder.exists || !currentFolder.isDir) {
                Toast.makeText(applicationContext, R.string.default_dir_error, Toast.LENGTH_LONG).show()
                currentFolder = FileWrapper(PrefsHelper.factoryResetFolder)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        listDirectoryOrRequestPermission(currentFolder)
    }

    override fun onStart() {
        super.onStart()
        UiBus.get().register(this)
    }

    override fun onStop() {
        super.onStop()
        UiBus.get().unregister(this)
        finishActionMode()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(Keys.CURRENT_FOLDER, currentFolder.path)
    }

    override fun onBackPressed() {
        if (currentFolder.file.parentFile == null
                || currentFolder.file == PrefsHelper.factoryResetFolder) {
            super.onBackPressed()
        } else {
            listDirectory(FileWrapper(currentFolder.file.parent))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_refresh -> {
                listDirectoryOrRequestPermission(currentFolder)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Subscribe fun onListDirectoryRequested(event: ListDirectoryRequestedEvent) {
        finishActionMode()
        listDirectoryOrRequestPermission(event.fileWrapper)
    }

    @Subscribe fun onOpenFileRequested(event: OpenFileRequestedEvent) {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt(event.fileWrapper.name))
        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", event.fileWrapper.file)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.setDataAndType(uri, mimeType)

        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(applicationContext, getString(R.string.no_app_for_ext), Toast.LENGTH_SHORT).show()
        }
    }

    @Subscribe fun onSelectedFilesChanged(event: SelectedFilesChangedEvent) {
        selectedFiles = event.selectedFiles
        if (!event.selectedFiles.isEmpty()) {
            if (actionMode == null) {
                actionMode = startSupportActionMode(this)
            } else {
                actionMode?.invalidate()
            }
        } else {
            finishActionMode()
        }
    }

    fun listDirectory(dir: FileWrapper) {
        currentFolder = dir
        title = currentFolder.name
        if (currentFolder.file.canRead()) {
            LoadFilesTask(currentFolder).execute()
        }
        finishActionMode()
        fileListView.gone()
        progressBar.visible()
    }

    @Subscribe fun onFilesReceived(event: FilesReceivedEvent) {
        fileListView.setData(event.files)
        progressBar.gone()
        fileListView.visible()
    }

    val isReadExternalGranted: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            } else {
                return true
            }
        }

    val isWriteExternalGranted: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            } else {
                return true
            }
        }

    fun requestReadExternalPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_READ_STORAGE)
        }
    }

    fun requestWriteExternalPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQ_WRITE_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQ_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listDirectory(currentFolder)
            }
        } else if (requestCode == REQ_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                deleteSelectedFiles()
            }
        }
    }

    fun listDirectoryOrRequestPermission(dir: FileWrapper) {
        if (isReadExternalGranted) {
            listDirectory(dir)
        } else {
            requestReadExternalPermission()
        }
    }

    fun deleteSelectedOrRequestPermission() {
        if (isWriteExternalGranted) {
            deleteSelectedFiles()
        } else {
            requestWriteExternalPermission()
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.contextual_menu, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        val count = selectedFiles.size
        mode.title = resources.getQuantityString(R.plurals.items_selected, count, count)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                deleteSelectedOrRequestPermission()
                true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        fileListView.clearSelectedFiles()
        actionMode = null
    }

    fun deleteSelectedFiles() {
        var counter = 0
        selectedFiles.forEach { if (it.file.deleteRecursively()) counter++ }
        selectedFiles = ArrayList()
        listDirectory(currentFolder)
        val msg = resources.getQuantityString(R.plurals.items_deleted, counter, counter)
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    fun finishActionMode() {
        fileListView.clearSelectedFiles()
        selectedFiles = ArrayList()
        actionMode?.finish()
    }
}
