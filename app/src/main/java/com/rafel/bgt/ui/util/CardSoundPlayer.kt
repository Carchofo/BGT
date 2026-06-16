package com.rafel.bgt.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.rafel.bgt.R

/**
 * Reproductor ligero de sonidos de cartas usando SoundPool.
 * Crea una instancia y llama a release() cuando ya no se necesite.
 */
class CardSoundPlayer(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundDeal   = soundPool.load(context, R.raw.card_deal,    1)
    private val soundDeal2  = soundPool.load(context, R.raw.card_deal2,   1)
    private val soundFlip   = soundPool.load(context, R.raw.card_flip,    1)
    private val soundShuffle= soundPool.load(context, R.raw.card_shuffle, 1)

    /** Sonido al repartir / revelar una carta (alterna entre los dos) */
    private var dealToggle = false
    fun playDeal() {
        if (SoundSettings.isMuted.value) return
        dealToggle = !dealToggle
        val id = if (dealToggle) soundDeal else soundDeal2
        soundPool.play(id, 0.9f, 0.9f, 1, 0, 1f)
    }

    /** Sonido al voltear una carta */
    fun playFlip() {
        if (!SoundSettings.isMuted.value) soundPool.play(soundFlip, 0.85f, 0.85f, 1, 0, 1f)
    }

    /** Sonido al barajar al inicio del turno */
    fun playShuffle() {
        if (!SoundSettings.isMuted.value) soundPool.play(soundShuffle, 0.85f, 0.85f, 1, 0, 1f)
    }

    fun release() = soundPool.release()
}
