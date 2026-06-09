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

private val CoiAmber  = Color(0xFFD4900A)
private val CoiGold   = Color(0xFFF5B800)

// ─── Tablas de dados ─────────────────────────────────────────────
private val DICE_TABLE = listOf(
    Triple("1", "🟣", "Morado"),
    Triple("2", "🟢", "Verde"),
    Triple("3", "🟠", "Naranja bajo"),
    Triple("4", "⬜", "Gris bajo"),
    Triple("5", "🔴", "Naranja alto"),
    Triple("6", "⬜", "Gris alto"),
)

private val CITY_TABLE = listOf(
    Triple("1–2", "🏰", "Ciudad Alta"),
    Triple("3–4", "🏛️", "Ciudad Central"),
    Triple("5–6", "🏠", "Ciudad Baja"),
)

// ─── Estado ───────────────────────────────────────────────────────
private class CoimbraState {
    var round by mutableStateOf(1)

    // Puntuación por categorías
    var ptCartas        by mutableStateOf(0)
    var ptPeregrin      by mutableStateOf(0)
    var ptInfluencia    by mutableStateOf(0)
    var ptMonedas       by mutableStateOf(0)
    var ptFavores       by mutableStateOf(0)
    var ptOtros         by mutableStateOf(0)

    val total get() = ptCartas + ptPeregrin + ptInfluencia + ptMonedas + ptFavores + ptOtros

    val ratingLabel get() = when {
        total < 130 -> "Mal resultado 😬"
        total < 160 -> "Aceptable 👍"
        total < 190 -> "Bueno 🌟"
        total < 220 -> "Excelente ⭐"
        else        -> "¡Increíble! 🏆"
    }

    val ratingColor get() = when {
        total < 130 -> BloodRed
        total < 160 -> GhostWhite
        total < 190 -> CoiAmber
        total < 220 -> CoiGold
        else        -> HalloweenOrange
    }
}

// ─── Pantalla principal ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoimbraSoloScreen(onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val s = remember { CoimbraState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Coimbra", color = GhostWhite, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        Text("Modo solitario · Bot de interferencia",
                            color = CoiAmber.copy(alpha = 0.85f),
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
                            selectedIconColor = CoiAmber, selectedTextColor = CoiAmber,
                            indicatorColor = CoiAmber.copy(alpha = 0.15f),
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
            0 -> CoiSetupTab(Modifier.padding(padding))
            1 -> CoiSoloTab(s, Modifier.padding(padding))
            2 -> CoiScoreTab(s, Modifier.padding(padding))
            3 -> CoiRulesTab(Modifier.padding(padding))
        }
    }
}

// ─── Componentes comunes ──────────────────────────────────────────
@Composable
private fun CoiCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder), content = {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content)
        }
    )
}

