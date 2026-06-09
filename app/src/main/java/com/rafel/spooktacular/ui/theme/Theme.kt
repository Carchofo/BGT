package com.rafel.spooktacular.ui.theme

import android.app.Activity
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rafel.spooktacular.ui.util.SoundIndication
import com.rafel.spooktacular.ui.util.TapSoundPlayer

private val SpooktacularColorScheme = darkColorScheme(
    primary             = HalloweenOrange,
    onPrimary           = NightBlack,
    primaryContainer    = HalloweenOrangeDark,
    onPrimaryContainer  = PumpkinYellow,
    secondary           = SpookyPurple,
    onSecondary         = GhostWhite,
    secondaryContainer  = SpookyPurpleLight,
    onSecondaryContainer= GhostWhite,
    background          = MidnightBlue,
    onBackground        = GhostWhite,
    surface             = CardBackground,
    onSurface           = GhostWhite,
    surfaceVariant      = CardBorder,
    onSurfaceVariant    = GraveGray,
    error               = BloodRed,
    onError             = GhostWhite,
)

@Composable
fun SpooktacularTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MidnightBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val context = LocalContext.current
    val tapPlayer = remember { TapSoundPlayer(context) }
    DisposableEffect(Unit) { onDispose { tapPlayer.release() } }

    @Suppress("DEPRECATION")
    val baseRipple = ripple()
    val soundIndication = remember(baseRipple, tapPlayer) {
        SoundIndication(baseRipple, tapPlayer)
    }

    CompositionLocalProvider(LocalIndication provides soundIndication) {
        MaterialTheme(
            colorScheme = SpooktacularColorScheme,
            typography  = Typography,
            content     = content
        )
    }
}
