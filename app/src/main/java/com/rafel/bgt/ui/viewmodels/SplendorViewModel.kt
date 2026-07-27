package com.rafel.bgt.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// ── Tipos de acción del automa ─────────────────────────────────────
sealed class AutomaAction {
    data class TakeGems(val gems: List<Int>) : AutomaAction()
    data class BuyCard(val row: Char, val col: Int, val discard: Int, val isSpecialTurn: Boolean = false) : AutomaAction()
}

data class AutomaCard(
    val phase1: AutomaAction,
    val phase2: AutomaAction,
    val phase3: AutomaAction,
    val isSpecial: Boolean = false
)

// ── Mazos del automa ───────────────────────────────────────────────
// Gemas: 1=Blanca 2=Azul 3=Verde 4=Roja 5=Negra
// Filas: A=Nivel3, B=Nivel2, C=Nivel1

private val NORMAL_BASE: List<AutomaCard> = listOf(
    AutomaCard(AutomaAction.TakeGems(listOf(1,2)),        AutomaAction.TakeGems(listOf(5,1,4)),   AutomaAction.BuyCard('A',4,3,true)),
    AutomaCard(AutomaAction.BuyCard('C',1,3),             AutomaAction.BuyCard('C',3,3),          AutomaAction.TakeGems(listOf(2,4,3))),
    AutomaCard(AutomaAction.TakeGems(listOf(5,4)),        AutomaAction.BuyCard('C',4,3),          AutomaAction.TakeGems(listOf(3,2,4))),
    AutomaCard(AutomaAction.TakeGems(listOf(3,2)),        AutomaAction.BuyCard('C',2,3),          AutomaAction.BuyCard('B',1,2,true)),
    AutomaCard(AutomaAction.TakeGems(listOf(2,5,1)),      AutomaAction.BuyCard('C',4,3),          AutomaAction.TakeGems(listOf(4,3,5))),
    AutomaCard(AutomaAction.TakeGems(listOf(4,3,2)),      AutomaAction.BuyCard('C',3,3),          AutomaAction.BuyCard('B',4,2)),
    AutomaCard(AutomaAction.BuyCard('C',3,3),             AutomaAction.TakeGems(listOf(3,4)),     AutomaAction.TakeGems(listOf(1,5,2))),
    AutomaCard(AutomaAction.BuyCard('C',4,3),             AutomaAction.TakeGems(listOf(2,5,1)),   AutomaAction.BuyCard('C',2,3)),
)
private val NORMAL_SPECIALS: List<AutomaCard> = listOf(
    AutomaCard(AutomaAction.BuyCard('B',2,3),             AutomaAction.BuyCard('B',3,3),          AutomaAction.BuyCard('A',2,3),  true),
    AutomaCard(AutomaAction.TakeGems(listOf(1,3,5)),      AutomaAction.BuyCard('B',1,3),          AutomaAction.BuyCard('A',3,3),  true),
    AutomaCard(AutomaAction.BuyCard('C',1,3),             AutomaAction.BuyCard('B',4,3),          AutomaAction.BuyCard('A',1,3),  true),
    AutomaCard(AutomaAction.TakeGems(listOf(2,4,1)),      AutomaAction.TakeGems(listOf(3,5,2)),   AutomaAction.BuyCard('A',4,2),  true),
)

private val HARD_BASE: List<AutomaCard> = listOf(
    AutomaCard(AutomaAction.BuyCard('C',2,2),             AutomaAction.BuyCard('B',2,2),          AutomaAction.BuyCard('A',2,2)),
    AutomaCard(AutomaAction.TakeGems(listOf(1,2,3)),      AutomaAction.BuyCard('B',1,2),          AutomaAction.BuyCard('A',1,2)),
    AutomaCard(AutomaAction.BuyCard('C',3,2),             AutomaAction.BuyCard('B',3,2),          AutomaAction.BuyCard('A',3,2)),
    AutomaCard(AutomaAction.TakeGems(listOf(4,5)),        AutomaAction.BuyCard('B',4,2),          AutomaAction.BuyCard('A',4,2)),
    AutomaCard(AutomaAction.TakeGems(listOf(1,3,5)),      AutomaAction.BuyCard('C',1,2),          AutomaAction.BuyCard('B',2,2)),
    AutomaCard(AutomaAction.BuyCard('C',4,2),             AutomaAction.BuyCard('B',3,2),          AutomaAction.BuyCard('A',1,2)),
    AutomaCard(AutomaAction.TakeGems(listOf(2,4,1)),      AutomaAction.TakeGems(listOf(3,5,4)),   AutomaAction.BuyCard('B',1,2)),
    AutomaCard(AutomaAction.BuyCard('C',1,2),             AutomaAction.BuyCard('B',2,2),          AutomaAction.BuyCard('A',3,2)),
)
private val HARD_SPECIALS: List<AutomaCard> = listOf(
    AutomaCard(AutomaAction.BuyCard('B',1,2),             AutomaAction.BuyCard('A',1,2),          AutomaAction.BuyCard('A',2,1),  true),
    AutomaCard(AutomaAction.BuyCard('B',4,2),             AutomaAction.BuyCard('A',4,2),          AutomaAction.BuyCard('A',3,1),  true),
    AutomaCard(AutomaAction.TakeGems(listOf(2,3,4)),      AutomaAction.BuyCard('B',2,2),          AutomaAction.BuyCard('A',4,1),  true),
    AutomaCard(AutomaAction.BuyCard('C',2,2),             AutomaAction.BuyCard('A',2,2),          AutomaAction.BuyCard('A',1,1),  true),
)

class SplendorViewModel : ViewModel() {

    // ── Config ────────────────────────────────────────────────────
    var selectedTab   by mutableIntStateOf(0)
    var hardMode      by mutableStateOf(false)
    var gameStarted   by mutableStateOf(false)

    // ── Estado partida ────────────────────────────────────────────
    var turn          by mutableIntStateOf(1)
    var automaVP      by mutableIntStateOf(0)
    var automaTokens  by mutableIntStateOf(0)
    var playerVP      by mutableIntStateOf(0)

    private var deck: List<AutomaCard> = emptyList()
    private var cardIndex by mutableIntStateOf(0)

    val phase get() = when {
        turn <= 10 -> 0
        turn <= 20 -> 1
        else       -> 2
    }

    val currentCard get() = deck.getOrNull(cardIndex % 10)
    val currentAction get() = currentCard?.let { card ->
        when (phase) { 0 -> card.phase1; 1 -> card.phase2; else -> card.phase3 }
    }

    val isOver   get() = turn > 30 || automaVP >= 15 || playerVP >= 15
    val playerWins get() = playerVP >= 15 && playerVP > automaVP
    val automaWins get() = automaVP >= 15 && automaVP >= playerVP

    fun startGame() {
        val base     = if (hardMode) HARD_BASE     else NORMAL_BASE
        val specials = if (hardMode) HARD_SPECIALS else NORMAL_SPECIALS
        deck = (base + specials.shuffled().take(2)).shuffled()
        turn = 1; cardIndex = 0; automaVP = 0; automaTokens = 0; playerVP = 0
        gameStarted = true
    }

    fun nextTurn() {
        if (turn >= 30) return
        cardIndex++
        if (cardIndex % 10 == 0) deck = deck.shuffled()
        turn++
    }

    fun reset() { gameStarted = false; hardMode = false }
}
