package com.rafel.bgt.ui.util

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Wraps any IndicationNodeFactory (e.g. Material3 ripple) and plays a tap
 * sound on every press. Provided via LocalIndication in BGTTheme so
 * every Button/IconButton/clickable in the app gets the sound automatically.
 */
class SoundIndication(
    private val wrapped: IndicationNodeFactory,
    private val player: TapSoundPlayer
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        SoundNode(wrapped.create(interactionSource), interactionSource, player)

    override fun hashCode() = System.identityHashCode(this)
    override fun equals(other: Any?) = this === other
}

private class SoundNode(
    wrappedNode: DelegatableNode,
    private val interactionSource: InteractionSource,
    private val player: TapSoundPlayer
) : DelegatingNode() {

    init {
        delegate(wrappedNode)
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions
                .filterIsInstance<PressInteraction.Press>()
                .collect { player.playTap() }
        }
    }
}
