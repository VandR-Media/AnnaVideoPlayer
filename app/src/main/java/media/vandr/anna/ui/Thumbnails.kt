package media.vandr.anna.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object Thumbnails {
    private val cache = object : LruCache<String, Bitmap>(20 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
    }

    suspend fun load(context: Context, uri: Uri): Bitmap? {
        val key = uri.toString()
        cache.get(key)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.loadThumbnail(uri, Size(320, 180), null)
            }.getOrNull()?.also { cache.put(key, it) }
        }
    }
}

@Composable
fun VideoCover(uri: Uri, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bitmap by produceState<Bitmap?>(null, uri) {
        value = Thumbnails.load(context.applicationContext, uri)
    }
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(Color(0xFF322855), Color(0xFF141C32)))
        ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap == null) {
            Icon(
                Icons.Filled.Movie,
                contentDescription = null,
                tint = Color(0xFF9F8ACA),
                modifier = Modifier.size(36.dp)
            )
        } else {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Превью видео",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
