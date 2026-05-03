package com.java.vmian.service

/**
 * Creates a tiny PCM WAV stream for MediaPlayer-based keep-alive playback.
 */
object KeepAliveAudioSource {
    const val SAMPLE_RATE = 8_000
    const val CHANNEL_COUNT = 1
    const val BITS_PER_SAMPLE = 16
    const val DURATION_SECONDS = 1
    const val PCM_DATA_SIZE_BYTES = SAMPLE_RATE * CHANNEL_COUNT * (BITS_PER_SAMPLE / 8) * DURATION_SECONDS

    fun createSilentWav(): ByteArray {
        val headerSize = 44
        val wav = ByteArray(headerSize + PCM_DATA_SIZE_BYTES)
        var offset = 0

        offset = writeAscii(wav, offset, "RIFF")
        offset = writeIntLe(wav, offset, wav.size - 8)
        offset = writeAscii(wav, offset, "WAVE")
        offset = writeAscii(wav, offset, "fmt ")
        offset = writeIntLe(wav, offset, 16)
        offset = writeShortLe(wav, offset, 1)
        offset = writeShortLe(wav, offset, CHANNEL_COUNT)
        offset = writeIntLe(wav, offset, SAMPLE_RATE)
        offset = writeIntLe(wav, offset, SAMPLE_RATE * CHANNEL_COUNT * (BITS_PER_SAMPLE / 8))
        offset = writeShortLe(wav, offset, CHANNEL_COUNT * (BITS_PER_SAMPLE / 8))
        offset = writeShortLe(wav, offset, BITS_PER_SAMPLE)
        offset = writeAscii(wav, offset, "data")
        writeIntLe(wav, offset, PCM_DATA_SIZE_BYTES)

        return wav
    }

    private fun writeAscii(target: ByteArray, offset: Int, value: String): Int {
        value.forEachIndexed { index, char ->
            target[offset + index] = char.code.toByte()
        }
        return offset + value.length
    }

    private fun writeShortLe(target: ByteArray, offset: Int, value: Int): Int {
        target[offset] = (value and 0xff).toByte()
        target[offset + 1] = ((value shr 8) and 0xff).toByte()
        return offset + 2
    }

    private fun writeIntLe(target: ByteArray, offset: Int, value: Int): Int {
        target[offset] = (value and 0xff).toByte()
        target[offset + 1] = ((value shr 8) and 0xff).toByte()
        target[offset + 2] = ((value shr 16) and 0xff).toByte()
        target[offset + 3] = ((value shr 24) and 0xff).toByte()
        return offset + 4
    }
}
