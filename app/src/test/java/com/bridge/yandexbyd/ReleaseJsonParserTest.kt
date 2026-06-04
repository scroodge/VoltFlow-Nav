package com.bridge.yandexbyd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseJsonParserTest {

    @Test
    fun isVersionNewer_comparesSemver() {
        assertTrue(ReleaseJsonParser.isVersionNewer("1.0.1", "1.0.0"))
        assertFalse(ReleaseJsonParser.isVersionNewer("1.0.0", "1.0.0"))
        assertFalse(ReleaseJsonParser.isVersionNewer("0.9.9", "1.0.0"))
        assertTrue(ReleaseJsonParser.isVersionNewer("2.0", "1.9.9"))
    }

    @Test
    fun parseReleaseJson_returnsUpdateWhenNewer() {
        val body = """
            {
              "tag_name": "v1.0.1",
              "body": "Bug fixes",
              "assets": [
                {"name": "notes.txt", "browser_download_url": "https://example.com/notes.txt"},
                {"name": "VoltFlowNav-v1.0.1.apk", "browser_download_url": "https://example.com/app.apk"}
              ]
            }
        """.trimIndent()

        val info = ReleaseJsonParser.parseReleaseJson(body, "1.0.0")
        assertEquals("1.0.1", info?.version)
        assertEquals("https://example.com/app.apk", info?.downloadUrl)
        assertEquals("Bug fixes", info?.releaseNotes)
    }

    @Test
    fun parseReleaseJson_returnsNullWhenSameVersion() {
        val body = """{"tag_name":"v1.0.0","assets":[{"name":"a.apk","browser_download_url":"https://x/a.apk"}]}"""
        assertNull(ReleaseJsonParser.parseReleaseJson(body, "1.0.0"))
    }
}
