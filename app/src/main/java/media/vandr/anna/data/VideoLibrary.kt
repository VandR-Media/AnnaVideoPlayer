package media.vandr.anna.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

data class VideoItem(
    val uri: Uri,
    val title: String,
    val relativePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAdded: Long
) {
    val folder: String get() = FolderNames.display(relativePath)
}

object FolderNames {
    fun display(relativePath: String?): String = relativePath
        ?.trim('/')
        ?.takeIf { it.isNotBlank() }
        ?: "Без папки"
}

enum class VideoOrder(val label: String) {
    NEWEST("Сначала новые"), NAME("По названию"), DURATION("По длительности"), SIZE("По размеру")
}

object LibrarySorting {
    fun sort(items: List<VideoItem>, order: VideoOrder): List<VideoItem> = when (order) {
        VideoOrder.NEWEST -> items.sortedByDescending { it.dateAdded }
        VideoOrder.NAME -> items.sortedBy { it.title.lowercase() }
        VideoOrder.DURATION -> items.sortedByDescending { it.durationMs }
        VideoOrder.SIZE -> items.sortedByDescending { it.sizeBytes }
    }
}

class VideoLibrary(private val context: Context) {
    fun scan(): List<VideoItem> {
        val columns = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.RELATIVE_PATH,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED
        )
        val result = ArrayList<VideoItem>()
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        context.contentResolver.query(
            collection, columns, null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val addedIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                result.add(
                    VideoItem(
                        uri = ContentUris.withAppendedId(collection, id),
                        title = cursor.getString(titleIndex) ?: "Видео $id",
                        relativePath = cursor.getString(pathIndex) ?: "",
                        durationMs = cursor.getLong(durationIndex),
                        sizeBytes = cursor.getLong(sizeIndex),
                        dateAdded = cursor.getLong(addedIndex)
                    )
                )
            }
        }
        return result
    }
}
