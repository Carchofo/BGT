package com.rafel.bgt.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rafel.bgt.R
import com.rafel.bgt.ui.theme.*
import com.rafel.bgt.ui.util.CardSoundPlayer

// ── Datos de cartas ───────────────────────────────────────────────────────────

data class KilltronCard(
    val name: String,
    val imageRes: Int,
    val deckLabel: String,
    val deckColor: Color,
    val description: String,
    val steps: List<Pair<String, String>> // Pair(emoji, texto)
)

val DECK_A_CARDS = listOf(
    KilltronCard(
        name = "Forcejeo", imageRes = R.drawable.card_forcejeo,
        deckLabel = "Mazo A", deckColor = Color(0xFFCC4400),
        description = "¡Mueve a una figura desde una estancia conectada a la estancia donde estás!",
        steps = listOf(
            "🤖" to "Killtron intenta mover a un jugador de una estancia conectada a la suya.",
            "🏆" to "Si hay empate, elige al jugador con más puntos.",
            "👻" to "Si no puede mover ningún jugador, mueve un espectador que no tenga en su pila de devorados.",
            "🎨" to "Prioridad de colores: Rojo → Amarillo → Verde → Azul → Morado."
        )
    ),
    KilltronCard(
        name = "Despliegue", imageRes = R.drawable.card_despliegue,
        deckLabel = "Mazo A", deckColor = Color(0xFFCC4400),
        description = "¡Mueve! Killtron se desplaza según la carta final del turno.",
        steps = listOf(
            "👁️" to "Si la carta final es Exterminar o Acosar → va a la estancia con más espectadores.",
            "🍖" to "Si la carta final es Comer → va a la estancia con más colores distintos que no tiene en su pila.",
            "🚪" to "En caso de empate, elige la estancia con más puertas.",
            "➡️" to "Solo se mueve a estancias adyacentes conectadas por una puerta."
        )
    ),
    KilltronCard(
        name = "Sobresalto", imageRes = R.drawable.card_sobresalto,
        deckLabel = "Mazo A", deckColor = Color(0xFFCC4400),
        description = "¡Mueve! ¡Juega 1 carta! Killtron se desplaza y ejecuta una acción extra.",
        steps = listOf(
            "➡️" to "Killtron se mueve (mismas reglas que Despliegue, según la carta final del turno).",
            "🃏" to "Después revela y ejecuta la carta superior del Mazo A.",
            "⚠️" to "La carta extra se resuelve completamente antes de continuar.",
            "🔄" to "Si el Mazo A está vacío, se rebaraja antes de robar la carta extra."
        )
    )
)

val DECK_B_CARDS = listOf(
    KilltronCard(
        name = "Avalancha", imageRes = R.drawable.card_avalancha,
        deckLabel = "Mazo B", deckColor = Color(0xFF6622BB),
        description = "¡Mueve a todos los monstruos de tu estancia! ¡+2 puntos por cada jugador movido!",
        steps = listOf(
            "💥" to "Killtron mueve a TODOS los jugadores que estén en su estancia.",
            "🏠" to "Los envía a la estancia con menos espectadores.",
            "🚪" to "En caso de empate, elige la estancia con menos puertas.",
            "🏅" to "Gana 2 puntos por cada jugador movido. ¡Puede acumular varios puntos de golpe!"
        )
    ),
    KilltronCard(
        name = "Buscar", imageRes = R.drawable.card_buscar,
        deckLabel = "Mazo B", deckColor = Color(0xFF6622BB),
        description = "¡Añade 3 espectadores! ¡Juega 1 carta! Killtron llena el tablero y actúa de nuevo.",
        steps = listOf(
            "👥" to "Añade 3 espectadores del saco a la estancia con menos figuras totales.",
            "🤝" to "En caso de empate, tú eliges dónde van los espectadores.",
            "🃏" to "Después revela y ejecuta la carta superior del Mazo B.",
            "🔄" to "Si el Mazo B está vacío, se rebaraja antes de robar la carta extra."
        )
    ),
    KilltronCard(
        name = "Arco Voltaico", imageRes = R.drawable.card_arco_voltaico,
        deckLabel = "Mazo B", deckColor = Color(0xFF6622BB),
        description = "¡Róbale 1 punto a cada jugador de tu estancia y de las estancias conectadas!",
        steps = listOf(
            "⚡" to "Identifica todos los jugadores en la estancia de Killtron.",
            "🔗" to "También afecta a los jugadores en estancias directamente conectadas por una puerta.",
            "📉" to "Cada jugador afectado pierde 1 punto. Killtron gana esos puntos.",
            "⚠️" to "Un jugador con 0 puntos no pierde puntos (no se va a negativo)."
        )
    )
)

