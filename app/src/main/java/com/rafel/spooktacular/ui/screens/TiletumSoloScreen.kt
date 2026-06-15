package com.rafel.spooktacular.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rafel.spooktacular.R
import com.rafel.spooktacular.ui.theme.*
import com.rafel.spooktacular.ui.util.CardSoundPlayer

// ── Colores ───────────────────────────────────────────────────────────────────

private val TilBg     = Color(0xFF1A1208)
private val TilCard   = Color(0xFF2C2010)
private val TilBorder = Color(0xFF5C4820)
private val TilGold   = Color(0xFFD4A017)
private val TilRed    = Color(0xFFAA2222)

// ── Colores de dados ──────────────────────────────────────────────────────────

enum class DiceColor(val label: String, val color: Color) {
    DARK("Gris oscuro", Color(0xFF444444)),
    BLUE("Azul/Gris claro", Color(0xFF4488BB)),
    YELLOW("Amarillo", Color(0xFFCCAA00)),
    PINK("Rosa", Color(0xFFCC4488));
    companion object { val ALL = entries }
}

// ── Modelo carta Titus ────────────────────────────────────────────────────────

data class TitusCard(
    val name: String,
    val imageRes: Int,
    val scoringFormula: String,
    val priorities: List<String>,
    val diceOrder: List<DiceColor>,
    val isChallenge: Boolean = false
)

// ── Mazo base (13 cartas de acción, banner dorado) ────────────────────────────

private val BASE_DECK = listOf(
    TitusCard(
        name = "Los edificios son para siempre",
        imageRes = R.drawable.tiletum_10,
        scoringFormula = "6 × Pilares × Casas",
        priorities = listOf(
            "Si Pilares > Casas → Acción de Comerciante 🏠",
            "Acción de Arquitecto 🏛️",
            "Si Pilares > Catedrales → selecciona dado Gris oscuro más alto"
        ),
        diceOrder = listOf(DiceColor.DARK, DiceColor.YELLOW, DiceColor.BLUE, DiceColor.PINK)
    ),
    TitusCard(
        name = "Rey y país",
        imageRes = R.drawable.tiletum_11,
        scoringFormula = "3 escudos + X coronas",
        priorities = listOf("Acción de Rey 👑"),
        diceOrder = listOf(DiceColor.YELLOW, DiceColor.PINK, DiceColor.BLUE, DiceColor.DARK)
    ),
    TitusCard(
        name = "Imperio mercantil",
        imageRes = R.drawable.tiletum_12,
        scoringFormula = "2 × Casas",
        priorities = listOf("Acción de Comerciante 🏠"),
        diceOrder = listOf(DiceColor.BLUE, DiceColor.PINK, DiceColor.YELLOW, DiceColor.DARK)
    ),
    TitusCard(
        name = "Alianzas familiares",
        imageRes = R.drawable.tiletum_13,
        scoringFormula = "6 × Contratos",
        priorities = listOf(
            "Si hay ≥1 Contrato sin cumplir → dado con mayor demanda (gris/amarillo, más dados del mismo color)",
            "Nuevo Escudo al alcance → Acción de Comerciante 🏠",
            "Nuevo Escudo al alcance → Acción de Arquitecto 🏛️",
            "Acción de Contrato 📜"
        ),
        diceOrder = listOf(DiceColor.BLUE, DiceColor.YELLOW, DiceColor.PINK, DiceColor.DARK)
    ),
    TitusCard(
        name = "Los negocios son los negocios",
        imageRes = R.drawable.tiletum_14,
        scoringFormula = "3 × Contratos",
        priorities = listOf(
            "Si hay ≥1 Contrato sin cumplir → dado con mayor demanda (gris/amarillo)",
            "Acción de Contrato 📜",
            "Dado Azul o Gris claro más alto"
        ),
        diceOrder = listOf(DiceColor.YELLOW, DiceColor.BLUE, DiceColor.DARK, DiceColor.PINK)
    ),
    TitusCard(
        name = "Comunidades religiosas",
        imageRes = R.drawable.tiletum_15,
        scoringFormula = "5 × Catedrales",
        priorities = listOf(
            "Si Pilares > Catedrales → dado Gris oscuro más alto",
            "Si hay ≥1 Contrato sin cumplir → dado con mayor demanda",
            "Acción de Arquitecto 🏛️",
            "Acción de Contrato 📜"
        ),
        diceOrder = listOf(DiceColor.DARK, DiceColor.BLUE, DiceColor.YELLOW, DiceColor.PINK)
    ),
    TitusCard(
        name = "Nobleza obliga",
        imageRes = R.drawable.tiletum_16,
        scoringFormula = "3 × Escudos",
        priorities = listOf(
            "Nuevo Escudo al alcance → Acción de Arquitecto 🏛️",
            "Nuevo Escudo al alcance → Acción de Comerciante 🏠",
            "Nuevo Escudo en la oferta de Contratos → Acción de Contrato 📜"
        ),
        diceOrder = listOf(DiceColor.PINK, DiceColor.DARK, DiceColor.BLUE, DiceColor.YELLOW)
    ),
    TitusCard(
        name = "Pilares orgullosos",
        imageRes = R.drawable.tiletum_17,
        scoringFormula = "2 × Pilares",
        priorities = listOf(
            "Acción de Arquitecto 🏛️",
            "Si Pilares > Casas → Acción de Comerciante 🏠"
        ),
        diceOrder = listOf(DiceColor.DARK, DiceColor.PINK, DiceColor.BLUE, DiceColor.YELLOW)
    ),
    TitusCard(
        name = "Caras conocidas",
        imageRes = R.drawable.tiletum_18,
        scoringFormula = "3 × Personajes (máx. 3/2)",
        priorities = listOf(
            "Acción de Personaje 👤",
            "Dado Rosa más alto"
        ),
        diceOrder = listOf(DiceColor.PINK, DiceColor.YELLOW, DiceColor.BLUE, DiceColor.DARK)
    ),
    TitusCard(
        name = "Manos ocupadas",
        imageRes = R.drawable.tiletum_19,
        scoringFormula = "5 × Contratos completados",
        priorities = listOf(
            "Acción de Personaje 👤",
            "Dado Rosa más alto"
        ),
        diceOrder = listOf(DiceColor.PINK, DiceColor.BLUE, DiceColor.YELLOW, DiceColor.DARK)
    ),
    TitusCard(
        name = "Chapiteles gloriosos",
        imageRes = R.drawable.tiletum_20,
        scoringFormula = "4 × Catedrales",
        priorities = listOf(
            "Si Pilares > Catedrales → dado Gris oscuro más alto",
            "Acción de Arquitecto 🏛️"
        ),
        diceOrder = listOf(DiceColor.DARK, DiceColor.YELLOW, DiceColor.PINK, DiceColor.BLUE)
    ),
    TitusCard(
        name = "Fama imperecedera",
        imageRes = R.drawable.tiletum_21,
        scoringFormula = "Casas × Pilares (si Casas ≥ Pilares)",
        priorities = listOf(
            "Si Casas ≥ Pilares → Acción de Arquitecto 🏛️",
            "Acción de Comerciante 🏠"
        ),
        diceOrder = listOf(DiceColor.DARK, DiceColor.BLUE, DiceColor.PINK, DiceColor.YELLOW)
    ),
    TitusCard(
        name = "Riquezas mundanas",
        imageRes = R.drawable.tiletum_22,
        scoringFormula = "Puntos por recurso más demandado",
        priorities = listOf("Dado de cualquier color más alto (elige el más demandado)"),
        diceOrder = DiceColor.ALL
    )
)

