package com.rafel.bgt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

private val FridayPrimary = Color(0xFF4E342E)
private val FridaySecondary = Color(0xFF6D4C41)
private val FridayAccent = Color(0xFFA5D6A7)

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
private fun FridayGameTab(vm: FridayViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().background(FridayPrimary)
            .padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("FRIDAY", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FridayAccent)
        Text("Dificultad: ${vm.difficulty}", color = Color.White.copy(alpha = 0.7f))

        HorizontalDivider(color = FridaySecondary)

        // Life points
        Text("Puntos de Vida", color = Color.White, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            FilledTonalButton(onClick = { if (vm.lifePoints > 0) vm.lifePoints-- },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FridaySecondary)) {
                Text("−", fontSize = 24.sp, color = Color.White)
            }
            Text("${vm.lifePoints}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = FridayAccent)
            FilledTonalButton(onClick = { vm.lifePoints++ },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FridaySecondary)) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }

        HorizontalDivider(color = FridaySecondary)

        // Round
        Text("Ronda", color = Color.White, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            FilledTonalButton(onClick = { if (vm.round > 1) vm.round-- },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FridaySecondary)) {
                Text("−", fontSize = 24.sp, color = Color.White)
            }
            Text("${vm.round} / 8", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            FilledTonalButton(onClick = { if (vm.round < 8) vm.round++ },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FridaySecondary)) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }

        HorizontalDivider(color = FridaySecondary)

        // Hazard strength
        Text("Fuerza Peligro Actual", color = Color.White, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            FilledTonalButton(onClick = { if (vm.hazardStrength > 0) vm.hazardStrength-- },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FridaySecondary)) {
                Text("−", fontSize = 24.sp, color = Color.White)
            }
            Text("${vm.hazardStrength}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            FilledTonalButton(onClick = { vm.hazardStrength++ },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = FridaySecondary)) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }

        HorizontalDivider(color = FridaySecondary)

        OutlinedButton(onClick = { vm.reset() }) {
            Text("Reiniciar partida", color = Color.White)
        }
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
        Text("${vm.lifePoints}", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = FridayAccent)
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
        modifier = Modifier.fillMaxSize().background(FridayPrimary)
            .padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reglas — Friday", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FridayAccent)
        listOf(
            "OBJETIVO" to "Ayudar a Robinson Crusoe a sobrevivir derrotando las dos cartas de pirata al final.",
            "TURNO" to "1. Roba carta de peligro → elige cara fácil/difícil.\n2. Roba cartas de lucha (cara fácil = número libre, cara difícil = número fijo).\n3. Si pierdes: pierde PV = diferencia. Puedes añadir cartas al coste de 2 PV/carta.\n4. Si ganas: añade la carta de peligro a tu mazo.",
            "CARTAS ENVEJECIMIENTO" to "Al pasar ronda 3 y ronda 6, baraja cartas de envejecimiento (negativas) en tu mazo.",
            "PIRATAS" to "Rondas 7–8: pelea contra los dos piratas. Necesitas más fuerza total que su valor.",
            "FIN" to "Ganas si derrotas ambos piratas con PV > 0. Pierdes si PV llega a 0."
        ).forEach { (title, body) ->
            Text(title, fontWeight = FontWeight.Bold, color = FridayAccent)
            Text(body, color = Color.White.copy(alpha = 0.85f), lineHeight = 20.sp)
            HorizontalDivider(color = FridaySecondary.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun FridaySetupTab(vm: FridayViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().background(FridayPrimary)
            .padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Setup", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FridayAccent)

        Text("Dificultad", color = Color.White, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Fácil", "Normal", "Difícil").forEach { d ->
                FilterChip(
                    selected = vm.difficulty == d,
                    onClick = { vm.difficulty = d },
                    label = { Text(d, color = if (vm.difficulty == d) FridayPrimary else Color.White) },
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
            "1. Separa las cartas de Robinson (mazo inicial): 6×Distracción(−1), 3×Comer bien(+0), 1×Foco(copia), 1×Doble filo, 2×Luchador(+2), 2×Corredor(huir gratis), 1×Astuto(+1 carta extra).",
            "2. Baraja el mazo de peligros por nivel (Verde → Amarillo → Rojo).",
            "Fácil: empieza con 22 PV y sin cartas de envejecimiento nivel 1.",
            "Normal: 20 PV, envejecimiento estándar.",
            "Difícil: 18 PV, más cartas negativas de envejecimiento.",
            "3. Coloca los dos piratas boca abajo al final del mazo.",
            "4. ¡Empieza con ronda 1!"
        ).forEach { step ->
            Text(step, color = Color.White.copy(alpha = 0.85f), lineHeight = 20.sp)
        }

        Button(onClick = { vm.reset() }, colors = ButtonDefaults.buttonColors(containerColor = FridaySecondary)) {
            Text("Confirmar setup e iniciar", color = Color.White)
        }
    }
}
