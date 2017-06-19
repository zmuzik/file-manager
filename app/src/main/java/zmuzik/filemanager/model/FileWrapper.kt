package zmuzik.filemanager.model

import java.io.File

/**
 * Holds all the data that would otherwise have to be retrieved in various situations asynchronously.
 * This class is mostly meant to be constructed in background thread
 */

class FileWrapper(val file: File) {
    val path = file.absolutePath
    val name = file.name
    val isDir = file.isDirectory
    val exists = file.exists()
    val dirSize = if (isDir) file.list()?.size ?: 0 else 0
    val fileSize = file.length()
    val lastModified = file.lastModified()

    constructor(path: String) : this(File(path))
}