// ── Cartas de desafío (8, banner azul) ───────────────────────────────────────

private val CHALLENGE_CARDS = listOf(
    TitusCard(
        name = "Arcas profundas",
        imageRes = R.drawable.tiletum_01,
        scoringFormula = "Dado → +1 bolsa de recursos",
        priorities = listOf("Al sacar un dado, Titus obtiene +1 bolsa de recursos adicional"),
        diceOrder = DiceColor.ALL,
        isChallenge = true
    ),
    TitusCard(
        name = "Favor a la Corte",
        imageRes = R.drawable.tiletum_02,
        scoringFormula = "Preparación: +1 corona de Rey",
        priorities = listOf("En la fase de Preparación, si el marcador de Titus está por detrás del escudo 5, avanza hasta el escudo 5"),
        diceOrder = DiceColor.ALL,
        isChallenge = true
    ),
    TitusCard(
        name = "Listos para la acción",
        imageRes = R.drawable.tiletum_03,
        scoringFormula = "Preparación: losetas de bonus extra",
        priorities = listOf("En la fase de Preparación, Titus obtiene losetas de bonificación según su posición (+3/+2/+2/+1/+1)"),
        diceOrder = DiceColor.ALL,
        isChallenge = true
    ),
    TitusCard(
        name = "Amigos muy especiales",
        imageRes = R.drawable.tiletum_04,
        scoringFormula = "Personaje+dado → 5 monedas / Contrato → Contrato",
        priorities = listOf(
            "Si Titus ejecuta Acción de Personaje y el dado > 5, obtiene 5 monedas",
            "Si Titus obtiene un Contrato, obtiene otro Contrato adicional"
        ),
        diceOrder = DiceColor.ALL,
        isChallenge = true
    ),
    TitusCard(
        name = "Clero ingenioso",
        imageRes = R.drawable.tiletum_05,
        scoringFormula = "Bolsa + Escudo → acción doble",
        priorities = listOf("Cuando Titus obtiene una bolsa de recursos, también obtiene un escudo (y viceversa)"),
        diceOrder = DiceColor.ALL,
        isChallenge = true
    ),
    TitusCard(
        name = "Ganancias eclesiásticas",
        imageRes = R.drawable.tiletum_06,
        scoringFormula = "Personaje → 1 escudo / 6 losetas → escudo bonus",
        priorities = listOf(
            "Cada vez que Titus ejecuta Acción de Personaje, obtiene 1 escudo",
            "Si tiene 6 losetas de tipos distintos, obtiene un escudo adicional"
        ),
        diceOrder = DiceColor.ALL,
        isChallenge = true
    ),
    TitusCard(
        name = "Visitantes a la Curia",
        imageRes = R.drawable.tiletum_07,
        scoringFormula = "Personaje→bolsa/escudo; Personaje+rueda→X escudo",
        priorities = listOf(
            "Personaje con bolsa → bolsa O escudo (a elegir)",
            "Personaje con rueda de acción → X escudos según posición"
        ),
        diceOrder = DiceColor.ALL,
        isChallenge = true
    ),
    TitusCard(
        name = "Susurros al oído del rey",
        imageRes = R.drawable.tiletum_08,
        scoringFormula = "Combo de acciones → +1 corona de Rey",
        priorities = listOf("Si Titus ejecuta Arquitecto, Comerciante, Rey o Contrato en la misma ronda, obtiene +1 corona"),
        diceOrder = DiceColor.ALL,
        isChallenge = true
    )
)

