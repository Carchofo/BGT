package com.rafel.bgt.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafel.bgt.ui.theme.*
import com.rafel.bgt.ui.viewmodels.AutomaAction
import com.rafel.bgt.ui.viewmodels.SplendorViewModel

private val SpRed    = Color(0xFFC41E3A)
private val SpGold   = Color(0xFFDAA520)
private val SpDark   = Color(0xFF1A0505)
private val SpCard   = Color(0xFF2A1010)

// Gema: color de fondo, nombre
private val GEM_INFO = mapOf(
    1 to (Color(0xFFE8E8E8) to "Blanca"),
    2 to (Color(0xFF1565C0) to "Azul"),
    3 to (Color(0xFF2E7D32) to "Verde"),
    4 to (Color(0xFFC62828) to "Roja"),
    5 to (Color(0xFF212121) to "Negra"),
)
private val PHASE_LABELS = listOf("Fase Inicial" to Color(0xFF388E3C), "Fase Media" to Color(0xFFF9A825), "Fase Final" to SpRed)
private val ROW_LABELS   = mapOf('A' to "Nivel 3", 'B' to "Nivel 2", 'C' to "Nivel 1")

// ── Pantalla principal ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplendorSoloScreen(onBack: () -> Unit = {}, vm: SplendorViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Splendor", color = GhostWhite, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        Text("Automa fan-made · dukefanblue2005",
                            color = SpGold.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (vm.selectedTab != 0) vm.selectedTab = 0 else onBack()
                    }) { Icon(Icons.Default.ArrowBack, null, tint = GhostWhite) }
                },
                actions = {
                    if (vm.gameStarted) {
                        IconButton(onClick = { vm.reset() }) {
                            Icon(Icons.Default.Refresh, null, tint = SpGold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SpDark, tonalElevation = 0.dp) {
                listOf(
                    Triple(0, Icons.Default.SmartToy, "Automa"),
                    Triple(1, Icons.Default.EmojiEvents, "Puntuación"),
                    Triple(2, Icons.Default.MenuBook, "Reglas"),
                ).forEach { (idx, icon, label) ->
                    NavigationBarItem(
                        selected = vm.selectedTab == idx,
                        onClick  = { vm.selectedTab = idx },
                        icon     = { Icon(icon, null) },
                        label    = { Text(label) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = SpGold, selectedTextColor   = SpGold,
                            indicatorColor      = SpGold.copy(alpha = 0.15f),
                            unselectedIconColor = GhostWhite.copy(alpha = 0.4f),
                            unselectedTextColor = GhostWhite.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        },
        containerColor = SpDark
    ) { padding ->
        when (vm.selectedTab) {
            0 -> AutomaTab(vm, Modifier.padding(padding))
            1 -> ScoreTab(vm, Modifier.padding(padding))
            2 -> RulesTab(Modifier.padding(padding))
        }
    }
}

// ── Tab Automa ─────────────────────────────────────────────────────
@Composable
private fun AutomaTab(vm: SplendorViewModel, modifier: Modifier = Modifier) {
    if (!vm.gameStarted) {
        SetupScreen(vm, modifier)
    } else {
        GameScreen(vm, modifier)
    }
}

@Composable
private fun SetupScreen(vm: SplendorViewModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💎", fontSize = 56.sp)
        Text("Splendor Solitario",
            color = GhostWhite, fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text("Automa fan-made — mazo de 10 cartas (8 base + 2 especiales aleatorias)",
            color = GhostWhite.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center)

        Spacer(Modifier.height(8.dp))

        // Selector de dificultad
        SpCard {
            Text("Dificultad", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            Text("En modo difícil el automa compra cartas de nivel superior antes y descarta menos fichas.",
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(false to "Normal", true to "Difícil").forEach { (hard, label) ->
                    val sel = vm.hardMode == hard
                    Surface(
                        onClick = { vm.hardMode = hard },
                        shape   = RoundedCornerShape(8.dp),
                        color   = if (sel) SpRed else SpCard,
                        border  = BorderStroke(1.dp, if (sel) SpRed else CardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            color    = if (sel) GhostWhite else GhostWhite.copy(alpha = 0.5f),
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }

        SpCard {
            Text("Preparación del tablero", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            listOf(
                "Prepara el tablero como para 2 jugadores (4 cartas por fila)",
                "5 fichas de cada color + 5 fichas de oro",
                "El automa siempre juega primero en cada ronda",
                "Objetivo: llegar a 15 PV antes que el automa en máx. 30 turnos",
            ).forEach {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Text("•", color = SpGold, modifier = Modifier.padding(end = 8.dp, top = 2.dp))
                    Text(it, color = GhostWhite.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Button(
            onClick  = { vm.startGame() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = SpRed)
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = GhostWhite)
            Spacer(Modifier.width(8.dp))
            Text("Empezar partida", color = GhostWhite, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun GameScreen(vm: SplendorViewModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Estado partida ─────────────────────────────────────────
        if (vm.isOver) {
            EndCard(vm)
            return@Column
        }

        // Turno + fase
        val (phaseLabel, phaseColor) = PHASE_LABELS[vm.phase]
        SpCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Turno ${vm.turn} / 30", color = GhostWhite, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)
                    Text("Turnos restantes: ${30 - vm.turn}", color = GhostWhite.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = phaseColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, phaseColor)) {
                    Text(phaseLabel, color = phaseColor, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // ── Acción del automa ──────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = SpCard),
            border   = BorderStroke(2.dp, SpRed.copy(alpha = 0.6f))
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🤖 El automa…", color = SpGold, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)

                when (val action = vm.currentAction) {
                    is AutomaAction.TakeGems -> GemAction(action)
                    is AutomaAction.BuyCard  -> BuyAction(action)
                    null -> Text("—", color = GhostWhite)
                }

                if (vm.currentCard?.isSpecial == true) {
                    Surface(shape = RoundedCornerShape(20.dp),
                        color = SpGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SpGold)) {
                        Text("★ Carta especial", color = SpGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ── Botón siguiente turno ──────────────────────────────────
        Button(
            onClick  = { vm.nextTurn() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = SpRed),
            enabled  = !vm.isOver
        ) {
            Text("Turno siguiente →", color = GhostWhite, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
        }

        // ── Seguimiento del automa ─────────────────────────────────
        SpCard {
            Text("Automa", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
            SpCounter("Puntos de victoria", vm.automaVP, SpRed,
                onInc = { vm.automaVP++ }, onDec = { if (vm.automaVP > 0) vm.automaVP-- })
            SpCounter("Fichas en pila", vm.automaTokens, SpGold,
                subtitle = "Se descartan desde abajo de la pila al comprar",
                onInc = { vm.automaTokens++ }, onDec = { if (vm.automaTokens > 0) vm.automaTokens-- })
        }
    }
}

@Composable
private fun GemAction(action: AutomaAction.TakeGems) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Toma fichas", color = GhostWhite, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            action.gems.forEach { g ->
                val (color, name) = GEM_INFO[g] ?: (Color.Gray to "?")
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(color),
                        contentAlignment = Alignment.Center) {
                        Text("💎", fontSize = 20.sp)
                    }
                    Text(name, color = GhostWhite.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun BuyAction(action: AutomaAction.BuyCard) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Compra la carta en posición", color = GhostWhite,
            style = MaterialTheme.typography.bodyMedium)
        Text("${action.row}${action.col}",
            color = SpGold, fontWeight = FontWeight.Black, fontSize = 40.sp)
        Text("${ROW_LABELS[action.row]} · Columna ${action.col}",
            color = GhostWhite.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
        HorizontalDivider(color = CardBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Remove, null, tint = SpRed, modifier = Modifier.size(16.dp))
            Text("Descarta ${action.discard} fichas de abajo de su pila",
                color = GhostWhite.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
        }
        if (action.isSpecialTurn) {
            Text("(aunque no tenga fichas suficientes, igualmente compra la carta)",
                color = GhostWhite.copy(alpha = 0.45f), style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center)
        }
        Text("Si la carta no está disponible → la acción no tiene efecto",
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun EndCard(vm: SplendorViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SpCard),
        border = BorderStroke(2.dp, if (vm.playerWins) Color(0xFF4CAF50) else SpRed)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (vm.playerWins) "🏆 ¡Ganaste!" else if (vm.automaWins) "🤖 Gana el automa" else "⏱ Tiempo agotado",
                color = if (vm.playerWins) Color(0xFF4CAF50) else SpRed,
                fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
            Text("Tú: ${vm.playerVP} PV  ·  Automa: ${vm.automaVP} PV",
                color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { vm.reset() }, shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpRed)) {
                Text("Nueva partida", color = GhostWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Tab Puntuación ─────────────────────────────────────────────────
@Composable
private fun ScoreTab(vm: SplendorViewModel, modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        SpCard {
            Text("Tu puntuación", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            SpCounter("Puntos de victoria", vm.playerVP, SpGold,
                subtitle = "Cartas compradas + Nobles conseguidos",
                onInc = { vm.playerVP++ }, onDec = { if (vm.playerVP > 0) vm.playerVP-- })
        }

        SpCard {
            Text("Automa", color = SpRed, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            SpCounter("Puntos de victoria", vm.automaVP, SpRed,
                subtitle = "Solo cartas con PV (no recibe Nobles)",
                onInc = { vm.automaVP++ }, onDec = { if (vm.automaVP > 0) vm.automaVP-- })
        }

        if (vm.playerVP > 0 || vm.automaVP > 0) {
            val diff = vm.playerVP - vm.automaVP
            val (msg, color) = when {
                diff > 0  -> ("Vas ganando por $diff PV" to Color(0xFF4CAF50))
                diff < 0  -> ("El automa te lleva ${-diff} PV" to SpRed)
                else      -> ("Empate" to SpGold)
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, color.copy(alpha = 0.4f))) {
                Text(msg, color = color, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
            }
        }

        SpCard {
            Text("Condición de victoria", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            listOf(
                "Al final de cada ronda: si alguien tiene ≥15 PV, termina la partida",
                "Gana quien tenga más PV con ≥15",
                "Si ambos llegan en la misma ronda con el mismo total → gana quien tenga menos cartas compradas",
                "Si nadie llega a 15 en 30 turnos → el jugador pierde",
            ).forEach {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Text("•", color = SpRed, modifier = Modifier.padding(end = 8.dp, top = 2.dp))
                    Text(it, color = GhostWhite.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ── Tab Reglas ─────────────────────────────────────────────────────
@Composable
private fun RulesTab(modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        SpCard {
            Text("Cómo funciona el automa", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            listOf(
                "El automa actúa PRIMERO en cada turno",
                "El mazo tiene 10 cartas: 8 base + 2 especiales aleatorias",
                "Cuando se agota el mazo, se baraja de nuevo",
                "El automa NUNCA reclama Nobles ni usa fichas de oro",
            ).forEach { RuleRow(it) }
        }

        SpCard {
            Text("Fases del juego", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            listOf(
                Triple("Turnos 1–10", "Fase Inicial", Color(0xFF388E3C)),
                Triple("Turnos 11–20", "Fase Media", Color(0xFFF9A825)),
                Triple("Turnos 21–30", "Fase Final", SpRed),
            ).forEach { (turns, label, color) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(turns, color = GhostWhite.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall)
                    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, color)) {
                        Text(label, color = color,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        SpCard {
            Text("Acción: Tomar fichas", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text("La carta indica qué colores tomar (1 de cada uno). Si un color está agotado en el tablero, se ignora ese color. Las fichas se apilan en el área del automa.",
                color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
        }

        SpCard {
            Text("Acción: Comprar carta", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text("La carta indica posición (fila A/B/C + columna 1-4) y cuántas fichas descarta el automa de abajo de su pila. El automa compra aunque no tenga fichas. Si la carta no está en el tablero, la acción no tiene efecto. La carta repuesta del mazo es la normal.",
                color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
            listOf("A" to "Nivel 3 (fila superior)", "B" to "Nivel 2", "C" to "Nivel 1 (fila inferior)").forEach { (row, label) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(row, color = SpGold, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp), style = MaterialTheme.typography.bodyMedium)
                    Text("→ $label", color = GhostWhite.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        SpCard {
            Text("Colores de gemas", color = SpGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                GEM_INFO.forEach { (_, info) ->
                    val (color, name) = info
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(color))
                        Text(name, color = GhostWhite.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ── Componentes compartidos ────────────────────────────────────────
@Composable
private fun SpCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SpCard),
        border = BorderStroke(1.dp, CardBorder)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content)
    }
}

@Composable
private fun SpCounter(
    label: String, value: Int, accent: Color,
    subtitle: String? = null,
    onInc: () -> Unit, onDec: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(label, color = GhostWhite.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) Text(subtitle, color = GhostWhite.copy(alpha = 0.4f),
                style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = onDec, modifier = Modifier.size(32.dp), enabled = value > 0) {
                Icon(Icons.Default.Remove, null,
                    tint = if (value > 0) accent else CardBorder, modifier = Modifier.size(18.dp))
            }
            Text("$value", color = GhostWhite, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp))
            IconButton(onClick = onInc, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null, tint = accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RuleRow(text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Text("•", color = SpGold, modifier = Modifier.padding(end = 8.dp, top = 2.dp))
        Text(text, color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
    }
}
