package com.rafel.bgt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafel.bgt.ui.viewmodels.FridayViewModel

private val FridayPrimary   = Color(0xFF4E342E)
private val FridaySecondary = Color(0xFF6D4C41)
private val FridayAccent    = Color(0xFFA5D6A7)

private val PhaseColorVerde    = Color(0xFF388E3C)
private val PhaseColorAmarillo = Color(0xFFF9A825)
private val PhaseColorRojo     = Color(0xFFD32F2F)
private val PhaseColorPiratas  = Color(0xFF4A148C)

private fun phaseColor(phase: Int) = when (phase) {
    0 -> PhaseColorVerde
    1 -> PhaseColorAmarillo
    2 -> PhaseColorRojo
    else -> PhaseColorPiratas
}

@Composable
fun FridaySoloScreen(onBack: () -> Unit = {}, vm: FridayViewModel = viewModel()) {
    Scaffold(
        containerColor = FridayPrimary,
        bottomBar = {
            NavigationBar(containerColor = FridaySecondary) {
                NavigationBarItem(
                    selected = vm.selectedTab == 0,
                    onClick = { vm.selectedTab = 0 },
                    icon = { Icon(Icons.Default.Casino, contentDescription = null) },
                    label = { Text("Juego") }
                )
                NavigationBarItem(
                    selected = vm.selectedTab == 1,
                    onClick = { vm.selectedTab = 1 },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                    label = { Text("Puntuación") }
                )
                NavigationBarItem(
                    selected = vm.selectedTab == 2,
                    onClick = { vm.selectedTab = 2 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Reglas") }
                )
                NavigationBarItem(
                    selected = vm.selectedTab == 3,
                    onClick = { vm.selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Setup") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (vm.selectedTab) {
                0 -> FridayGameTab(vm)
                1 -> FridayScoreTab(vm)
                2 -> FridayRulesTab()
                3 -> FridaySetupTab(vm)
            }
        }
    }
}

@Composable
private fun Counter(
    label: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    valueColor: Color = Color.White,
    valueFontSize: Int = 48
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FilledTonalButton(
                onClick = onMinus,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FridaySecondary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) { Text("−", fontSize = 22.sp, color = Color.White) }
            Text("$value", fontSize = valueFontSize.sp, fontWeight = FontWeight.Bold, color = valueColor)
            FilledTonalButton(
                onClick = onPlus,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FridaySecondary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) { Text("+", fontSize = 22.sp, color = Color.White) }
        }
    }
}

@Composable
private fun FridayGameTab(vm: FridayViewModel) {
    val pc = phaseColor(vm.phase)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FridayPrimary)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("FRIDAY", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = FridayAccent)

        // Fase
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = pc.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(vm.phaseLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = pc)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { vm.prevPhase() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = pc)
                    ) { Text("← Fase anterior") }
                    Button(
                        onClick = { vm.nextPhase() },
                        colors = ButtonDefaults.buttonColors(containerColor = pc)
                    ) { Text("Fase siguiente →") }
                }
                if (vm.phase < 3) {
                    Text(
                        "Agota el mazo de peligros para avanzar de fase",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        "¡Derrota a los 2 piratas para ganar!",
                        fontSize = 12.sp,
                        color = pc,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        HorizontalDivider(color = FridaySecondary)

        // Vida
        Counter(
            label = "Puntos de Vida ❤️",
            value = vm.lifePoints,
            onMinus = { if (vm.lifePoints > 0) vm.lifePoints-- },
            onPlus = { vm.lifePoints++ },
            valueColor = if (vm.lifePoints <= 3) Color(0xFFEF5350) else FridayAccent,
            valueFontSize = 56
        )

        HorizontalDivider(color = FridaySecondary)

        // Combate helper
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = FridaySecondary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Helper de combate", color = FridayAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Counter(
                        label = "Cartas gratis\n(número blanco)",
                        value = vm.freeCards,
                        onMinus = { if (vm.freeCards > 0) vm.freeCards-- },
                        onPlus = { vm.freeCards++ },
                        valueFontSize = 32
                    )
                    Counter(
                        label = "Fuerza\nrequerida",
                        value = vm.hazardTarget,
                        onMinus = { if (vm.hazardTarget > 0) vm.hazardTarget-- },
                        onPlus = { vm.hazardTarget++ },
                        valueFontSize = 32
                    )
                }

                Counter(
                    label = "Fuerza Robinson acumulada",
                    value = vm.robinsonStrength,
                    onMinus = { if (vm.robinsonStrength > 0) vm.robinsonStrength-- },
                    onPlus = { vm.robinsonStrength++ },
                    valueColor = if (vm.isWinning) PhaseColorVerde else PhaseColorRojo,
                    valueFontSize = 40
                )

                // Resultado
                val diff = vm.strengthDiff
                val resultText = when {
                    diff >= 0 -> "✅ Ganando por $diff"
                    else -> "❌ Perdiendo por ${-diff} (coste: ${-diff} PV)"
                }
                val resultColor = if (vm.isWinning) PhaseColorVerde else PhaseColorRojo
                Text(resultText, color = resultColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Gastar 1 vida por carta extra
                    OutlinedButton(
                        onClick = { vm.spendLifeForCard() },
                        enabled = vm.lifePoints > 0,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FridayAccent)
                    ) { Text("−1 PV → carta extra") }

                    // Aceptar derrota
                    if (!vm.isWinning) {
                        Button(
                            onClick = { vm.acceptDefeat() },
                            colors = ButtonDefaults.buttonColors(containerColor = PhaseColorRojo)
                        ) { Text("Aceptar derrota") }
                    }
                }

                if (vm.lifeSpentThisFight > 0) {
                    Text(
                        "Vida gastada en este combate: ${vm.lifeSpentThisFight} PV",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                TextButton(onClick = { vm.resetFight() }) {
                    Text("Limpiar combate", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }
            }
        }

        OutlinedButton(
            onClick = { vm.reset() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.5f))
        ) { Text("Reiniciar partida") }
    }
}