// ── Fases ─────────────────────────────────────────────────────────────────────

private enum class TilPhase { SETUP, PLAYING, KING, FAIR, FINAL }

// ── Estado de puntuación (persiste entre cambios de tab) ──────────────────────

private class TilScoringState {
    var yourInGame      by mutableStateOf(0)
    var yourHouses      by mutableStateOf(0)
    var yourPillars     by mutableStateOf(0)
    var yourBuildings   by mutableStateOf(0)   // 0‒6
    var yourResources   by mutableStateOf(0)
    var titusInGame     by mutableStateOf(0)
    var titusContracts  by mutableStateOf(0)
    var titusCathedrals by mutableStateOf(0)
}

// ── Pantalla principal ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiletumSoloScreen(onBack: () -> Unit = {}) {

    val context = LocalContext.current
    val soundPlayer = remember { CardSoundPlayer(context) }
    DisposableEffect(Unit) { onDispose { soundPlayer.release() } }

    var phase by remember { mutableStateOf(TilPhase.SETUP) }
    var challengeCount by remember { mutableIntStateOf(0) }
    var deck by remember { mutableStateOf<List<TitusCard>>(emptyList()) }
    var deckIndex by remember { mutableIntStateOf(0) }
    var currentCard by remember { mutableStateOf<TitusCard?>(null) }
    var prevCard by remember { mutableStateOf<TitusCard?>(null) }
    var round by remember { mutableIntStateOf(1) }
    var turn by remember { mutableIntStateOf(1) }
    var titusScore by remember { mutableIntStateOf(0) }
    var playerScore by remember { mutableIntStateOf(0) }
    var dieValue   by remember { mutableIntStateOf(3) }
    var dieColor   by remember { mutableStateOf(DiceColor.DARK) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tilScoring  = remember { TilScoringState() }

    val scored = if (dieColor == DiceColor.YELLOW) dieValue else dieValue * 2

    fun buildDeck(challenges: Int): List<TitusCard> {
        val challengeCards = CHALLENGE_CARDS.shuffled().take(challenges)
        return (BASE_DECK + challengeCards).shuffled()
    }

    fun drawCard() {
        if (deckIndex >= deck.size) {
            deck = buildDeck(challengeCount)
            deckIndex = 0
        }
        prevCard = currentCard
        currentCard = deck[deckIndex++]
    }

    fun confirmTurn() {
        titusScore += scored
        if (turn < 3) {
            turn++
            drawCard()
        } else {
            phase = TilPhase.KING
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.til_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TilGold, fontWeight = FontWeight.Bold)
                        if (selectedTab == 0 && phase == TilPhase.PLAYING)
                            Text(stringResource(R.string.til_round_turn, round, turn),
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostWhite.copy(alpha = 0.5f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTab != 0) {
                            selectedTab = 0
                        } else when (phase) {
                            TilPhase.KING    -> phase = TilPhase.PLAYING
                            TilPhase.FAIR    -> phase = TilPhase.KING
                            TilPhase.PLAYING -> phase = TilPhase.SETUP
                            else             -> onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TilGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TilBg)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = TilBg, tonalElevation = 0.dp) {
                listOf(
                    Triple(stringResource(R.string.nav_setup),   Icons.Default.Settings,    0),
                    Triple(stringResource(R.string.nav_solo),    Icons.Default.SmartToy,    1),
                    Triple(stringResource(R.string.nav_scoring), Icons.Default.EmojiEvents, 2),
                    Triple(stringResource(R.string.nav_rules),   Icons.Default.MenuBook,    3)
                ).forEach { (label, icon, idx) ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick  = { selectedTab = idx },
                        icon  = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = TilGold,
                            selectedTextColor   = TilGold,
                            unselectedIconColor = GhostWhite.copy(alpha = 0.4f),
                            unselectedTextColor = GhostWhite.copy(alpha = 0.4f),
                            indicatorColor      = TilGold.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = TilBg
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(listOf(TilBg, Color(0xFF0E0A04))))
        ) {
            when (selectedTab) {
                0 -> TilSetupTab(Modifier.padding(padding))
                1 -> when (phase) {
                    TilPhase.SETUP -> TilSetupScreen(
                        challengeCount = challengeCount,
                        onChallenge = { challengeCount = it },
                        onStart = {
                            soundPlayer.playShuffle()
                            deck = buildDeck(challengeCount)
                            deckIndex = 0; round = 1; turn = 1
                            titusScore = 0; playerScore = 10
                            drawCard()
                            phase = TilPhase.PLAYING
                        }
                    )
                    TilPhase.PLAYING -> TilPlayingScreen(
                        card = currentCard,
                        prevCard = prevCard,
                        dieValue = dieValue, dieColor = dieColor,
                        scored = scored,
                        titusScore = titusScore, playerScore = playerScore,
                        onDieValue = { dieValue = it },
                        onDieColor = { dieColor = it },
                        onConfirm = { soundPlayer.playDeal(); confirmTurn() }
                    )
                    TilPhase.KING -> TilKingScreen(
                        titusScore = titusScore, playerScore = playerScore,
                        onConfirm = { tGain ->
                            titusScore += tGain
                            phase = TilPhase.FAIR
                        }
                    )
                    TilPhase.FAIR -> TilFairScreen(
                        round = round, challengeCount = challengeCount,
                        titusScore = titusScore, playerScore = playerScore,
                        onConfirm = { fairPts ->
                            titusScore += fairPts
                            if (round < 4) {
                                soundPlayer.playShuffle()
                                round++; turn = 1
                                deck = buildDeck(challengeCount)
                                deckIndex = 0
                                drawCard()
                                phase = TilPhase.PLAYING
                            } else {
                                phase = TilPhase.FINAL
                            }
                        }
                    )
                    TilPhase.FINAL -> TilFinalScreen(
                        titusScore = titusScore, playerScore = playerScore,
                        onRestart = { phase = TilPhase.SETUP }
                    )
                }
                1 -> TilScoringContent(tilScoring)
                2 -> TilRulesContent()
            }
        }
    }
}

