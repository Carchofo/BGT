package com.rafel.bgt.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rafel.bgt.ui.screens.CastleComboState

class CastleComboViewModel : ViewModel() {
    var selectedTab by mutableIntStateOf(0)
    val state = CastleComboState()
}