@Composable
private fun FridayScoreTab(vm: FridayViewModel) {
    val ranking = when {
        vm.lifePoints <= 0 -> "💀 DERROTA"
        vm.lifePoints in 1..5 -> "😅 Sobreviviste (por poco)"
        vm.lifePoints in 6..10 -> "🏆 Victoria"
        else -> "⭐ Victoria Perfecta"
    }
    Column(
        modifier = Modifier.fillMaxSize().background(FridayPrimary).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Puntuación Final", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FridayAccent)
        Text("Puntos de vida restantes:", color = Color.White)
        Text(
            "${vm.lifePoints}",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = if (vm.lifePoints <= 0) PhaseColorRojo else FridayAccent
        )
        Text(ranking, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        HorizontalDivider(color = FridaySecondary)
        Text("Tabla de resultados", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "0 PV" to "💀 Derrota",
                "1–5 PV" to "😅 Sobreviviste",
                "6–10 PV" to "🏆 Victoria",
                "11+ PV" to "⭐ Victoria Perfecta"
            ).forEach { (pts, label) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(pts, color = Color.White.copy(alpha = 0.8f))
                    Text(label, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FridayRulesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FridayPrimary)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reglas — Viernes (Friday)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FridayAccent)

        listOf(
            "OBJETIVO" to
                "Ayudar a Robinson Crusoe a sobrevivir 3 fases y derrotar a los 2 piratas al final.",
            "FASES" to
                "El mazo de peligros se agota 3 veces (Verde → Amarillo → Rojo). Al agotar el mazo, avanza de fase. Tras la Fase Roja vienen los Piratas.",
            "TURNO" to
                "1. Roba 2 cartas de peligro → elige una, descarta la otra.\n" +
                "2. El número blanco de la carta = cartas de Robinson que robas gratis.\n" +
                "3. Puedes gastar 1 PV para robar 1 carta extra (sin límite).\n" +
                "4. Suma la fuerza de todas las cartas robadas. Compárala con la fuerza requerida (varía por fase).",
            "COMBATE: VICTORIA" to
                "Tu fuerza ≥ fuerza requerida → ganas. Añade la carta de peligro a tu mazo de Robinson.",
            "COMBATE: DERROTA" to
                "Tu fuerza < fuerza requerida → pierdes X PV (X = diferencia). Sin embargo, puedes elegir eliminar del juego las cartas de Robinson más débiles que hayas robado (1 carta por 2 PV de pérdida que absorbes).",
            "CARTAS DE ENVEJECIMIENTO" to
                "Cuando el mazo de Robinson se agota y hay que barajarlo, añade una carta de envejecimiento (negativa) al mazo. Si no quedan cartas de envejecimiento → DERROTA.",
            "PIRATAS" to
                "Tras la Fase Roja: pelea contra los 2 piratas de forma separada. Cada pirata tiene una fuerza fija. Aplican las mismas reglas de combate. Carta de envejecimiento de bonus antes de cada pelea.",
            "FIN DE PARTIDA" to
                "VICTORIA: derrotas ambos piratas con PV > 0.\nDERROTA: PV llegan a 0, o debes añadir carta de envejecimiento y no quedan."
        ).forEach { (title, body) ->
            Text(title, fontWeight = FontWeight.Bold, color = FridayAccent, fontSize = 13.sp)
            Text(body, color = Color.White.copy(alpha = 0.85f), lineHeight = 20.sp, fontSize = 13.sp)
            HorizontalDivider(color = FridaySecondary.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun FridaySetupTab(vm: FridayViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FridayPrimary)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Setup", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FridayAccent)

        Text("Dificultad", color = Color.White, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Fácil" to "22 PV", "Normal" to "20 PV", "Difícil" to "18 PV").forEach { (d, pv) ->
                FilterChip(
                    selected = vm.difficulty == d,
                    onClick = { vm.difficulty = d },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(d, color = if (vm.difficulty == d) FridayPrimary else Color.White, fontSize = 13.sp)
                            Text(pv, color = if (vm.difficulty == d) FridayPrimary else Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FridayAccent,
                        containerColor = FridaySecondary
                    )
                )
            }
        }

        HorizontalDivider(color = FridaySecondary)
        Text("Preparación", color = FridayAccent, fontWeight = FontWeight.Bold)

        listOf(
            "1. Mazo Robinson inicial (18 cartas):\n   • 6× Distracción (−1)\n   • 3× Comer bien (+0, roba carta extra)\n   • 1× Foco (copia el valor de otra)\n   • 1× Doble filo (−2 o +2)\n   • 2× Luchador (+2)\n   • 2× Corredor (huir sin coste)\n   • 1× Astuto (+1 carta extra gratis)",
            "2. Mazo de peligros: ordena Verde encima, luego Amarillo, luego Rojo (o mezcla cada fase por separado).",
            "3. Separa las 2 cartas de pirata. Se usan al final, tras la Fase Roja.",
            "4. Cartas de envejecimiento: distribución según dificultad. Se añaden al mazo Robinson cuando se agota.",
            "Fácil: 22 PV, menos cartas de envejecimiento negativas.\nNormal: 20 PV, envejecimiento estándar.\nDifícil: 18 PV, más cartas de envejecimiento negativas y piratas más fuertes."
        ).forEach { step ->
            Text(step, color = Color.White.copy(alpha = 0.85f), lineHeight = 20.sp, fontSize = 13.sp)
        }

        Button(
            onClick = { vm.reset() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FridaySecondary)
        ) {
            Text("Confirmar setup e iniciar partida", color = FridayAccent, fontWeight = FontWeight.Bold)
        }
    }
}
