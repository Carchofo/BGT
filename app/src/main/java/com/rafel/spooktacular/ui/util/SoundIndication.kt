package com.rafel.spooktacular.ui.util

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.drawscope.ContentDrawScope

/**
 * Wraps any Indication (e.g. the Material3 ripple) and plays a tap
 * sound on every press. Provide this via LocalIndication in the theme
 * and every clickable/button in the app gets the sound for free.
 */
class SoundIndication(
    private val wrapped: Indication,
    private val player: TapSoundPlayer
) : Indication {

    private inner class SoundInstance(
        private val wrappedInstance: IndicationInstance
    ) : IndicationInstance {
        override fun ContentDrawScope.drawIndication() {
            with(wrappedInstance) { drawIndication() }
        }
    }

    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource): IndicationInstance {
        val isPressed by interactionSource.collectIsPressedAsState()
        LaunchedEffect(isPressed) {
            if (isPressed) player.playTap()
        }
        val wrappedInstance = wrapped.rememberUpdatedInstance(interactionSource)
        return SoundInstance(wrappedInstance)
    }
}
