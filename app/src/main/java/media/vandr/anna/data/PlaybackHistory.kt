package media.vandr.anna.data

import android.content.Context
import java.security.MessageDigest

class PlaybackHistory(context: Context) {
    private val prefs = context.getSharedPreferences("anna_library", Context.MODE_PRIVATE)

    private fun key(uri: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(uri.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isFavorite(uri: String): Boolean = prefs.getBoolean("favorite_${key(uri)}", false)

    fun toggleFavorite(uri: String) {
        prefs.edit().putBoolean("favorite_${key(uri)}", !isFavorite(uri)).apply()
    }

    fun position(uri: String): Long = prefs.getLong("position_${key(uri)}", 0L)
    fun setPosition(uri: String, positionMs: Long) {
        prefs.edit().putLong("position_${key(uri)}", positionMs.coerceAtLeast(0L)).apply()
    }
    fun clearPosition(uri: String) = prefs.edit().remove("position_${key(uri)}").apply()

    fun recordVisit(uri: String) {
        val entries = visits().toMutableList()
        entries.remove(uri)
        entries.add(0, uri)
        prefs.edit().putString("recent", entries.take(100).joinToString("\n")).apply()
    }

    fun visits(): List<String> = prefs.getString("recent", "").orEmpty()
        .lineSequence().filter { it.isNotBlank() }.toList()
}
