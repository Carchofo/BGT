package com.rafel.bgt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafel.bgt.ui.viewmodels.WingspanDifficulty
import com.rafel.bgt.ui.viewmodels.WingspanViewModel

private val ForestGreen = Color(0xFF1B3A2F)
private val ForestDeep  = Color(0xFF0D2018)
private val SkyBlue     = Color(0xFF6FA8DC)
private val EggCream    = Color(0xFFF0E6D2)
private val Ink         = Color(0xFFEDEDED)

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WingspanSoloScreen(onBack: () -> Unit, vm: WingspanViewModel = viewModel()) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Wingspan — Automa", color = Ink,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (vm.selectedTab != 0) vm.selectedTab = 0 else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Ink)
                    }
                },
                actions = {
                    if (vm.selectedTab == 0 && !vm.setup) {
                        Text("Ronda ${vm.round}/4", color = SkyBlue, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 16.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestDeep)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = ForestDeep, tonalElevation = 0.dp) {
                listOf(
                    Triple("Ajustes", Icons.Default.Settings, 0),
                    Triple("Automa", Icons.Default.SmartToy, 1),
                    Triple("Puntos", Icons.Default.EmojiEvents, 2),
                    Triple("Reglas", Icons.Default.MenuBook, 3)
                ).forEach { (label, icon, idx) ->
                    NavigationBarItem(
                        selected = vm.selectedTab == idx,
                        onClick  = { vm.selectedTab = idx },
                        icon  = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = SkyBlue,
                            selectedTextColor   = SkyBlue,
                            unselectedIconColor = Ink.copy(alpha = 0.4f),
                            unselectedTextColor = Ink.copy(alpha = 0.4f),
                            indicatorColor      = SkyBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = ForestDeep
    ) { pad ->
        Box(
            Modifier.fillMaxSize().padding(pad)
                .background(Brush.verticalGradient(listOf(ForestDeep, Color(0xFF060F0B))))
        ) {
            when {
                vm.selectedTab == 2 -> WsScoringContent(vm)
                vm.selectedTab == 3 -> WsRulesContent()
                vm.setup -> WsSetup(vm.difficulty, onSelect = { vm.difficulty = it }, onStart = { vm.setup = false })
                else -> WsAutomaTab(vm)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WsSetup(selected: WingspanDifficulty, onSelect: (WingspanDifficulty) -> Unit, onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🐦", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text("Elige la dificultad del Automa", color = Ink, fontWeight = FontWeight.Bold,
            fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text("Afecta a los puntos que da cada carta boca abajo del Automa",
            color = Ink.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))

        WingspanDifficulty.entries.forEach { diff ->
            Surface(
                onClick = { onSelect(diff) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected == diff) SkyBlue.copy(alpha = 0.2f) else ForestGreen,
                border = if (selected == diff)
                    androidx.compose.foundation.BorderStroke(2.dp, SkyBlue) else null,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(diff.label, color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${diff.pointsPerFaceDownCard} pts/carta", color = SkyBlue, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Empezar partida", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
private data class AutomaAction(val label: String, val desc: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val action: (WingspanViewModel) -> Unit)

private val AUTOMA_ACTIONS = listOf(
    AutomaAction("Robar pájaro", "Descarta las 3 cartas visibles del mercado y roba 1 boca abajo",
        Icons.Default.Style) { vm -> vm.automaFaceDownCards++ },
    AutomaAction("Poner huevos", "+1 huevo por cada icono de huevo en la fila (introduce el total)",
        Icons.Default.Egg) { /* se ajusta con el stepper de huevos */ },
    AutomaAction("Ganar comida", "Toma dados del comedero según prioridad; relanza si solo queda un tipo",
        Icons.Default.Restaurant) { vm -> vm.automaFoodTokens++ },
    AutomaAction("Cubo de objetivo", "Mueve la posición del Automa en el objetivo de la ronda actual",
        Icons.Default.Flag) { /* se ajusta en la pestaña de puntos */ },
    AutomaAction("Poder rosa", "Se activan tus poderes rosa — el Automa no participa en esta acción",
        Icons.Default.FlashOn) { /* no cambia estado del automa */ },
)

@Composable
private fun WsAutomaTab(vm: WingspanViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Cuando salga una carta del mazo Automa, pulsa la acción que muestra:",
            color = Ink.copy(alpha = 0.7f), fontSize = 13.sp)

        AUTOMA_ACTIONS.forEach { action ->
            Surface(
                onClick = { action.action(vm) },
                shape = RoundedCornerShape(12.dp),
                color = ForestGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(action.icon, null, tint = SkyBlue, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(action.label, color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(action.desc, color = Ink.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = Ink.copy(alpha = 0.15f))

        Text("Estado del Automa", color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        WsStepperRow("🥚 Huevos", vm.automaEggs, { vm.automaEggs = (vm.automaEggs - 1).coerceAtLeast(0) }, { vm.automaEggs++ })
        WsStepperRow("🃏 Cartas boca abajo", vm.automaFaceDownCards, { vm.automaFaceDownCards = (vm.automaFaceDownCards - 1).coerceAtLeast(0) }, { vm.automaFaceDownCards++ })
        WsStepperRow("🍽️ Fichas de comida", vm.automaFoodTokens, { vm.automaFoodTokens = (vm.automaFoodTokens - 1).coerceAtLeast(0) }, { vm.automaFoodTokens++ })
        WsStepperRow("🐦 Valor pájaros jugados", vm.automaPlayedBirdsValue, { vm.automaPlayedBirdsValue = (vm.automaPlayedBirdsValue - 1).coerceAtLeast(0) }, { vm.automaPlayedBirdsValue++ })

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.nextRound() },
            enabled = vm.round < 4,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (vm.round < 4) "Terminar ronda ${vm.round}" else "Última ronda", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WsStepperRow(label: String, value: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Ink, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus) { Icon(Icons.Default.Remove, null, tint = SkyBlue) }
            Text("$value", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            IconButton(onClick = onPlus) { Icon(Icons.Default.Add, null, tint = SkyBlue) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WsScoringContent(vm: WingspanViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Puntuación del Automa", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Text("Objetivos de ronda (compara la posición del cubo del Automa contra la baldosa física):",
            color = Ink.copy(alpha = 0.6f), fontSize = 12.sp)

        for (i in 0..3) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ronda ${i + 1}", color = Ink, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.setRoundGoalScore(i, (vm.roundGoalScores[i] - 1).coerceAtLeast(0)) }) {
                        Icon(Icons.Default.Remove, null, tint = SkyBlue)
                    }
                    Text("${vm.roundGoalScores[i]}", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                    IconButton(onClick = { vm.setRoundGoalScore(i, vm.roundGoalScores[i] + 1) }) {
                        Icon(Icons.Default.Add, null, tint = SkyBlue)
                    }
                }
            }
        }

        HorizontalDivider(color = Ink.copy(alpha = 0.15f))

        WsScoreLine("Huevos", vm.automaEggs, vm.automaEggs)
        WsScoreLine("Cartas boca abajo (×${vm.difficulty.pointsPerFaceDownCard})", vm.automaFaceDownCards, vm.automaFaceDownCards * vm.difficulty.pointsPerFaceDownCard)
        WsScoreLine("Pájaros jugados", vm.automaPlayedBirdsValue, vm.automaPlayedBirdsValue)
        WsScoreLine("Objetivos de ronda", vm.roundGoalScores.sum(), vm.roundGoalScores.sum())

        HorizontalDivider(color = Ink.copy(alpha = 0.15f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TOTAL AUTOMA", color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("${vm.finalScore}", color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = EggCream.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Desempate: el Automa tiene 2 fichas de comida fijas. Si empatáis a puntos, necesitas 3 o más fichas de comida para ganar el desempate.",
                color = Ink.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun WsScoreLine(label: String, count: Int, points: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Ink.copy(alpha = 0.8f), fontSize = 14.sp)
        Text("$points pts", color = Ink, fontSize = 14.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WsRulesContent() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Cómo funciona el Automa", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "El Automa oficial de Wingspan (Automa Factory) se juega con su propio mazo de 10 " +
            "cartas + carta experta, que debes tener físicamente. Tras cada uno de tus turnos, " +
            "roba y resuelve 1 carta del Automa. Esta app solo lleva la cuenta de lo que acumula, " +
            "no sustituye al mazo.",
            color = Ink.copy(alpha = 0.75f), fontSize = 13.sp
        )

        WsRuleBlock("🃏 Robar pájaro", "Descarta las 3 cartas visibles del mercado y roba 1 boca abajo a la reserva del Automa. Vale 3/4/5 puntos según dificultad.")
        WsRuleBlock("🐦 Jugar pájaro", "De las cartas visibles del mercado que coincidan con su bonificación, se queda la de mayor valor y descarta el resto. Puntúa a valor de cara al final.")
        WsRuleBlock("🥚 Poner huevos", "+1 huevo por cada icono de huevo mostrado en esa fila de la carta.")
        WsRuleBlock("🍽️ Ganar comida", "Toma dados del comedero por orden de prioridad de la carta. Si solo queda un tipo de dado, se relanza el comedero.")
        WsRuleBlock("🚩 Cubo de objetivo", "Avanza o retrocede su posición en el objetivo de la ronda actual — compáralo con tu propia posición al final de la ronda.")
        WsRuleBlock("⚡ Poder rosa", "Se activan tus poderes rosa entre turnos — el Automa no interviene en esta acción.")

        Text(
            "Nada de esto sustituye el mazo físico ni las cartas de pájaro reales del juego.",
            color = Ink.copy(alpha = 0.5f), fontSize = 11.sp
        )
    }
}

@Composable
private fun WsRuleBlock(title: String, body: String) {
    Column {
        Text(title, color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(body, color = Ink.copy(alpha = 0.75f), fontSize = 13.sp)
    }
}
