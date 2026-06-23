package com.rafel.bgt.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class FridayViewModel : ViewModel() {
    var selectedTab by mutableIntStateOf(0)
    var lifePoints by mutableIntStateOf(20)
    var round by mutableIntStateOf(1)
    var hazardStrength by mutableIntStateOf(0)
    var difficulty by mutableStateOf("Normal")

    fun reset() {
        lifePoints = 20
        round = 1
        hazardStrength = 0
    }
}
