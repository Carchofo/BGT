package com.rafel.bgt.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class FridayViewModel : ViewModel() {

    var selectedTab by mutableIntStateOf(0)
    var difficulty by mutableStateOf("Normal")

    // Estado de partida
    var lifePoints by mutableIntStateOf(20)
    // 0=Verde, 1=Amarillo, 2=Rojo, 3=Piratas
    var phase by mutableIntStateOf(0)

    // Helper de combate
    var freeCards by mutableIntStateOf(0)       // cartas gratis que da el peligro
    var robinsonStrength by mutableIntStateOf(0) // fuerza acumulada de Robinson
    var hazardTarget by mutableIntStateOf(0)     // fuerza requerida por el peligro
    var lifeSpentThisFight by mutableIntStateOf(0) // vida gastada en cartas extra este combate

    val phaseLabel get() = when (phase) {
        0 -> "🟢 Fase Verde"
        1 -> "🟡 Fase Amarilla"
        2 -> "🔴 Fase Roja"
        else -> "☠️ Piratas"
    }

    val strengthDiff get() = robinsonStrength - hazardTarget
    val isWinning get() = strengthDiff >= 0

    fun startingLife() = when (difficulty) {
        "Fácil" -> 22
        "Difícil" -> 18
        else -> 20
    }

    fun nextPhase() { if (phase < 3) phase++ }
    fun prevPhase() { if (phase > 0) phase-- }

    fun spendLifeForCard() {
        if (lifePoints > 0) {
            lifePoints--
            lifeSpentThisFight++
        }
    }

    fun acceptDefeat() {
        // Pagar diferencia de fuerza con vida
        val cost = if (strengthDiff < 0) -strengthDiff else 0
        lifePoints = (lifePoints - cost).coerceAtLeast(0)
        resetFight()
    }

    fun resetFight() {
        freeCards = 0
        robinsonStrength = 0
        hazardTarget = 0
        lifeSpentThisFight = 0
    }

    fun reset() {
        lifePoints = startingLife()
        phase = 0
        resetFight()
    }
}
