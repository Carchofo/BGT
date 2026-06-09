package com.rafel.spooktacular.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafel.spooktacular.ui.theme.*

private val CCGold   = Color(0xFFDAA520)
private val CCBrown  = Color(0xFF8B4513)
private val CCCream  = Color(0xFFFFF8DC)

// ─── Estado compartido ────────────────────────────────────────────
private class CastleComboState {
    // Configuración
    var difficulty  by mutableStateOf(1)   // 0=Fácil, 1=Normal, 2=Difícil
    var turn        by mutableStateOf(1)

    // Recursos de Anton (acumulados durante la partida)
    var antonCoins     by mutableStateOf(0)
    var antonKeys      by mutableStateOf(0)
    var antonDiscounts by mutableStateOf(0)

    // Puntuación final
    var playerCards    by mutableStateOf(0)
    var playerKeys     by mutableStateOf(0)
    var antonCards     by mutableStateOf(0)

    // Cálculos de puntuación de Anton según dificultad
    fun antonKeyVP()      = if (difficulty < 2) antonKeys      else antonKeys * 2
    fun antonCoinVP()     = when (difficulty) { 0 -> antonCoins / 2; 1 -> antonCoins; else -> antonCoins * 2 }
    fun antonDiscountVP() = when (difficulty) { 0 -> antonDiscounts; 1 -> antonDiscounts * 2; else -> antonDiscounts * 3 }

    val playerTotal get() = playerCards + playerKeys
    val antonTotal  get() = antonCards + antonKeyVP() + antonCoinVP() + antonDiscountVP()
}

// ─── Pantalla principal ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastleComboSoloScreen(onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val s = remember { CastleComboState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Castle Combo", color = GhostWhite, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        Text("Modo solitario · Oponente: Anton",
                            color = CCGold.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTab != 0) selectedTab = 0 else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, null, tint = GhostWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0E0E1F), tonalElevation = 0.dp) {
                listOf(
                    Triple(0, Icons.Default.Settings, "Setup"),
                    Triple(1, Icons.Default.SmartToy, "Solitario"),
                    Triple(2, Icons.Default.EmojiEvents, "Puntuación"),
                    Triple(3, Icons.Default.MenuBook, "Reglas")
                ).forEach { (idx, icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == idx, onClick = { selectedTab = idx },
                        icon = { Icon(icon, null) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CCGold, selectedTextColor = CCGold,
                            indicatorColor = CCGold.copy(alpha = 0.15f),
                            unselectedIconColor = GhostWhite.copy(alpha = 0.4f),
                            unselectedTextColor = GhostWhite.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        },
        containerColor = MidnightBlue
    ) { padding ->
        when (selectedTab) {
            0 -> CCSetupTab(Modifier.padding(padding))
            1 -> CCSoloTab(s, Modifier.padding(padding))
            2 -> CCScoreTab(s, Modifier.padding(padding))
            3 -> CCRulesTab(Modifier.padding(padding))
        }
    }
}

// ─── Contadores comunes ───────────────────────────────────────────
@Composable
private fun CCCounter(
    label: String, value: Int,
    onInc: () -> Unit, onDec: () -> Unit,
    subtitle: String? = null,
    pts: String? = null,
    accent: Color = CCGold
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(label, color = GhostWhite.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null)
                Text(subtitle, color = GhostWhite.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (pts != null) {
                Text(pts, color = accent.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End,
                    modifier = Modifier.width(60.dp))
                Spacer(Modifier.width(6.dp))
            }
            IconButton(onClick = onDec, modifier = Modifier.size(32.dp), enabled = value > 0) {
                Icon(Icons.Default.Remove, null,
                    tint = if (value > 0) accent else CardBorder,
                    modifier = Modifier.size(18.dp))
            }
            Text("$value", color = GhostWhite, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp))
            IconButton(onClick = onInc, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null, tint = accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun CCCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content)
    }
}

@Composable
private fun CCSectionHeader(title: String, score: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = CCGold, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium)
        if (score != null)
            Text(score, color = CCGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
    }
}