@Composable
private fun CoiSectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(title, color = CoiAmber, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium)
        if (subtitle != null)
            Text(subtitle, color = GhostWhite.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CoiCounter(
    label: String, value: Int,
    onInc: () -> Unit, onDec: () -> Unit,
    pts: String? = null, subtitle: String? = null
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
                Text(pts, color = CoiAmber.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End,
                    modifier = Modifier.width(56.dp))
                Spacer(Modifier.width(6.dp))
            }
            IconButton(onClick = onDec, modifier = Modifier.size(32.dp), enabled = value > 0) {
                Icon(Icons.Default.Remove, null,
                    tint = if (value > 0) CoiAmber else CardBorder,
                    modifier = Modifier.size(18.dp))
            }
            Text("$value", color = GhostWhite, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp))
            IconButton(onClick = onInc, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null, tint = CoiAmber, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─── Tab Solitario ────────────────────────────────────────────────
@Composable
private fun CoiSoloTab(s: CoimbraState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ronda
        CoiCard {
            CoiSectionHeader("Ronda actual")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = { if (s.round > 1) s.round-- }, modifier = Modifier.size(44.dp),
                    enabled = s.round > 1) {
                    Icon(Icons.Default.Remove, null,
                        tint = if (s.round > 1) CoiAmber else CardBorder,
                        modifier = Modifier.size(24.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("${s.round}", color = CoiAmber, fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.displaySmall)
                    Text("de 4", color = GhostWhite.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { if (s.round < 4) s.round++ }, modifier = Modifier.size(44.dp),
                    enabled = s.round < 4) {
                    Icon(Icons.Default.Add, null,
                        tint = if (s.round < 4) CoiAmber else CardBorder,
                        modifier = Modifier.size(24.dp))
                }
            }
            Text("⚠️ El Bot siempre va primero en todos los turnos.",
                color = CoiAmber.copy(alpha = 0.8f), fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall)
        }

        // Tabla de dados del Bot
        CoiCard {
            CoiSectionHeader("Dados del Bot",
                "Tira 1d6 por cada dado que coge el Bot (3 dados por ronda)")
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            DICE_TABLE.forEach { (num, emoji, name) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(num, color = CoiAmber, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                    Text(emoji, style = MaterialTheme.typography.bodyLarge)
                    Text(name, color = GhostWhite.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
            Text("Si el color indicado no está disponible → prueba el dado blanco → sigue bajando en número hasta encontrar uno disponible.",
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            Text("Gris/Naranja «bajo» = el de menor valor de los dos disponibles · «alto» = el de mayor valor. Si solo queda uno, coge ese.",
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        }

        // Colocación en ciudad
        CoiCard {
            CoiSectionHeader("Colocación en Ciudad",
                "Tira 1d6 por cada dado del Bot para saber la zona")
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            CITY_TABLE.forEach { (rango, emoji, name) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(rango, color = CoiAmber, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(36.dp))
                    Text(emoji, style = MaterialTheme.typography.bodyLarge)
                    Text(name, color = GhostWhite.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Restricciones de colocación:", color = CoiAmber.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("• Máximo 2 dados del Bot en la misma zona.",
                    color = GhostWhite.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
                Text("• No colocar donde ya haya 4+ elementos (dados + fichas) a menos que el dado garantice una carta al Bot.",
                    color = GhostWhite.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
                Text("• Si la zona resultante ya tiene 2 dados Bot o está llena → reasignar: 1-3 Ciudad Alta, 4-6 Ciudad Central.",
                    color = GhostWhite.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Castillo
        CoiCard {
            CoiSectionHeader("Castillo")
            Text("Solo aplica si el jugador ha colocado un dado en el Castillo este turno.",
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Mira el valor del 7º dado sobrante (el que no se usa):",
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("7º dado ≥ tu dado", color = GhostWhite.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center)
                            Text("✅ Ignorar", color = CoiAmber, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center)
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("7º dado < tu dado", color = GhostWhite.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center)
                            Text("❌ Bot roba 1 ficha", color = BloodRed, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center)
                        }
                    }
                }
                Text("Si el Bot roba: tira 1d4 para ver cuál de las 4 fichas de favor coge antes que tú (el Bot usa 4 dados ese turno).",
                    color = GhostWhite.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Cartas
        CoiCard {
            CoiSectionHeader("Cartas")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Las 5 fichas de dado toman cartas normalmente (mayor influencia, empate al más a la izquierda).",
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text("Cuando le toca al Bot, tira un dado para elegir aleatoriamente entre las cartas disponibles:",
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
                val cardRolls = listOf("4 cartas → 1-4", "3 cartas → 1-3", "2 cartas → 1-2", "1 carta → la toma")
                cardRolls.forEach { txt ->
                    Text("• $txt", color = GhostWhite.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
                Text("🎯 Influencia Bot = valor de influencia de la carta + 1 (rango 2–5).",
                    color = CoiAmber, fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall)
                Text("El Bot no activa ningún otro efecto de las cartas salvo ganar la influencia +1.",
                    color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Coronas
        CoiCard {
            CoiSectionHeader("Coronas")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("El jugador solo cuenta 1 corona por ir 2º en orden de turno (el Bot siempre es 1º).",
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text("Más las coronas obtenidas de fichas de favor en el turno actual de puntuación.",
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text("No usar fichas de corona durante la partida (o solo en la última ronda si se quiere).",
                    color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Peregrinaciones
        CoiCard {
            CoiSectionHeader("Peregrinaciones")
            Text("El jugador puede elegir cualquiera de los 4 puntos de salida desde Coimbra. El Bot no hace peregrinaciones.",
                color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
        }

        // Cartas de personaje
        CoiCard {
            CoiSectionHeader("Cartas de personaje I")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("El Bot recibe 2 cartas I aleatorias al inicio. Su influencia = valor de la carta + 1.",
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text("El jugador recibe 2 cartas I aleatorias de las 8 disponibles (sin elección).",
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text("Cuando una carta del jugador implicaría robar recursos o PV a otro jugador → siempre recibe la opción sin PV.",
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─── Tab Puntuación ───────────────────────────────────────────────
@Composable
private fun CoiScoreTab(s: CoimbraState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Resultado
        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CoiAmber.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, CoiAmber.copy(alpha = 0.4f))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("PUNTUACIÓN TOTAL", color = GhostWhite.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                Text("${s.total}", color = CoiGold, fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displaySmall)
                Text(s.ratingLabel, color = s.ratingColor, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                // Escala visual
                RatingBar(s.total)
            }
        }

        // Categorías de puntuación
        CoiCard {
            Text("Desglose de puntuación", color = CoiAmber, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

            CoiCounter("🃏 Cartas de guilda", s.ptCartas,
                onInc = { s.ptCartas++ }, onDec = { if (s.ptCartas > 0) s.ptCartas-- },
                pts = "${s.ptCartas} PV",
                subtitle = "PV de pergaminos de puntuación al final")
            CoiCounter("⛪ Peregrinaciones", s.ptPeregrin,
                onInc = { s.ptPeregrin++ }, onDec = { if (s.ptPeregrin > 0) s.ptPeregrin-- },
                pts = "${s.ptPeregrin} PV",
                subtitle = "Monasterios visitados")
            CoiCounter("📊 Pistas de influencia", s.ptInfluencia,
                onInc = { s.ptInfluencia++ }, onDec = { if (s.ptInfluencia > 0) s.ptInfluencia-- },
                pts = "${s.ptInfluencia} PV",
                subtitle = "PV de rondas intermedias + final de partida")
            CoiCounter("🪙 Monedas", s.ptMonedas,
                onInc = { s.ptMonedas++ }, onDec = { if (s.ptMonedas > 0) s.ptMonedas-- },
                pts = "${s.ptMonedas} PV",
                subtitle = "Monedas convertidas a PV")
            CoiCounter("⭐ Fichas de favor", s.ptFavores,
                onInc = { s.ptFavores++ }, onDec = { if (s.ptFavores > 0) s.ptFavores-- },
                pts = "${s.ptFavores} PV",
                subtitle = "Bonificaciones de fichas de favor")
            CoiCounter("➕ Otros", s.ptOtros,
                onInc = { s.ptOtros++ }, onDec = { if (s.ptOtros > 0) s.ptOtros-- },
                pts = "${s.ptOtros} PV",
                subtitle = "Cartas de inicio, coronas, etc.")
        }

        // Tabla de clasificación
        CoiCard {
            Text("Tabla de clasificación", color = CoiAmber, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            listOf(
                Triple("< 130",   "Mal resultado 😬",  BloodRed),
                Triple("130–160", "Aceptable 👍",       GhostWhite),
                Triple("160–190", "Bueno 🌟",           CoiAmber),
                Triple("190–220", "Excelente ⭐",       CoiGold),
                Triple("> 220",   "¡Increíble! 🏆",    HalloweenOrange),
            ).forEach { (range, label, color) ->
                val isCurrent = when (range) {
                    "< 130"   -> s.total < 130
                    "130–160" -> s.total in 130..159
                    "160–190" -> s.total in 160..189
                    "190–220" -> s.total in 190..220
                    else      -> s.total > 220
                }
                Row(Modifier.fillMaxWidth()
                    .background(
                        if (isCurrent) color.copy(alpha = 0.1f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(range, color = if (isCurrent) color else GhostWhite.copy(alpha = 0.45f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(80.dp))
                    Text(label, color = if (isCurrent) color else GhostWhite.copy(alpha = 0.45f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                    if (isCurrent)
                        Text("◄", color = color, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RatingBar(total: Int) {
    val progress = (total.coerceIn(0, 230).toFloat() / 230f)
    val barColor = when {
        total < 130 -> BloodRed
        total < 160 -> GhostWhite
        total < 190 -> CoiAmber
        total < 220 -> CoiGold
        else        -> HalloweenOrange
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.fillMaxWidth().height(8.dp).background(CardBorder, RoundedCornerShape(4.dp))) {
            Box(Modifier.fillMaxWidth(progress).height(8.dp)
                .background(barColor, RoundedCornerShape(4.dp)))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0", color = GhostWhite.copy(alpha = 0.3f), style = MaterialTheme.typography.labelSmall)
            Text("130", color = GhostWhite.copy(alpha = 0.3f), style = MaterialTheme.typography.labelSmall)
            Text("160", color = GhostWhite.copy(alpha = 0.3f), style = MaterialTheme.typography.labelSmall)
            Text("190", color = GhostWhite.copy(alpha = 0.3f), style = MaterialTheme.typography.labelSmall)
            Text("220", color = GhostWhite.copy(alpha = 0.3f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ─── Tab Reglas ───────────────────────────────────────────────────
@Composable
private fun CoiRulesTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Coimbra · Modo solitario", color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text("Bot de Interferencia — fan-made por Dave Smith (skybowl, BGG).",
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        CoiRuleBlock("⚙️ Preparación",
            "Partida de 2 jugadores (tú vs. Bot) con estas excepciones:\n" +
            "• El Bot siempre va primero (todos los turnos, todas las rondas).\n" +
            "• El Bot recibe 2 cartas I aleatorias; su influencia = valor de cada carta + 1.\n" +
            "• El jugador recibe también 2 cartas I aleatorias de las 8 (sin elegir).\n" +
            "• El jugador puede elegir cualquiera de los 4 puntos de salida de Coimbra.\n" +
            "• El Bot no hace peregrinaciones.")

        CoiRuleBlock("🎲 Dados del Bot (nemotecnia: M-V-N-G-N-G)",
            "Tira 1d6 por cada dado que coja el Bot:\n" +
            "1 = Morado · 2 = Verde · 3 = Naranja bajo · 4 = Gris bajo · 5 = Naranja alto · 6 = Gris alto\n\n" +
            "Si el color no está: prueba dado blanco → sigue bajando en número.\n" +
            "«Bajo/Alto» en Gris y Naranja = menor/mayor valor entre los dos disponibles. Si solo hay uno, coge ese.")

        CoiRuleBlock("🏙️ Colocación en Ciudad",
            "Tira 1d6 por cada dado del Bot:\n" +
            "1-2 = Ciudad Alta · 3-4 = Ciudad Central · 5-6 = Ciudad Baja\n\n" +
            "Restricciones: máx. 2 dados del Bot por zona · no colocar donde haya 4+ elementos totales (salvo que el dado garantice carta al Bot).\n" +
            "Si la zona está bloqueada → reasignar: 1-3 Ciudad Alta, 4-6 Ciudad Central.")

        CoiRuleBlock("🏰 Castillo",
            "Solo si el jugador colocó un dado en el Castillo este turno:\n\n" +
            "Compara el 7º dado sobrante con tu dado del Castillo.\n" +
            "• 7º dado ≥ tu dado → ignorar, no pasa nada.\n" +
            "• 7º dado < tu dado → el Bot roba una ficha de favor antes que tú (tira 1d4 para saber cuál). El Bot usa 4 dados ese turno.")

        CoiRuleBlock("🃏 Cartas",
            "Las 5 fichas de dado toman cartas normalmente (mayor influencia, empate al más a la izquierda).\n\n" +
            "Cuando le toca al Bot: tira un dado entre las cartas disponibles (1-4, 1-3, 1-2, o la última).\n\n" +
            "Influencia Bot = valor influencia de la carta + 1 (rango 2–5).\n" +
            "El Bot no activa ningún otro efecto de las cartas.")

        CoiRuleBlock("👑 Coronas",
            "El jugador solo tiene 1 corona por ir 2º en orden de turno + las coronas obtenidas de fichas de favor del turno actual de puntuación.\n" +
            "No usar fichas de corona durante la partida (opcional: usarlas en la última ronda).")

        CoiRuleBlock("🧍 Cartas de personaje con robo",
            "Si una carta del jugador permite robar recursos o PV de otro jugador → siempre recibe la opción sin PV.")

        CoiRuleBlock("🏆 Tabla de resultados",
            "< 130 → Mal resultado\n" +
            "130–160 → Aceptable\n" +
            "160–190 → Bueno\n" +
            "190–220 → Excelente\n" +
            "> 220 → ¡Increíble!")

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CoiRuleBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = CoiAmber, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ─── Tab Setup ─────────────────────────────────────────────────────
@Composable
private fun CoiSetupTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Preparación · Coimbra", color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text("2 a 4 jugadores · Portugal s. XV",
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        CoiRuleBlock("🗺️ Tablero general",
            "• Despliega el tablero principal y el tablero de suministro.\n" +
            "• Coloca los 6 dados del color correspondiente en el espacio central.\n" +
            "• Reparte las cartas de personaje I del mazo inicial.\n" +
            "• Coloca las fichas de favor en sus espacios.\n" +
            "• Rellena el mercado de cartas con las cartas de los 4 monasterios.\n" +
            "• Marca la ronda 1 en el track de rondas.")
        CoiRuleBlock("👤 Cada jugador recibe",
            "• 1 tablero personal de doble capa.\n" +
            "• 3 portadados → en los espacios de persiana del tablero.\n" +
            "• 2 marcadores de cubo → posición 7 del track de guardias y track de monedas.\n" +
            "• 1 disco → casilla 0 del track de PV del tablero principal.\n" +
            "• 4 discos → casilla 0 de cada uno de los 4 tracks de influencia.\n" +
            "• 7 monedas + 7 guardias (sobre el tablero personal).\n" +
            "• 2 cartas de personaje I del mazo inicial (aleatorias).")
        CoiRuleBlock("⚙️ Solo mode — ajustes adicionales",
            "• El Bot recibe 2 cartas I aleatorias. Su influencia = valor de carta + 1.\n" +
            "• El jugador recibe también 2 cartas I aleatorias (sin elección).\n" +
            "• El Bot NO coloca portadados — usa el dado sobrante (7º) para el Castillo.\n" +
            "• El jugador elige cualquiera de los 4 puntos de salida desde Coimbra.")
        CoiRuleBlock("🎲 Orden de turno",
            "• El Bot siempre va PRIMERO en todos los turnos de todas las rondas.\n" +
            "• El jugador va segundo, con 1 corona fija por ser 2º en orden de turno.")
    }
}
