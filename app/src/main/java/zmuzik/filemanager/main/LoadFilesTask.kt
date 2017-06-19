package zmuzik.filemanager.main

import android.os.AsyncTask
import zmuzik.filemanager.common.bus.FilesReceivedEvent
import zmuzik.filemanager.common.bus.UiBus
import java.io.File


class LoadFilesTask(val currentFolder: File) : AsyncTask<Void?, Void?, Void?>() {

    override fun doInBackground(vararg params: Void?): Void? {
        val files = currentFolder.listFiles()?.asList()?.sortedBy { it.name } ?: ArrayList()
        UiBus.get().post(FilesReceivedEvent(files))
        return null
    }
}