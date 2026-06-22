package com.rafel.bgt.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MaracaiboViewModel : ViewModel() {

    var setup by mutableStateOf(true)
    var selectedTab by mutableIntStateOf(0)
    var bCards by mutableIntStateOf(0)
    var withExp by mutableStateOf(false)

    // Jordan counters
    var jordanMarker by mutableIntStateOf(0)
    var jordanUpgrades by mutableIntStateOf(0)
    var jordanMissions by mutableIntStateOf(1)
    var jordanResidences by mutableIntStateOf(0)
    var jordanFigurehead by mutableStateOf(false)
    var jordanRivers by mutableIntStateOf(0)
    var jordanTreasures by mutableIntStateOf(0)
    var jordanVP by mutableIntStateOf(0)
    var round by mutableIntStateOf(1)
    var jordanInGulf by mutableStateOf(false)

    // UI state
    var showPanel by mutableStateOf(false)
    var showUpgrade by mutableStateOf(false)

    // Scoring
    var yourRivers by mutableIntStateOf(0)
    var yourDoubloons by mutableIntStateOf(0)
    var yourUpgradeVP by mutableIntStateOf(0)
    var yourResidenceVP by mutableIntStateOf(0)
    var yourTreasureVP by mutableIntStateOf(0)
    var yourBuriedTreasures by mutableIntStateOf(0)
    var yourMissionVP by mutableIntStateOf(0)
    var yourMissionBonus by mutableStateOf(false)

    fun reset() {
        setup = true
        selectedTab = 0
        bCards = 0
        withExp = false
        jordanMarker = 0
        jordanUpgrades = 0
        jordanMissions = 1
        jordanResidences = 0
        jordanFigurehead = false
        jordanRivers = 0
        jordanTreasures = 0
        jordanVP = 0
        round = 1
        jordanInGulf = false
        showPanel = false
        showUpgrade = false
        yourRivers = 0
        yourDoubloons = 0
        yourUpgradeVP = 0
        yourResidenceVP = 0
        yourTreasureVP = 0
        yourBuriedTreasures = 0
        yourMissionVP = 0
        yourMissionBonus = false
    }
}
