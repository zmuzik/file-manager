package zmuzik.filemanager.common

import java.text.SimpleDateFormat
import java.util.*


val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

fun formatDate(date: Date) = dateFormat.format(date)

fun formatDate(timestamp: Long) = formatDate(Date(timestamp))

fun formatFileSize(bytes: Long): String {
    val unit = 1024
    if (bytes < unit) return bytes.toString() + " B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
    val pre = "kMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
}

fun fileExt(fileName: String): String {
    val lastDot = fileName.lastIndexOf('.')
    return when {
        (lastDot > 0) -> fileName.substring(lastDot + 1)
        else -> ""
    }
}