val DECK_C_CARDS = listOf(
    KilltronCard(
        name = "Acosar", imageRes = R.drawable.card_acosar,
        deckLabel = "Mazo C", deckColor = Color(0xFF1A7A50),
        description = "¡Asusta a los espectadores y luego vuelve a asustar! Killtron puntúa dos veces.",
        steps = listOf(
            "👻" to "1ª ronda: por cada puerta, mueve 1 espectador a la estancia conectada. +1 pto por espectador.",
            "🧭" to "Empieza por la puerta de arriba y sigue en sentido horario.",
            "🎨" to "Prioridad de colores: Rojo → Amarillo → Verde → Azul → Morado.",
            "👻" to "2ª ronda: repite el proceso completo de asustar una segunda vez en la misma acción."
        )
    ),
    KilltronCard(
        name = "Exterminar", imageRes = R.drawable.card_exterminar,
        deckLabel = "Mazo C", deckColor = Color(0xFF1A7A50),
        description = "¡Asusta a los espectadores! Killtron genera puntos inmediatos.",
        steps = listOf(
            "👻" to "Por cada puerta de su estancia, mueve 1 espectador a la estancia conectada.",
            "🧭" to "Empieza por la puerta de arriba y continúa en sentido horario.",
            "🎨" to "Prioridad de colores: Rojo → Amarillo → Verde → Azul → Morado.",
            "🏅" to "Gana 1 punto por cada espectador que haya pasado por una puerta."
        )
    ),
    KilltronCard(
        name = "Comer", imageRes = R.drawable.card_comer,
        deckLabel = "Mazo C", deckColor = Color(0xFF1A7A50),
        description = "¡Devora a 1 espectador de cada color! Completa un set y gana un ticket.",
        steps = listOf(
            "🍖" to "Killtron devora 1 espectador de cada color disponible en su estancia.",
            "🎨" to "Prioridad: Rojo → Amarillo → Verde → Azul → Morado.",
            "🎟️" to "Si tiene 1 de cada color (5 colores): entrega el set y gana un ticket (puntos ocultos al final).",
            "👥" to "Tras completar un set, añade 3 espectadores a la estancia con menos figuras."
        )
    )
)

// ── Fases ─────────────────────────────────────────────────────────────────────

private enum class SoloPhase { READY, PLAYING }

// ── Pantalla principal ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloModeScreen(onBack: () -> Unit = {}) {

    val context = LocalContext.current
    val soundPlayer = remember { CardSoundPlayer(context) }
    DisposableEffect(Unit) { onDispose { soundPlayer.release() } }

    // Mazos barajados — se rebarajan solos al agotarse
    var queueA by remember { mutableStateOf(DECK_A_CARDS.shuffled()) }
    var queueB by remember { mutableStateOf(DECK_B_CARDS.shuffled()) }
    var queueC by remember { mutableStateOf(DECK_C_CARDS.shuffled()) }
    var idxA   by remember { mutableIntStateOf(0) }
    var idxB   by remember { mutableIntStateOf(0) }
    var idxC   by remember { mutableIntStateOf(0) }

    var phase      by remember { mutableStateOf(SoloPhase.READY) }
    var cardIndex  by remember { mutableIntStateOf(0) }   // 0=A, 1=B, 2=C
    var turnNumber by remember { mutableIntStateOf(1) }
    var turnCards  by remember { mutableStateOf<List<KilltronCard>>(emptyList()) }
    var playerScore  by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Intercepta el botón atrás del sistema mientras se juega
    BackHandler(enabled = phase == SoloPhase.PLAYING) {
        phase = SoloPhase.READY
    }

    fun drawTurn(): List<KilltronCard> {
        if (idxA >= queueA.size) { queueA = DECK_A_CARDS.shuffled(); idxA = 0 }
        if (idxB >= queueB.size) { queueB = DECK_B_CARDS.shuffled(); idxB = 0 }
        if (idxC >= queueC.size) { queueC = DECK_C_CARDS.shuffled(); idxC = 0 }
        return listOf(queueA[idxA++], queueB[idxB++], queueC[idxC++])
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (phase == SoloPhase.READY) stringResource(R.string.killtron_title) else stringResource(R.string.killtron_turn_card, turnNumber, cardIndex + 1),
                        style = MaterialTheme.typography.titleLarge,
                        color = GhostWhite, fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (phase == SoloPhase.PLAYING) { phase = SoloPhase.READY }
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = HalloweenOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MidnightBlue, tonalElevation = 0.dp) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HalloweenOrange, selectedTextColor = HalloweenOrange,
                    indicatorColor = HalloweenOrange.copy(alpha = 0.15f),
                    unselectedIconColor = GhostWhite.copy(alpha = 0.4f),
                    unselectedTextColor = GhostWhite.copy(alpha = 0.4f)
                )
                NavigationBarItem(
                    selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text(stringResource(R.string.nav_setup)) },
                    colors = navColors
                )
                NavigationBarItem(
                    selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.SmartToy, null) },
                    label = { Text(stringResource(R.string.nav_game)) },
                    colors = navColors
                )
                NavigationBarItem(
                    selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.EmojiEvents, null) },
                    label = { Text("Puntos") },
                    colors = navColors
                )
            }
        },
        containerColor = MidnightBlue
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(listOf(MidnightBlue, Color(0xFF0D0D1A))))
        ) {
            when (selectedTab) {
                0 -> SpookySetupTab(Modifier.fillMaxSize())
                2 -> SpooktacularScoreTab(
                    playerScore = playerScore,
                    onScoreChange = { playerScore = it },
                    onReset = { playerScore = 0 }
                )
                else -> when (phase) {
                    SoloPhase.READY -> ReadyScreen(
                        turnNumber = turnNumber,
                        onStart = {
                            soundPlayer.playShuffle()
                            turnCards = drawTurn()
                            cardIndex = 0
                            phase = SoloPhase.PLAYING
                        }
                    )
                    SoloPhase.PLAYING -> PlayingScreen(
                        cards = turnCards,
                        cardIndex = cardIndex,
                        onSelectCard = { i ->
                            soundPlayer.playFlip()
                            cardIndex = i
                        },
                        onEndTurn = {
                            turnNumber++
                            phase = SoloPhase.READY
                        }
                    )
                }
            }
        }
    }
}

