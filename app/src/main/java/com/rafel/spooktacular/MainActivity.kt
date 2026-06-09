package com.rafel.spooktacular

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafel.spooktacular.ui.screens.CascadiaScoreScreen
import com.rafel.spooktacular.ui.screens.CastleComboSoloScreen
import com.rafel.spooktacular.ui.screens.CoimbraSoloScreen
import com.rafel.spooktacular.ui.screens.CMSoloModeScreen
import com.rafel.spooktacular.ui.screens.TiletumSoloScreen
import com.rafel.spooktacular.ui.screens.MaracaiboSoloScreen
import com.rafel.spooktacular.ui.screens.HomeScreen
import com.rafel.spooktacular.ui.screens.MonstersScreen
import com.rafel.spooktacular.ui.screens.ScoreBoardScreen
import com.rafel.spooktacular.ui.screens.SoloModeScreen
import com.rafel.spooktacular.ui.theme.GhostWhite
import com.rafel.spooktacular.ui.theme.HalloweenOrange
import com.rafel.spooktacular.ui.theme.SpooktacularTheme
import com.rafel.spooktacular.ui.util.SoundSettings

private const val PREFS_NAME = "tbg_prefs"
private const val KEY_DISCLAIMER = "disclaimer_accepted"

private val DISCLAIMER_SECTIONS = listOf(
    "🎮  Aplicación independiente" to
        "TBG – Tools Board Games no está afiliada ni respaldada por Board&Dice, AEG, Capstone Games, Samaruc Games, Bad Comet, Plan B Games ni Blue Orange Games.",
    "©  Propiedad intelectual" to
        "Los nombres de los juegos y sus logotipos son propiedad de sus respectivas editoriales. Su mención es únicamente identificativa.",
    "🃏  Requiere el juego físico" to
        "Esta app es un complemento. Necesitas el juego físico para usarla. No incluye reglas completas ni contenido protegido por copyright.",
    "👥  Modos solitarios fan-made" to
        "Los modos solitarios están basados en variantes publicadas gratuitamente en BoardGameGeek por la comunidad. El crédito es de sus autores originales."
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SoundSettings.init(this)
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setContent {
            SpooktacularTheme {
                var showDisclaimer by remember {
                    mutableStateOf(!prefs.getBoolean(KEY_DISCLAIMER, false))
                }

                SpooktacularApp()

                if (showDisclaimer) {
                    DisclaimerDialog(
                        onAccept = {
                            prefs.edit().putBoolean(KEY_DISCLAIMER, true).apply()
                            showDisclaimer = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisclaimerDialog(onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* obligatorio aceptar */ },
        title = {
            Text(
                "TBG – Tools Board Games",
                fontWeight = FontWeight.Bold,
                color = GhostWhite
            )
        },
        text = {
            LazyColumn {
                items(DISCLAIMER_SECTIONS) { (title, body) ->
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        color = HalloweenOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        body,
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider(color = GhostWhite.copy(alpha = 0.1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = HalloweenOrange)
            ) {
                Text("Aceptar y Continuar", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = com.rafel.spooktacular.ui.theme.CardBackground
    )
}

@Composable
fun SpooktacularApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home")              { HomeScreen(onNavigate = { navController.navigate(it) }) }
        composable("solo_mode")         { SoloModeScreen(onBack = { navController.popBackStack() }) }
        composable("monsters")          { MonstersScreen(onBack = { navController.popBackStack() }) }
        composable("score_board")       { ScoreBoardScreen(onBack = { navController.popBackStack() }) }
        composable("cm_solo")           { CMSoloModeScreen(onBack = { navController.popBackStack() }) }
        composable("tiletum_solo")      { TiletumSoloScreen(onBack = { navController.popBackStack() }) }
        composable("maracaibo_solo")    { MaracaiboSoloScreen(onBack = { navController.popBackStack() }) }
        composable("cascadia_score")    { CascadiaScoreScreen(onBack = { navController.popBackStack() }) }
        composable("castle_combo_solo") { CastleComboSoloScreen(onBack = { navController.popBackStack() }) }
        composable("coimbra_solo")      { CoimbraSoloScreen(onBack = { navController.popBackStack() }) }
    }
}
