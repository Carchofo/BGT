package com.rafel.spooktacular.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

private val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")

class DisclaimerViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.appDataStore

    /**
     * null  → aún cargando desde DataStore
     * false → usuario no ha aceptado todavía
     * true  → usuario ya aceptó (no mostrar disclaimer)
     */
    val isAccepted: StateFlow<Boolean?> = dataStore.data
        .map { prefs -> prefs[DISCLAIMER_ACCEPTED] ?: false }   // clave ausente → false (no aceptado)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun acceptDisclaimer() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[DISCLAIMER_ACCEPTED] = true
            }
        }
    }
}
