package com.bridge.yandexbyd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmapIconMapperTest {

    @Test
    fun fromText_turnLeftRussian() {
        assertEquals(AmapIconMapper.LEFT, AmapIconMapper.fromText("Поверните налево"))
    }

    @Test
    fun fromText_turnRightEnglish() {
        assertEquals(AmapIconMapper.RIGHT, AmapIconMapper.fromText("Turn right onto Main St"))
    }

    @Test
    fun fromText_uturnBeforeGenericTurn() {
        assertEquals(AmapIconMapper.UTURN, AmapIconMapper.fromText("Сделайте разворот"))
    }

    @Test
    fun fromText_roundabout() {
        assertEquals(AmapIconMapper.ROUNDABOUT, AmapIconMapper.fromText("На кольце съезжайте"))
    }

    @Test
    fun fromText_destination() {
        assertEquals(AmapIconMapper.DEST, AmapIconMapper.fromText("Вы прибыли"))
    }

    @Test
    fun fromText_unknownReturnsNull() {
        assertNull(AmapIconMapper.fromText(""))
    }
}
