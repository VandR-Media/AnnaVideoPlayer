package media.vandr.anna.data

import java.net.URI

object StreamLinks {
    fun supported(url: String): Boolean = try {
        val uri = URI(url.trim())
        uri.scheme?.lowercase() in setOf("https", "rtsp") && !uri.host.isNullOrBlank()
    } catch (_: Exception) {
        false
    }
}