// ── Setup ─────────────────────────────────────────────────────────────────────

@Composable
private fun TilSetupScreen(
    challengeCount: Int, onChallenge: (Int) -> Unit, onStart: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Image(
            painter = painterResource(R.drawable.tiletum_09),
            contentDescription = "Cardenal Titus",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(0.55f).aspectRatio(0.77f)
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, TilGold, RoundedCornerShape(14.dp))
        )
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.til_solo_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TilGold, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(stringResource(R.string.til_vs_titus),
            style = MaterialTheme.typography.bodyLarge,
            color = GhostWhite.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))

        TilCard {
            Column(Modifier.padding(14.dp)) {
                Text(stringResource(R.string.til_quick_setup),
                    style = MaterialTheme.typography.titleSmall,
                    color = TilGold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                listOf(
                    stringResource(R.string.til_quick_setup_1),
                    stringResource(R.string.til_quick_setup_2),
                    stringResource(R.string.til_quick_setup_3),
                    stringResource(R.string.til_quick_setup_4)
                ).forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        TilCard {
            Column(Modifier.padding(14.dp)) {
                Text(stringResource(R.string.til_challenge_cards_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = TilGold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.til_challenge_cards_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostWhite.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0 to stringResource(R.string.til_difficulty_easy), 2 to stringResource(R.string.til_difficulty_normal), 5 to stringResource(R.string.til_difficulty_hard)).forEach { (n, label) ->
                        val sel = n == challengeCount
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (sel) TilGold.copy(alpha = 0.2f) else TilCard)
                                .border(if (sel) 2.dp else 1.dp,
                                    if (sel) TilGold else TilBorder, RoundedCornerShape(10.dp))
                                .clickable { onChallenge(n) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label,
                                color = if (sel) TilGold else GhostWhite.copy(alpha = 0.5f),
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.til_deck_total, 13 + challengeCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = TilGold.copy(alpha = 0.7f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TilGold)
        ) {
            Text(stringResource(R.string.btn_start_game_til), color = TilBg, fontWeight = FontWeight.Black)
        }
    }
}

// ── Pantalla de juego ─────────────────────────────────────────────────────────

@Composable
private fun TilPlayingScreen(
    card: TitusCard?, prevCard: TitusCard?,
    dieValue: Int, dieColor: DiceColor, scored: Int,
    titusScore: Int, playerScore: Int,
    onDieValue: (Int) -> Unit, onDieColor: (DiceColor) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Marcadores
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TilScoreChip(stringResource(R.string.til_score_you), playerScore, Color(0xFF2A6B3A), Modifier.weight(1f))
            TilScoreChip(stringResource(R.string.til_score_titus), titusScore, TilRed, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))

        if (card != null) {
            // Carta + prioridades
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen + scoring
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.weight(0.42f).aspectRatio(0.77f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp,
                                if (card.isChallenge) Color(0xFF4488BB) else TilGold,
                                RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(card.imageRes),
                            contentDescription = card.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (card.isChallenge) {
                            Box(
                                Modifier.fillMaxWidth().align(Alignment.TopCenter)
                                    .background(Color(0xFF4488BB).copy(alpha = 0.85f))
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.til_challenge_badge), fontSize = 9.sp,
                                    color = GhostWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(0.58f)) {
                        Text(card.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = TilGold, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp))
                                .background(TilGold.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("📊 ${card.scoringFormula}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TilGold, lineHeight = 16.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        // Dados preferidos
                        Text(stringResource(R.string.til_preferred_dice),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                            color = GhostWhite.copy(alpha = 0.5f))
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            card.diceOrder.take(4).forEachIndexed { i, dc ->
                                Box(
                                    Modifier.size(22.dp).clip(RoundedCornerShape(4.dp))
                                        .background(dc.color.copy(alpha = 0.8f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${i + 1}",
                                        fontSize = 9.sp, color = Color.White,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Prioridades
                TilCard {
                    Column(Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.til_titus_priorities),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                            color = TilGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        HorizontalDivider(Modifier.padding(vertical = 6.dp), color = TilBorder)
                        card.priorities.forEachIndexed { i, p ->
                            Row(
                                Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("${i + 1}.",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TilGold, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(20.dp))
                                Text(p,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GhostWhite.copy(alpha = 0.85f),
                                    lineHeight = 17.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Selector de dado
                TilCard {
                    Column(Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.til_selected_die),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                            color = TilGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        // Valor
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.til_die_value), style = MaterialTheme.typography.bodySmall,
                                color = GhostWhite.copy(alpha = 0.6f))
                            Spacer(Modifier.width(8.dp))
                            (1..6).forEach { v ->
                                val sel = v == dieValue
                                Box(
                                    Modifier.size(34.dp).clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) TilGold else TilCard)
                                        .border(1.dp, if (sel) TilGold else TilBorder, RoundedCornerShape(6.dp))
                                        .clickable { onDieValue(v) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$v", fontWeight = FontWeight.Bold,
                                        color = if (sel) TilBg else GhostWhite.copy(alpha = 0.6f))
                                }
                                Spacer(Modifier.width(3.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        // Color
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.til_die_color), style = MaterialTheme.typography.bodySmall,
                                color = GhostWhite.copy(alpha = 0.6f))
                            DiceColor.ALL.forEach { dc ->
                                val sel = dc == dieColor
                                Box(
                                    Modifier.size(if (sel) 30.dp else 24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(dc.color.copy(alpha = if (sel) 1f else 0.5f))
                                        .then(if (sel) Modifier.border(2.dp, Color.White, RoundedCornerShape(6.dp)) else Modifier)
                                        .clickable { onDieColor(dc) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        val ptText = if (dieColor == DiceColor.YELLOW)
                            stringResource(R.string.til_yellow_die_pts, dieValue)
                        else
                            stringResource(R.string.til_die_pts_x2, dieValue * 2, dieValue)
                        Text(stringResource(R.string.til_titus_scores_detail, ptText),
                            style = MaterialTheme.typography.bodySmall, color = TilGold)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Puntos + botón confirmar
            Card(
                colors = CardDefaults.cardColors(containerColor = TilRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.5.dp, TilRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.til_titus_scores), color = GhostWhite.copy(alpha = 0.6f))
                    Text("$scored PV", style = MaterialTheme.typography.headlineMedium,
                        color = TilRed, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TilGold)
            ) {
                Text(stringResource(R.string.btn_confirm_turn), color = TilBg, fontWeight = FontWeight.Black)
            }
        }
    }
}

// ── Fase del Rey ──────────────────────────────────────────────────────────────

@Composable
private fun TilKingScreen(
    titusScore: Int, playerScore: Int, onConfirm: (Int) -> Unit
) {
    var ahead by remember { mutableStateOf(false) }
    var extra by remember { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.til_king_phase), style = MaterialTheme.typography.headlineMedium,
            color = TilGold, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(20.dp))
        TilCard {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.til_king_question),
                    style = MaterialTheme.typography.bodyLarge, color = GhostWhite)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(true to stringResource(R.string.til_king_yes), false to stringResource(R.string.til_king_no)).forEach { (v, l) ->
                        val sel = v == ahead
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (sel) TilGold.copy(alpha = 0.2f) else TilCard)
                            .border(if (sel) 2.dp else 1.dp, if (sel) TilGold else TilBorder, RoundedCornerShape(10.dp))
                            .clickable { ahead = v }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center) {
                            Text(l, color = if (sel) TilGold else GhostWhite.copy(alpha = 0.5f),
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                if (ahead) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.til_king_rules),
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite.copy(alpha = 0.7f), lineHeight = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    TilStepCounter(stringResource(R.string.til_king_extra_vp), extra,
                        onInc = { extra++ }, onDec = { if (extra > -10) extra-- })
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onConfirm(if (ahead) 4 + extra else 0) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TilGold)
        ) {
            Text(stringResource(R.string.btn_continue_fair), color = TilBg, fontWeight = FontWeight.Black)
        }
    }
}

// ── Fase de Feria ─────────────────────────────────────────────────────────────

@Composable
private fun TilFairScreen(
    round: Int, challengeCount: Int,
    titusScore: Int, playerScore: Int,
    onConfirm: (Int) -> Unit
) {
    var a by remember { mutableIntStateOf(0) }
    var b by remember { mutableIntStateOf(0) }
    val hard = challengeCount >= 5
    val multi = if (hard) round + 1 else round
    val pts = (a + b) * multi
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.til_fair_phase), style = MaterialTheme.typography.headlineMedium,
            color = TilGold, fontWeight = FontWeight.Black)
        Text(stringResource(R.string.til_fair_round, round), style = MaterialTheme.typography.bodyMedium,
            color = GhostWhite.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))
        TilCard {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.til_fair_no_map),
                    style = MaterialTheme.typography.bodySmall, color = GhostWhite.copy(alpha = 0.6f))
                Spacer(Modifier.height(10.dp))
                Text(if (hard) stringResource(R.string.til_fair_formula_hard, a, b, round + 1)
                     else stringResource(R.string.til_fair_formula_normal, a, b, round),
                    style = MaterialTheme.typography.bodyMedium, color = TilGold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                TilStepCounter(stringResource(R.string.til_fair_a), a, onInc = { a++ }, onDec = { if (a > 0) a-- })
                Spacer(Modifier.height(8.dp))
                TilStepCounter(stringResource(R.string.til_fair_b), b, onInc = { b++ }, onDec = { if (b > 0) b-- })
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = TilRed.copy(alpha = 0.15f)),
                    border = BorderStroke(1.5.dp, TilRed), shape = RoundedCornerShape(10.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.til_titus_scores), color = GhostWhite.copy(alpha = 0.6f))
                        Text("$pts PV", style = MaterialTheme.typography.headlineMedium,
                            color = TilRed, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onConfirm(pts) }, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TilGold)) {
            Text(if (round < 4) stringResource(R.string.btn_next_round) else stringResource(R.string.btn_final_score),
                color = TilBg, fontWeight = FontWeight.Black)
        }
    }
}

// ── Puntuación Final ──────────────────────────────────────────────────────────

@Composable
private fun TilFinalScreen(titusScore: Int, playerScore: Int, onRestart: () -> Unit) {
    var yourH by remember { mutableIntStateOf(0) }
    var yourP by remember { mutableIntStateOf(0) }
    var titusC by remember { mutableIntStateOf(0) }
    var tilusCat by remember { mutableIntStateOf(0) }

    val playerFinal = playerScore + (yourH * yourP)
    val titusExtra = titusC * tilusCat
    val titusFinal = titusScore + titusExtra
    val win = playerFinal > titusFinal

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.til_final_score_title), style = MaterialTheme.typography.headlineLarge,
            color = TilGold, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        TilCard {
            Column(Modifier.padding(14.dp)) {
                Text(stringResource(R.string.til_your_score), style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF2A6B3A), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                TilStepCounter(stringResource(R.string.til_your_houses), yourH, onInc = { yourH++ }, onDec = { if (yourH > 0) yourH-- })
                Spacer(Modifier.height(6.dp))
                TilStepCounter(stringResource(R.string.til_your_pillars), yourP, onInc = { yourP++ }, onDec = { if (yourP > 0) yourP-- })
                Spacer(Modifier.height(8.dp))
                Text("$playerScore + ${yourH}×${yourP} = $playerFinal PV",
                    style = MaterialTheme.typography.titleMedium, color = GhostWhite, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
        TilCard {
            Column(Modifier.padding(14.dp)) {
                Text(stringResource(R.string.til_titus_score_title), style = MaterialTheme.typography.titleSmall,
                    color = TilRed, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.til_accumulated, titusScore), style = MaterialTheme.typography.bodyMedium, color = GhostWhite)
                Spacer(Modifier.height(6.dp))
                TilStepCounter(stringResource(R.string.til_titus_contracts), titusC, onInc = { titusC++ }, onDec = { if (titusC > 0) titusC-- })
                Spacer(Modifier.height(6.dp))
                TilStepCounter(stringResource(R.string.til_titus_cathedrals), tilusCat, onInc = { tilusCat++ }, onDec = { if (tilusCat > 0) tilusCat-- })
                Spacer(Modifier.height(8.dp))
                Text("$titusScore + ${titusC}×${tilusCat} = $titusFinal PV",
                    style = MaterialTheme.typography.titleMedium, color = TilRed, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = if (win) Color(0xFF2A6B3A).copy(alpha = 0.2f) else TilRed.copy(alpha = 0.2f)),
            border = BorderStroke(2.dp, if (win) Color(0xFF2A6B3A) else TilRed),
            shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (win) stringResource(R.string.til_result_win) else stringResource(R.string.til_result_lose),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (win) Color(0xFF4AEE7A) else TilRed, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.til_you_vs_titus, playerFinal, titusFinal),
                    style = MaterialTheme.typography.bodyLarge, color = GhostWhite)
            }
        }
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, TilGold)) {
            Text(stringResource(R.string.btn_new_game), color = TilGold)
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
private fun TilCard(content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TilCard),
        border = BorderStroke(1.dp, TilBorder), shape = RoundedCornerShape(14.dp)
    ) { content() }
}

