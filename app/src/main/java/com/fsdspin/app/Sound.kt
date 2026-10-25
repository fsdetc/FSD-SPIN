package com.fsdspin.app

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Tick + win sounds for the wheel.
 * Sounds are short WAV files in res/raw (tick.wav, win.wav) generated
 * for this project — replace them with your own files of the same name
 * to change the sound without touching code.
 */
class WheelSounds(context: Context) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val tickId: Int = pool.load(context, R.raw.tick, 1)
    private val winId: Int = pool.load(context, R.raw.win, 1)
    var enabled: Boolean = true

    fun playTick() {
        if (enabled) pool.play(tickId, 1f, 1f, 0, 0, 1f)
    }

    fun playWin() {
        if (enabled) pool.play(winId, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}