// ── Pantalla READY — reverso de carta como botón ──────────────────────────────

@Composable
private fun ReadyScreen(turnNumber: Int, onStart: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (turnNumber > 1) {
            Text(
                stringResource(R.string.killtron_turn_label, turnNumber),
                style = MaterialTheme.typography.headlineMedium,
                color = HalloweenOrange, fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.killtron_executing),
                style = MaterialTheme.typography.bodyMedium,
                color = GhostWhite.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(32.dp))
        } else {
            Text(
                stringResource(R.string.killtron_name),
                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 3.sp),
                color = HalloweenOrange, fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.killtron_start_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = GhostWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
        }

        // Reverso de carta — botón grande
        Box(
            Modifier
                .fillMaxWidth(0.72f)
                .aspectRatio(0.72f)
                .scale(pulse)
                .clip(RoundedCornerShape(18.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(HalloweenOrange.copy(alpha = glowAlpha), SpookyPurple.copy(alpha = glowAlpha))),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(onClick = onStart),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.card_back),
                contentDescription = stringResource(R.string.cd_start_turn),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay con texto
            Box(
                Modifier.fillMaxSize()
                    .background(NightBlack.copy(alpha = 0.35f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(NightBlack.copy(alpha = 0.7f))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (turnNumber == 1) stringResource(R.string.btn_start_game) else stringResource(R.string.btn_turn_n, turnNumber),
                        style = MaterialTheme.typography.titleMedium,
                        color = HalloweenOrange,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

// ── Localización de cartas ────────────────────────────────────────────────────

@Composable
private fun localizeCard(card: KilltronCard): KilltronCard = when (card.name) {
    "Forcejeo" -> card.copy(
        deckLabel = stringResource(R.string.deck_a_label),
        description = stringResource(R.string.card_forcejeo_desc),
        steps = listOf(
            "🤖" to stringResource(R.string.card_forcejeo_step1),
            "🏆" to stringResource(R.string.card_forcejeo_step2),
            "👻" to stringResource(R.string.card_forcejeo_step3),
            "🎨" to stringResource(R.string.card_forcejeo_step4)
        )
    )
    "Despliegue" -> card.copy(
        deckLabel = stringResource(R.string.deck_a_label),
        description = stringResource(R.string.card_despliegue_desc)
    )
    "Sobresalto" -> card.copy(
        deckLabel = stringResource(R.string.deck_a_label),
        description = stringResource(R.string.card_sobresalto_desc)
    )
    "Avalancha" -> card.copy(
        deckLabel = stringResource(R.string.deck_b_label),
        description = stringResource(R.string.card_avalancha_desc),
        steps = listOf(
            "💥" to stringResource(R.string.card_avalancha_step1),
            "🏠" to stringResource(R.string.card_avalancha_step2),
            "🚪" to stringResource(R.string.card_avalancha_step3),
            "🏅" to stringResource(R.string.card_avalancha_step4)
        )
    )
    "Buscar" -> card.copy(
        deckLabel = stringResource(R.string.deck_b_label),
        description = stringResource(R.string.card_buscar_desc),
        steps = listOf(
            "👥" to stringResource(R.string.card_buscar_step1),
            "🤝" to stringResource(R.string.card_buscar_step2),
            "🃏" to stringResource(R.string.card_buscar_step3),
            "🔄" to stringResource(R.string.card_buscar_step4)
        )
    )
    "Arco Voltaico" -> card.copy(
        deckLabel = stringResource(R.string.deck_b_label),
        description = stringResource(R.string.card_arco_desc),
        steps = listOf(
            "⚡" to stringResource(R.string.card_arco_step1),
            "🔗" to stringResource(R.string.card_arco_step2),
            "📉" to stringResource(R.string.card_arco_step3),
            "⚠️" to stringResource(R.string.card_arco_step4)
        )
    )
    "Acosar" -> card.copy(
        deckLabel = stringResource(R.string.deck_c_label),
        description = stringResource(R.string.card_acosar_desc),
        steps = listOf(
            "👻" to stringResource(R.string.card_acosar_step1),
            "🧭" to stringResource(R.string.card_acosar_step2),
            "🎨" to stringResource(R.string.card_acosar_step3),
            "👻" to stringResource(R.string.card_acosar_step4)
        )
    )
    "Exterminar" -> card.copy(
        deckLabel = stringResource(R.string.deck_c_label),
        description = stringResource(R.string.card_exterminar_desc),
        steps = listOf(
            "👻" to stringResource(R.string.card_exterminar_step1),
            "🧭" to stringResource(R.string.card_exterminar_step2),
            "🎨" to stringResource(R.string.card_exterminar_step3),
            "🏅" to stringResource(R.string.card_exterminar_step4)
        )
    )
    "Comer" -> card.copy(
        deckLabel = stringResource(R.string.deck_c_label),
        description = stringResource(R.string.card_comer_desc),
        steps = listOf(
            "🍖" to stringResource(R.string.card_comer_step1),
            "🎨" to stringResource(R.string.card_comer_step2),
            "🎟️" to stringResource(R.string.card_comer_step3),
            "👥" to stringResource(R.string.card_comer_step4)
        )
    )
    else -> card
}

// ── Resolución dinámica de movimiento según la carta final ────────────────────

@Composable
private fun resolveCard(card: KilltronCard, finalCard: KilltronCard): KilltronCard {
    val localized = localizeCard(card)
    val movStep = "➡️" to when (finalCard.name) {
        "Exterminar", "Acosar" -> stringResource(R.string.move_result_spectators, finalCard.name)
        "Comer"                -> stringResource(R.string.move_result_eat)
        else                   -> stringResource(R.string.move_result_generic)
    }
    val doorStep = "🚪" to stringResource(R.string.move_door_tiebreak)
    val adjStep  = "➡️" to stringResource(R.string.move_adjacent_only)
    return when (card.name) {
        "Despliegue" -> localized.copy(steps = listOf(movStep, doorStep, adjStep))
        "Sobresalto" -> localized.copy(
            steps = listOf(
                movStep, doorStep,
                "🃏" to stringResource(R.string.card_sobresalto_step2),
                "⚠️" to stringResource(R.string.card_sobresalto_step3),
                "🔄" to stringResource(R.string.card_sobresalto_step4)
            )
        )
        else -> localized
    }
}

// ── Pantalla PLAYING — 3 cartas reveladas a la vez, tabs clicables ────────────

@Composable
private fun PlayingScreen(
    cards: List<KilltronCard>,
    cardIndex: Int,
    onSelectCard: (Int) -> Unit,
    onEndTurn: () -> Unit
) {
    if (cards.isEmpty()) return
    val finalCard = cards.last()
    val card = resolveCard(cards[cardIndex], finalCard)  // @Composable — OK aquí

    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Tabs A / B / C — todos clicables desde el inicio ─────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cards.forEachIndexed { i, c ->
                val active = i == cardIndex
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) c.deckColor else CardBorder)
                        .clickable { onSelectCard(i) }
                        .padding(horizontal = if (active) 14.dp else 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            c.deckLabel,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                            color = if (active) GhostWhite else GhostWhite.copy(alpha = 0.5f),
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        )
                        if (!active) {
                            Text(
                                c.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = GhostWhite.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // ── Carta activa + pasos ──────────────────────────────────────────────
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .weight(0.45f)
                        .aspectRatio(0.72f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, card.deckColor, RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(card.imageRes),
                        contentDescription = card.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(0.55f)) {
                    Text(
                        card.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = GhostWhite, fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        card.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostWhite.copy(alpha = 0.75f),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, card.deckColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.card_how_killtron_executes),
                        style = MaterialTheme.typography.titleSmall,
                        color = card.deckColor, fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(color = card.deckColor.copy(alpha = 0.25f))
                    card.steps.forEachIndexed { i, (emoji, texto) ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                "${i + 1}.",
                                style = MaterialTheme.typography.labelLarge,
                                color = card.deckColor, fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(22.dp)
                            )
                            Text(emoji, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                texto,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GhostWhite.copy(alpha = 0.85f),
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Navegación entre cartas + Fin de turno ───────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { if (cardIndex > 0) onSelectCard(cardIndex - 1) },
                    enabled = cardIndex > 0,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (cardIndex > 0) CardBorder else CardBorder.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, null,
                        tint = if (cardIndex > 0) GhostWhite else GhostWhite.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_prev),
                        color = if (cardIndex > 0) GhostWhite else GhostWhite.copy(alpha = 0.3f))
                }
                Button(
                    onClick = { if (cardIndex < cards.size - 1) onSelectCard(cardIndex + 1) },
                    enabled = cardIndex < cards.size - 1,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HalloweenOrange,
                        disabledContainerColor = HalloweenOrange.copy(alpha = 0.3f)
                    )
                ) {
                    Text(stringResource(R.string.btn_next),
                        color = if (cardIndex < cards.size - 1) NightBlack else NightBlack.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, null,
                        tint = if (cardIndex < cards.size - 1) NightBlack else NightBlack.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            // Fin de turno — pequeño y discreto para evitar pulsaciones accidentales
            TextButton(
                onClick = onEndTurn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                    tint = GhostWhite.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.btn_end_turn),
                    color = GhostWhite.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ── Tab Setup ─────────────────────────────────────────────────────
@Composable
private fun SpookySetupTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.spooky_setup_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.spooky_setup_subtitle),
            color = HalloweenOrange.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall)

        SpookySetupBlock(stringResource(R.string.spooky_setup_board_title),
            stringResource(R.string.spooky_setup_board_body))
        SpookySetupBlock(stringResource(R.string.spooky_setup_player_title),
            stringResource(R.string.spooky_setup_player_body))
        SpookySetupBlock(stringResource(R.string.spooky_setup_killtron_title),
            stringResource(R.string.spooky_setup_killtron_body))
        SpookySetupBlock(stringResource(R.string.spooky_setup_objective_title),
            stringResource(R.string.spooky_setup_objective_body))
        SpookySetupBlock(stringResource(R.string.spooky_setup_soloai_title),
            stringResource(R.string.spooky_setup_soloai_body))
    }
}

@Composable
private fun SpooktacularScoreTab(playerScore: Int, onScoreChange: (Int) -> Unit, onReset: () -> Unit) {
    val bg     = Color(0xFF1A0A2E)
    val accent = HalloweenOrange
    val text   = GhostWhite

    val ranking = when {
        playerScore < 10 -> "💀 DERROTA"
        playerScore < 20 -> "😅 Sobreviviste"
        playerScore < 30 -> "🏆 Victoria"
        else             -> "⭐ Victoria Perfecta"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(bg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("🎃", fontSize = 48.sp)
        Text("Tu Puntuación", color = text, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)

        Surface(color = Color(0xFF2A0D45), shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                IconButton(onClick = { onScoreChange(playerScore - 1) }) {
                    Icon(Icons.Default.Remove, null, tint = accent, modifier = Modifier.size(32.dp))
                }
                Text("$playerScore", color = text, fontWeight = FontWeight.Black, fontSize = 48.sp)
                IconButton(onClick = { onScoreChange(playerScore + 1) }) {
                    Icon(Icons.Default.Add, null, tint = accent, modifier = Modifier.size(32.dp))
                }
            }
        }

        Text(ranking, color = accent, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall)

        Surface(color = Color(0xFF1E0D36), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("< 10" to "💀 Derrota", "10-19" to "😅 Sobreviviste",
                       "20-29" to "🏆 Victoria", "30+" to "⭐ Victoria Perfecta").forEach { (range, label) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(range, color = text.copy(0.5f), style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(48.dp))
                        Text(label, color = text, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onReset,
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.4f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Reiniciar puntuación", color = accent)
        }
    }
}

@Composable
private fun SpookySetupBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, HalloweenOrange.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = HalloweenOrange, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
