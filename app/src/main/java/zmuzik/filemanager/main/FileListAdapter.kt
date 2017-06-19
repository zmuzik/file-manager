package zmuzik.filemanager.main

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import zmuzik.filemanager.R
import zmuzik.filemanager.common.bus.ListDirectoryRequestedEvent
import zmuzik.filemanager.common.bus.OpenFileRequestedEvent
import zmuzik.filemanager.common.bus.SelectedFilesChangedEvent
import zmuzik.filemanager.common.bus.UiBus
import zmuzik.filemanager.common.formatDate
import zmuzik.filemanager.common.formatFileSize
import zmuzik.filemanager.model.FileWrapper


class FileListAdapter(val context: Context, val files: List<FileWrapper>, val isGrid: Boolean) :
        RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    val selectedItems: ArrayList<FileWrapper> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileListAdapter.ViewHolder {
        val itemLayoutId = if (isGrid) R.layout.file_grid_item else R.layout.file_list_item
        val view = LayoutInflater.from(parent.context).inflate(itemLayoutId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileListAdapter.ViewHolder, position: Int) {
        val file = files[position]
        val isDir = file.isDir
        val iconId = when {
            (isDir && selectedItems.contains(file)) -> R.drawable.ic_folder_selected_24dp
            (isDir && !selectedItems.contains(file)) -> R.drawable.ic_folder_24dp
            (!isDir && selectedItems.contains(file)) -> R.drawable.ic_file_selected_24dp
            else -> R.drawable.ic_file_24dp
        }

        holder.icon.setImageDrawable(ContextCompat.getDrawable(context, iconId))
        holder.fileName.text = file.name
        holder.date.text = formatDate(file.lastModified)
        holder.size.text =
                if (isDir) context.resources.getQuantityString(R.plurals.items, file.dirSize, file.dirSize)
                else formatFileSize(file.fileSize)

        holder.itemRoot.setOnClickListener {
            if (isDir) {
                val pulseAnim = AnimationUtils.loadAnimation(context, R.anim.pulse)
                holder.icon.postDelayed({ UiBus.get().post(ListDirectoryRequestedEvent(file)) }, 200L)
                holder.icon.startAnimation(pulseAnim)
            } else {
                UiBus.get().post(OpenFileRequestedEvent(file))
            }
        }

        holder.itemRoot.setOnLongClickListener {
            if (selectedItems.contains(file)) {
                selectedItems.remove(file)
            } else {
                selectedItems.add(file)
            }
            notifyItemChanged(position)
            UiBus.get().post(SelectedFilesChangedEvent(selectedItems))
            true
        }
    }

    override fun getItemCount(): Int = files.size

    inner class ViewHolder(val itemRoot: android.view.View) : RecyclerView.ViewHolder(itemRoot) {
        val icon: ImageView = itemRoot.findViewById(R.id.icon) as ImageView
        val fileName: TextView = itemRoot.findViewById(R.id.fileName) as TextView
        val date: TextView = itemRoot.findViewById(R.id.date) as TextView
        val size: TextView = itemRoot.findViewById(R.id.size) as TextView
    }
}
