package com.bridge.yandexbyd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslitTest {

    @Test
    fun hasCyrillic_detectsRussian() {
        assertTrue(Translit.hasCyrillic("Сурганова"))
        assertFalse(Translit.hasCyrillic("Surganova 12"))
    }

    @Test
    fun transliterate_leavesLatinUnchanged() {
        assertEquals("TEST 300 m", Translit.transliterate("TEST 300 m"))
    }

    @Test
    fun transliterate_convertsStreetName() {
        assertEquals("Surganova", Translit.transliterate("Сурганова"))
    }

    @Test
    fun transliterate_handlesMixedCase() {
        assertEquals("Shosse", Translit.transliterate("Шоссе"))
    }
}
