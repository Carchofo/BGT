package com.rafel.bgt.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val FAVORITES_KEY = stringSetPreferencesKey("favorite_games")

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.appDataStore

    val favoriteIds: StateFlow<Set<String>> = dataStore.data
        .map { prefs -> prefs[FAVORITES_KEY] ?: emptySet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    fun toggleFavorite(gameId: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[FAVORITES_KEY] ?: emptySet()
                prefs[FAVORITES_KEY] = if (gameId in current) current - gameId else current + gameId
            }
        }
    }
}
