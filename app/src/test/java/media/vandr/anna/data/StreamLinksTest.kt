package media.vandr.anna.data

import org.junit.Assert.*
import org.junit.Test

class StreamLinksTest {
    @Test fun acceptsCommonStreamProtocols() {
        assertTrue(StreamLinks.supported("https://example.org/movie.m3u8?token=123"))
        assertTrue(StreamLinks.supported("http://example.org/video.mp4"))
        assertTrue(StreamLinks.supported("rtsp://example.org/live"))
    }

    @Test fun rejectsUnsafeOrMalformedLinks() {
        assertFalse(StreamLinks.supported("file:///sdcard/secret.mp4"))
        assertFalse(StreamLinks.supported("javascript:alert(1)"))
        assertFalse(StreamLinks.supported("example.org/video.mp4"))
        assertFalse(StreamLinks.supported(""))
    }

    @Test fun folderNameIsStable() {
        assertEquals("Movies/Family", FolderNames.display("Movies/Family/"))
        assertEquals("Без папки", FolderNames.display("/"))
        assertEquals("Без папки", FolderNames.display(null))
    }
}
