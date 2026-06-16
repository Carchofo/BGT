package com.rafel.bgt.ui.theme

import android.app.Activity
import androidx.compose.foundation.IndicationNodeFactory
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
import com.rafel.bgt.ui.util.SoundIndication
import com.rafel.bgt.ui.util.TapSoundPlayer

private val BGTColorScheme = darkColorScheme(
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
fun BGTTheme(content: @Composable () -> Unit) {
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

    val baseRipple = ripple() as IndicationNodeFactory
    val soundIndication = remember(baseRipple, tapPlayer) {
        SoundIndication(baseRipple, tapPlayer)
    }

    CompositionLocalProvider(LocalIndication provides soundIndication) {
        MaterialTheme(
            colorScheme = BGTColorScheme,
            typography  = Typography,
            content     = content
        )
    }
}