@Composable
private fun TilScoreChip(label: String, score: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                color = color, fontWeight = FontWeight.Bold)
            Text("$score PV", style = MaterialTheme.typography.titleMedium,
                color = GhostWhite, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TilStepCounter(label: String, value: Int, onInc: () -> Unit, onDec: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = GhostWhite.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
        IconButton(onClick = onDec, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Remove, null, tint = TilGold)
        }
        Text("$value", style = MaterialTheme.typography.titleMedium, color = GhostWhite,
            fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        IconButton(onClick = onInc, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Add, null, tint = TilGold)
        }
    }
}

// ── Tab: Puntuación ───────────────────────────────────────────────────────────

private val BUILD_VP = intArrayOf(0, 0, 0, 5, 10, 20, 30)

@Composable
private fun TilScoringContent(s: TilScoringState) {
    val bld         = s.yourBuildings.coerceIn(0, 6)
    val buildVP     = BUILD_VP[bld]
    val resVP       = s.yourResources / 4
    val housePillar = s.yourHouses * s.yourPillars
    val yourTotal   = s.yourInGame + housePillar + buildVP + resVP
    val titusTotal  = s.titusInGame + s.titusContracts * s.titusCathedrals
    val win = yourTotal > titusTotal
    val tie = yourTotal == titusTotal

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.til_score_tab_title),
            style = MaterialTheme.typography.titleMedium,
            color = TilGold, fontWeight = FontWeight.Black)

        // ── Jugador ───────────────────────────────────────────────────────────
        TilCard {
            Column(Modifier.padding(14.dp)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.til_you_score_section), style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF4AEE7A), fontWeight = FontWeight.Bold)
                    Text("$yourTotal PV", style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4AEE7A), fontWeight = FontWeight.Black)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFF4AEE7A).copy(0.25f))

                TilStepCounter(stringResource(R.string.til_vp_in_game), s.yourInGame,
                    onInc = { s.yourInGame++ }, onDec = { if (s.yourInGame > 0) s.yourInGame-- })

                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.til_houses_x_pillars),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = GhostWhite.copy(0.5f))
                TilStepCounter(stringResource(R.string.til_houses), s.yourHouses,
                    onInc = { s.yourHouses++ }, onDec = { if (s.yourHouses > 0) s.yourHouses-- })
                TilStepCounter(stringResource(R.string.til_pillars), s.yourPillars,
                    onInc = { s.yourPillars++ }, onDec = { if (s.yourPillars > 0) s.yourPillars-- })
                Text("${s.yourHouses} × ${s.yourPillars} = $housePillar PV",
                    style = MaterialTheme.typography.bodySmall, color = TilGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)

                Spacer(Modifier.height(6.dp))
                TilStepCounter(stringResource(R.string.til_buildings), bld,
                    onInc = { if (s.yourBuildings < 6) s.yourBuildings++ },
                    onDec = { if (s.yourBuildings > 0) s.yourBuildings-- })
                Text("Tabla: 0·0·0·5·10·20·30 PV  →  $buildVP PV",
                    style = MaterialTheme.typography.bodySmall, color = TilGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)

                Spacer(Modifier.height(6.dp))
                TilStepCounter(stringResource(R.string.til_resources_div4), s.yourResources,
                    onInc = { s.yourResources++ }, onDec = { if (s.yourResources > 0) s.yourResources-- })
                Text("${s.yourResources} ÷ 4 = $resVP PV",
                    style = MaterialTheme.typography.bodySmall, color = TilGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }
        }

        // ── Titus ─────────────────────────────────────────────────────────────
        TilCard {
            Column(Modifier.padding(14.dp)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("🤖 ${stringResource(R.string.til_score_titus)}", style = MaterialTheme.typography.titleSmall,
                        color = TilRed, fontWeight = FontWeight.Bold)
                    Text("$titusTotal PV", style = MaterialTheme.typography.titleMedium,
                        color = TilRed, fontWeight = FontWeight.Black)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = TilRed.copy(0.25f))

                TilStepCounter(stringResource(R.string.til_vp_in_game), s.titusInGame,
                    onInc = { s.titusInGame++ }, onDec = { if (s.titusInGame > 0) s.titusInGame-- })

                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.til_contracts_x_cathedrals),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = GhostWhite.copy(0.5f))
                TilStepCounter(stringResource(R.string.til_titus_contracts), s.titusContracts,
                    onInc = { s.titusContracts++ }, onDec = { if (s.titusContracts > 0) s.titusContracts-- })
                TilStepCounter(stringResource(R.string.til_titus_cathedrals), s.titusCathedrals,
                    onInc = { s.titusCathedrals++ }, onDec = { if (s.titusCathedrals > 0) s.titusCathedrals-- })
                Text("${s.titusContracts} × ${s.titusCathedrals} = ${s.titusContracts * s.titusCathedrals} PV",
                    style = MaterialTheme.typography.bodySmall, color = TilRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }
        }

        // ── Resultado ─────────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (win) Color(0xFF2A6B3A).copy(alpha = 0.15f)
                                 else if (tie) TilGold.copy(alpha = 0.1f)
                                 else TilRed.copy(alpha = 0.15f)
            ),
            border = BorderStroke(2.dp, if (win) Color(0xFF4AEE7A) else if (tie) TilGold else TilRed),
            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (win) stringResource(R.string.til_result_win) else if (tie) stringResource(R.string.til_result_tie) else stringResource(R.string.til_result_lose),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (win) Color(0xFF4AEE7A) else if (tie) TilGold else TilRed,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.til_you_vs_titus, yourTotal, titusTotal),
                    style = MaterialTheme.typography.bodyMedium, color = GhostWhite)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ── Tab: Reglas ───────────────────────────────────────────────────────────────

