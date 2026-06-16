package com.rafel.bgt.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.rafel.bgt.R

class TapSoundPlayer(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundTap = soundPool.load(context, R.raw.tap, 1)

    fun playTap() {
        if (!SoundSettings.isMuted.value) soundPool.play(soundTap, 0.7f, 0.7f, 0, 0, 1f)
    }

    fun release() = soundPool.release()
}
