package zmuzik.filemanager.main

import android.os.AsyncTask
import zmuzik.filemanager.common.bus.FilesReceivedEvent
import zmuzik.filemanager.common.bus.UiBus
import zmuzik.filemanager.model.FileWrapper


class LoadFilesTask(val currentFolder: FileWrapper) : AsyncTask<Void?, Void?, Void?>() {

    override fun doInBackground(vararg params: Void?): Void? {
        val files = currentFolder.file.listFiles()?.asList()?.sortedBy { it.name } ?: ArrayList()
        UiBus.get().post(FilesReceivedEvent(files.map { FileWrapper(it) }))
        return null
    }
}