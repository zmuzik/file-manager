package zmuzik.filemanager.common.bus

import zmuzik.filemanager.model.FileWrapper

class FilesReceivedEvent(val files: List<FileWrapper>)