// ─── Tab Solitario ────────────────────────────────────────────────
@Composable
private fun CCSoloTab(s: CastleComboState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dificultad
        CCCard {
            CCSectionHeader("Dificultad")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Fácil", "Normal", "Difícil").forEachIndexed { i, label ->
                    val sel = s.difficulty == i
                    Surface(
                        onClick = { s.difficulty = i }, shape = RoundedCornerShape(8.dp),
                        color = if (sel) CCGold else CardBackground,
                        border = BorderStroke(1.dp, if (sel) CCGold else CardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            color = if (sel) Color.Black else GhostWhite.copy(alpha = 0.6f),
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            // Tabla de puntuación de Anton
            DifficultyTable(s.difficulty)
        }

        // Turno
        CCCard {
            CCSectionHeader("Turno de juego")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = { if (s.turn > 1) s.turn-- }, modifier = Modifier.size(44.dp),
                    enabled = s.turn > 1) {
                    Icon(Icons.Default.Remove, null,
                        tint = if (s.turn > 1) CCGold else CardBorder,
                        modifier = Modifier.size(24.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("${s.turn}", color = CCGold, fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.displaySmall)
                    Text("de 9", color = GhostWhite.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { if (s.turn < 9) s.turn++ }, modifier = Modifier.size(44.dp),
                    enabled = s.turn < 9) {
                    Icon(Icons.Default.Add, null,
                        tint = if (s.turn < 9) CCGold else CardBorder,
                        modifier = Modifier.size(24.dp))
                }
            }
            if (s.turn <= 9) {
                val isAntonTurn = s.turn % 2 == 0
                val label = if (isAntonTurn) "Turno de Anton 🤖" else "Tu turno 👤"
                val color = if (isAntonTurn) CCGold else GhostWhite
                Text(label, color = color, fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        // Recursos de Anton
        CCCard {
            CCSectionHeader("Recursos de Anton (acumulados)")
            Text("Registra los recursos que Anton gana por sus habilidades de carta durante la partida.",
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            CCCounter("🪙 Monedas", s.antonCoins,
                onInc = { s.antonCoins++ }, onDec = { if (s.antonCoins > 0) s.antonCoins-- },
                subtitle = antonCoinRule(s.difficulty))
            CCCounter("🗝️ Llaves", s.antonKeys,
                onInc = { s.antonKeys++ }, onDec = { if (s.antonKeys > 0) s.antonKeys-- },
                subtitle = if (s.difficulty < 2) "1 PV cada una" else "2 PV cada una")
            CCCounter("⚡ Descuentos", s.antonDiscounts,
                onInc = { s.antonDiscounts++ }, onDec = { if (s.antonDiscounts > 0) s.antonDiscounts-- },
                subtitle = "${antonDiscountRate(s.difficulty)} PV cada uno · descuento doble = 2 descuentos")
        }

        // Guía del turno de Anton
        CCCard {
            CCSectionHeader("Turno de Anton · Guía rápida")
            AntonTurnGuide()
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DifficultyTable(difficulty: Int) {
    val headers = listOf("", "Fácil", "Normal", "Difícil")
    val rows = listOf(
        listOf("Llaves", "1 PV/c", "1 PV/c", "2 PV/c"),
        listOf("Monedas", "1 PV/2", "1 PV/c", "2 PV/c"),
        listOf("Descuentos", "1 PV/c", "2 PV/c", "3 PV/c"),
        listOf("«Si falta»", "Solo si falta", "Siempre", "Siempre"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            headers.forEachIndexed { i, h ->
                val w = if (i == 0) 0.32f else 0.23f
                val isSelected = i > 0 && (i - 1) == difficulty
                Text(h, color = if (isSelected) CCGold else GhostWhite.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(w), textAlign = if (i == 0) TextAlign.Start else TextAlign.Center)
            }
        }
        HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEachIndexed { i, cell ->
                    val w = if (i == 0) 0.32f else 0.23f
                    val isSelected = i > 0 && (i - 1) == difficulty
                    Text(cell, color = if (isSelected) GhostWhite else GhostWhite.copy(alpha = 0.45f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.weight(w), textAlign = if (i == 0) TextAlign.Start else TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun AntonTurnGuide() {
    val steps = listOf(
        "Revela la carta de decisión superior del mazo.",
        "Anton toma la primera carta de la fila actual que coincida con la prioridad indicada.",
        "Si no hay coincidencia, revisa las otras dos prioridades de la carta. Si sigue sin haber, usa las prioridades 4ª, 5ª y 6ª (cartas de prioridad encima del tablero).",
        "Anton no paga coste. Obtiene TODOS los beneficios de la habilidad ('o' → 'y').",
        "Anton coloca cartas de izquierda a derecha, arriba a abajo. Excepto si la carta puntúa por posición específica.",
        "Mueve el Mensajero si la carta lo indica.",
        "Aplica efectos adicionales de la carta de decisión si los hay."
    )
    steps.forEachIndexed { i, step ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${i + 1}.", color = CCGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(18.dp))
            Text(step, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun antonCoinRule(d: Int) = when (d) { 0 -> "1 PV cada 2 monedas"; 1 -> "1 PV cada una"; else -> "2 PV cada una" }
private fun antonDiscountRate(d: Int) = when (d) { 0 -> 1; 1 -> 2; else -> 3 }

// ─── Tab Puntuación ───────────────────────────────────────────────
@Composable
private fun CCScoreTab(s: CastleComboState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Resultado
        val playerWins = s.playerTotal > s.antonTotal
        val tie       = s.playerTotal == s.antonTotal
        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    tie        -> CardBackground
                    playerWins -> CCGold.copy(alpha = 0.12f)
                    else       -> SpookyPurple.copy(alpha = 0.12f)
                }
            ),
            border = BorderStroke(1.dp, when {
                tie        -> CardBorder
                playerWins -> CCGold.copy(alpha = 0.5f)
                else       -> SpookyPurple.copy(alpha = 0.5f)
            })
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    when {
                        tie        -> "¡Empate!"
                        playerWins -> "¡Has ganado! 🏆"
                        else       -> "Anton gana 🤖"
                    },
                    color = when {
                        tie        -> GhostWhite
                        playerWins -> CCGold
                        else       -> SpookyPurple
                    },
                    fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TÚ", color = GhostWhite.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelMedium)
                        Text("${s.playerTotal}", color = GhostWhite, fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineLarge)
                    }
                    Text("vs", color = GhostWhite.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterVertically))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ANTON", color = GhostWhite.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelMedium)
                        Text("${s.antonTotal}", color = CCGold, fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
        }

        // Tu puntuación
        CCCard {
            CCSectionHeader("Tu puntuación", "${s.playerTotal} PV")
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            CCCounter("📜 Pergaminos (suma de todas las cartas)", s.playerCards,
                onInc = { s.playerCards++ }, onDec = { if (s.playerCards > 0) s.playerCards-- },
                pts = "${s.playerCards} PV")
            CCCounter("🗝️ Llaves (1 PV cada una)", s.playerKeys,
                onInc = { s.playerKeys++ }, onDec = { if (s.playerKeys > 0) s.playerKeys-- },
                pts = "${s.playerKeys} PV")
        }

        // Puntuación de Anton
        CCCard {
            CCSectionHeader("Anton", "${s.antonTotal} PV")
            Text("Dificultad: ${listOf("Fácil", "Normal", "Difícil")[s.difficulty]}",
                color = CCGold.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            CCCounter("📜 Pergaminos de cartas", s.antonCards,
                onInc = { s.antonCards++ }, onDec = { if (s.antonCards > 0) s.antonCards-- },
                pts = "${s.antonCards} PV")
            CCCounter("🗝️ Llaves (${if (s.difficulty < 2) "1" else "2"} PV c/u)", s.antonKeys,
                onInc = { s.antonKeys++ }, onDec = { if (s.antonKeys > 0) s.antonKeys-- },
                pts = "${s.antonKeyVP()} PV")
            CCCounter("🪙 Monedas (${antonCoinRule(s.difficulty)})", s.antonCoins,
                onInc = { s.antonCoins++ }, onDec = { if (s.antonCoins > 0) s.antonCoins-- },
                pts = "${s.antonCoinVP()} PV")
            CCCounter("⚡ Descuentos (${antonDiscountRate(s.difficulty)} PV c/u)", s.antonDiscounts,
                onInc = { s.antonDiscounts++ }, onDec = { if (s.antonDiscounts > 0) s.antonDiscounts-- },
                pts = "${s.antonDiscountVP()} PV")
            HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
            Text("Nota: las cartas de «si X falta» puntúan ${
                if (s.difficulty == 0) "solo si la condición se cumple" else "siempre (independientemente de la condición)"
            }.", color = GhostWhite.copy(alpha = 0.45f), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─── Tab Reglas ───────────────────────────────────────────────────
@Composable
private fun CCRulesTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Castle Combo · Modo Solitario", color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text("Modo solitario fan-made (ben_uez, BGG) · Requiere las cartas de decisión y prioridad impresas.",
            color = GhostWhite.copy(alpha = 0.45f), style = MaterialTheme.typography.bodySmall)

        CCRuleBlock("⚙️ Preparación",
            "Configura el juego normalmente. Añade el tablero de Anton.\n" +
            "Baraja las 6 cartas de prioridad (colores = tipos de escudo) y colócalas encima del tablero de Anton (una por columna).\n" +
            "Baraja las 12 cartas de decisión y colócalas bocabajo. Anton empieza sin monedas ni llaves.\n" +
            "Elige la dificultad. Tú eres el jugador inicial.")

        CCRuleBlock("🎮 Turno de Anton",
            "Anton NO usa llaves y SIEMPRE toma cartas de la fila actual (donde está el Mensajero). Solo realiza los pasos 2-4.\n\n" +
            "1. Revela la carta de decisión superior.\n" +
            "2. Anton toma la primera carta de la fila que coincida con la prioridad indicada (izq. o der. según la flecha).\n" +
            "3. Sin coincidencia: prueba las otras dos prioridades de la carta; luego las prioridades 4ª, 5ª y 6ª (cartas encima del tablero).\n" +
            "4. Anton no paga costes. Obtiene TODOS los beneficios ('o' → 'y').\n" +
            "5. Colocación: izquierda→derecha, arriba→abajo. Excepción: cartas que puntúan por posición van al primer hueco válido.\n" +
            "6. Mueve el Mensajero si la carta lo indica.\n" +
            "7. Aplica efectos adicionales de la carta de decisión.")

        CCRuleBlock("🃏 Efectos adicionales (cartas de decisión)",
            "Icono de descarte: descarta las 3 cartas de la fila actual.\n" +
            "Icono de barajado: baraja todas las cartas de decisión de nuevo en el mazo.")

        CCRuleBlock("🏆 Puntuación de Anton",
            "Anton puntúa sus cartas igual que en el multijugador (pergaminos).\n" +
            "Recursos adicionales según dificultad:\n\n" +
            "FÁCIL · Llaves: 1PV/c · Monedas: 1PV/2 · Descuentos: 1PV/c · «Si falta»: solo si se cumple\n" +
            "NORMAL · Llaves: 1PV/c · Monedas: 1PV/c · Descuentos: 2PV/c · «Si falta»: siempre\n" +
            "DIFÍCIL · Llaves: 2PV/c · Monedas: 2PV/c · Descuentos: 3PV/c · «Si falta»: siempre\n\n" +
            "Un descuento que aplica a ambas filas cuenta como 2 descuentos.")

        CCRuleBlock("📜 Resumen de puntuación del juego base",
            "Cada carta tiene un pergamino de puntuación que se evalúa al final.\n" +
            "Las 6 tipos de escudo son: Nobleza (👑), Militar (⚔️), Fe (✝️), Artesanía (🔨), Estudiante (📚), Campesino (🌾).\n" +
            "Las llaves otorgan 1 PV cada una.\n" +
            "El oro solo puntúa si está almacenado en cartas con Bolsa.\n" +
            "Las cartas bocabajo no otorgan puntos (pero dan 6 monedas + 2 llaves al cogerlas).")

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CCRuleBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = CCGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ─── Tab Setup ─────────────────────────────────────────────────────
@Composable
private fun CCSetupTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Preparación · Castle Combo", color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text("1 a 4 jugadores · Juego de combinaciones",
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        CCSetupBlock("🗺️ Tablero central",
            "• Coloca el tablero central con el mercado inferior (Ciudad Baja) y superior (Ciudad Alta).\n" +
            "• Reparte cartas en 2 filas según el número de jugadores:\n" +
            "  → 1-2 jugadores: 2 columnas. 3-4 jugadores: 3 columnas.\n" +
            "• Coloca el suministro de monedas (oro) y llaves accesibles a todos.")
        CCSetupBlock("👤 Cada jugador recibe",
            "• 15 monedas de oro.\n" +
            "• 2 llaves.\n" +
            "• Sin cartas al inicio — las cartas se compran durante la partida.\n" +
            "• Sin tablero individual (el 3×3 se forma en tu área de juego).")
        CCSetupBlock("🤖 Solo mode — Anton",
            "• Anton no recibe componentes al inicio de la partida.\n" +
            "• Lleva la cuenta de: monedas, llaves y descuentos que Anton acumula durante la partida.\n" +
            "• Al final Anton convierte sus recursos en PV según la dificultad elegida:\n" +
            "  Fácil → llaves ×1, monedas ÷2, descuentos ×1\n" +
            "  Normal → llaves ×1, monedas ×1, descuentos ×2\n" +
            "  Difícil → llaves ×2, monedas ×2, descuentos ×3")
        CCSetupBlock("🃏 Turno de Anton",
            "• Cada vez que el jugador compra una carta, Anton también roba o actúa según las reglas del modo solitario.\n" +
            "• Consulta la pestaña Solitario para ver el tracker de turno.")
    }
}

@Composable
private fun CCSetupBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CCGold.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = CCGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
