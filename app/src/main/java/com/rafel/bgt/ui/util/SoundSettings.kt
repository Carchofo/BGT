package com.rafel.bgt.ui.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Global singleton para el estado de mute de la app.
 * Se inicializa una vez desde MainActivity y persiste con SharedPreferences.
 */
object SoundSettings {

    private const val PREFS_NAME = "tbg_prefs"
    private const val KEY_MUTED  = "sound_muted"

    var isMuted = mutableStateOf(false)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isMuted.value = prefs.getBoolean(KEY_MUTED, false)
    }

    fun toggle(context: Context) {
        isMuted.value = !isMuted.value
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_MUTED, isMuted.value).apply()
    }
}
