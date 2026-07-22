package com.rafel.bgt.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.rafel.bgt.ui.theme.BloodRed
import com.rafel.bgt.ui.theme.GhostWhite
import com.rafel.bgt.ui.theme.HalloweenOrange

private val CoiAmber = Color(0xFFD4900A)
private val CoiGold  = Color(0xFFF5B800)

class CoimbraViewModel : ViewModel() {

    var selectedTab by mutableIntStateOf(0)
    // null = usar la variante marcada isDefault en automa-variants.json
    var selectedVariantId by mutableStateOf<String?>(null)
    var round by mutableIntStateOf(1)

    // Puntuación por categorías
    var ptCartas     by mutableIntStateOf(0)
    var ptPeregrin    by mutableIntStateOf(0)
    var ptInfluencia  by mutableIntStateOf(0)
    var ptMonedas     by mutableIntStateOf(0)
    var ptFavores     by mutableIntStateOf(0)
    var ptOtros       by mutableIntStateOf(0)

    val total get() = ptCartas + ptPeregrin + ptInfluencia + ptMonedas + ptFavores + ptOtros

    val ratingIndex get() = when {
        total < 130 -> 0
        total < 160 -> 1
        total < 190 -> 2
        total < 220 -> 3
        else        -> 4
    }

    val ratingColor get() = when {
        total < 130 -> BloodRed
        total < 160 -> GhostWhite
        total < 190 -> CoiAmber
        total < 220 -> CoiGold
        else        -> HalloweenOrange
    }
}
