package zmuzik.filemanager.common.bus

import zmuzik.filemanager.model.FileWrapper

class SelectedFilesChangedEvent(val selectedFiles: ArrayList<FileWrapper>)