@Composable
private fun TilRulesContent() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.til_rules_title), style = MaterialTheme.typography.titleMedium,
            color = TilGold, fontWeight = FontWeight.Black)

        TilRuleCard(stringResource(R.string.til_rules_turn_title)) {
            listOf(
                stringResource(R.string.til_rules_turn_1),
                stringResource(R.string.til_rules_turn_2),
                stringResource(R.string.til_rules_turn_3),
                stringResource(R.string.til_rules_turn_4),
                stringResource(R.string.til_rules_turn_5)
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }

        TilRuleCard(stringResource(R.string.til_rules_dice_title)) {
            listOf(
                stringResource(R.string.til_rules_dice_1),
                stringResource(R.string.til_rules_dice_2),
                stringResource(R.string.til_rules_dice_3)
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }

        TilRuleCard(stringResource(R.string.til_rules_phases_title)) {
            listOf(
                stringResource(R.string.til_rules_phases_1),
                stringResource(R.string.til_rules_phases_2),
                stringResource(R.string.til_rules_phases_3),
                stringResource(R.string.til_rules_phases_4)
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }

        TilRuleCard(stringResource(R.string.til_rules_player_score_title)) {
            listOf(
                stringResource(R.string.til_rules_player_score_1),
                stringResource(R.string.til_rules_player_score_2),
                stringResource(R.string.til_rules_player_score_3),
                stringResource(R.string.til_rules_player_score_4)
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }

        TilRuleCard(stringResource(R.string.til_rules_titus_score_title)) {
            listOf(
                stringResource(R.string.til_rules_titus_score_1),
                stringResource(R.string.til_rules_titus_score_2)
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TilRuleCard(title: String, content: @Composable () -> Unit) {
    TilCard {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                color = TilGold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

// ─── Tab Setup ─────────────────────────────────────────────────────
@Composable
internal fun TilSetupTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.til_setup_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.til_setup_subtitle),
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        TilSetupBlock(stringResource(R.string.til_setup_board_title), stringResource(R.string.til_setup_board_body))
        TilSetupBlock(stringResource(R.string.til_setup_player_title), stringResource(R.string.til_setup_player_body))
        TilSetupBlock(stringResource(R.string.til_setup_coins_title), stringResource(R.string.til_setup_coins_body))
        TilSetupBlock(stringResource(R.string.til_setup_solo_title), stringResource(R.string.til_setup_solo_body))
    }
}

@Composable
private fun TilSetupBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF201708)),
        border = androidx.compose.foundation.BorderStroke(1.dp, TilGold.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = TilGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
