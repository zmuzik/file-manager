package zmuzik.filemanager.main

import android.content.Context
import android.content.res.Configuration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import zmuzik.filemanager.R
import java.io.File

open class FilesView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                               defStyleAttr: Int = 0) :
        RecyclerView(context, attrs, defStyleAttr) {

    val horiz = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = resources.getInteger(R.integer.grid_columns)

    var files: List<File> = ArrayList()

    override fun onFinishInflate() {
        super.onFinishInflate()
        setHasFixedSize(true)
        layoutManager = if (horiz) GridLayoutManager(context, columns) else LinearLayoutManager(context)
    }

    fun setData(newFiles: List<File>) {
        files = newFiles
        adapter = FileListAdapter(context, files, horiz)
    }

    fun clearSelectedFiles() {
        (adapter as FileListAdapter?)?.selectedItems?.clear()
        (adapter as FileListAdapter?)?.notifyDataSetChanged()
    }
}