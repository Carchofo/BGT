package com.rafel.bgt.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class WingspanDifficulty(val label: String, val pointsPerFaceDownCard: Int) {
    EAGLET("Eaglet", 3),
    EAGLE("Eagle", 4),
    EAGLE_EYED("Eagle-eyed Eagle", 5)
}

/**
 * Tracker del automa oficial de Wingspan (Automa Factory). No reimplementa el
 * mazo de 10 cartas del automa (eso lo lee el jugador de sus cartas físicas) —
 * solo lleva la cuenta de lo que el automa acumula según la acción que salga,
 * siguiendo el manual oficial.
 */
class WingspanViewModel : ViewModel() {

    var setup by mutableStateOf(true)
    var selectedTab by mutableIntStateOf(0)
    var difficulty by mutableStateOf(WingspanDifficulty.EAGLE)

    var round by mutableIntStateOf(1)

    // Estado acumulado del automa
    var automaEggs by mutableIntStateOf(0)
    var automaFaceDownCards by mutableIntStateOf(0)
    var automaPlayedBirdsValue by mutableIntStateOf(0)
    var automaFoodTokens by mutableIntStateOf(2) // el automa parte con 2 fichas de comida (regla de desempate)

    // Puntuación de objetivo de ronda, introducida por el jugador tras comparar
    // la posición del cubo del automa contra la baldosa física de objetivo
    var roundGoalScores by mutableStateOf(listOf(0, 0, 0, 0))

    val finalScore: Int
        get() = automaEggs +
                (automaFaceDownCards * difficulty.pointsPerFaceDownCard) +
                automaPlayedBirdsValue +
                roundGoalScores.sum()

    fun nextRound() {
        if (round < 4) round++
    }

    fun setRoundGoalScore(roundIndex: Int, value: Int) {
        if (roundIndex !in 0..3) return
        roundGoalScores = roundGoalScores.toMutableList().also { it[roundIndex] = value }
    }

    fun reset() {
        setup = true
        selectedTab = 0
        difficulty = WingspanDifficulty.EAGLE
        round = 1
        automaEggs = 0
        automaFaceDownCards = 0
        automaPlayedBirdsValue = 0
        automaFoodTokens = 2
        roundGoalScores = listOf(0, 0, 0, 0)
    }
}
