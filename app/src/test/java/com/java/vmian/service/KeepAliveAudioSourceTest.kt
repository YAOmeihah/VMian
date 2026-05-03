package com.java.vmian.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeepAliveAudioSourceTest {

    @Test
    fun silentWav_hasValidPcmWaveHeaderAndSilencePayload() {
        val wav = KeepAliveAudioSource.createSilentWav()

        assertEquals('R'.code.toByte(), wav[0])
        assertEquals('I'.code.toByte(), wav[1])
        assertEquals('F'.code.toByte(), wav[2])
        assertEquals('F'.code.toByte(), wav[3])
        assertEquals('W'.code.toByte(), wav[8])
        assertEquals('A'.code.toByte(), wav[9])
        assertEquals('V'.code.toByte(), wav[10])
        assertEquals('E'.code.toByte(), wav[11])
        assertEquals('f'.code.toByte(), wav[12])
        assertEquals('d'.code.toByte(), wav[36])
        assertEquals('a'.code.toByte(), wav[37])
        assertEquals('t'.code.toByte(), wav[38])
        assertEquals('a'.code.toByte(), wav[39])

        assertEquals(44 + KeepAliveAudioSource.PCM_DATA_SIZE_BYTES, wav.size)
        assertTrue(wav.drop(44).all { it == 0.toByte() })
    }
}
