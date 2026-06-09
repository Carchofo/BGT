package com.rafel.spooktacular.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.rafel.spooktacular.R
import com.rafel.spooktacular.ui.theme.*
import com.rafel.spooktacular.ui.util.CardSoundPlayer

// ── Datos de cartas Tingent ───────────────────────────────────────────────────

data class TingentCard(
    val id: Int,
    val imageRes: Int,
    val advancesTracker: Boolean,
    val yellowAction: String,
    val brownAction: String,
    val yellowDetail: String = "",
    val brownDetail: String = ""
)

private val TINGENT_CARDS = listOf(
    TingentCard(1, R.drawable.cm_card_01, true,
        "Capitán → máx. Flor 🌸 + robar carta",
        "Cazamariposas 🦋",
        "Coloca al Capitán adyacente al mayor nº de hábitats de Flor. Gana 1PV por cada Flor adyacente. Roba 1 carta del Territorio Salvaje bocarriba (especie del objetivo).",
        "Activa el icono de efecto especial adyacente al Capitán."
    ),
    TingentCard(2, R.drawable.cm_card_02, true,
        "Capitán → máx. Seta 🍄 + robar carta",
        "Capitán → máx. Coral 🪸",
        "Coloca al Capitán adyacente al mayor nº de hábitats de Seta. Gana 1PV por cada Seta adyacente. Roba 1 carta del Territorio Salvaje bocarriba.",
        "Coloca al Capitán adyacente al mayor nº de hábitats de Coral. Gana 1PV por cada Coral adyacente."
    ),
    TingentCard(3, R.drawable.cm_card_03, true,
        "Capitán → máx. Coral 🪸 + robar carta",
        "Capitán → máx. Flor 🌸",
        "Coloca al Capitán adyacente al mayor nº de hábitats de Coral. Gana 1PV por cada Coral adyacente. Roba 1 carta del Territorio Salvaje bocarriba.",
        "Coloca al Capitán adyacente al mayor nº de hábitats de Flor. Gana 1PV por cada Flor adyacente."
    ),
    TingentCard(4, R.drawable.cm_card_04, true,
        "Roba 2 cartas bocabajo 📥📥",
        "Cazamariposas 🦋",
        "Roba 2 cartas del mazo de Criaturas y colócalas bocabajo en el área de Tingent. Gana PV por ellas al final según la dificultad.",
        "Activa el icono de efecto especial adyacente al Capitán de Tingent."
    ),
    TingentCard(5, R.drawable.cm_card_05, true,
        "Cazamariposas 🦋 + Huevo 🥚",
        "Huevo 🥚 + Cazamariposas 🦋",
        "Activa el icono de efecto especial adyacente al Capitán. Luego obtén 1 Huevo del suministro de la misma especie que la última carta bocarriba de Tingent.",
        "Obtén 1 Huevo del suministro. Luego activa el icono de efecto especial adyacente al Capitán."
    ),
    TingentCard(6, R.drawable.cm_card_06, true,
        "Tripulante + carta bocarriba 📋",
        "Capitán + carta bocarriba 📋",
        "Coloca un Tripulante en el mapa. Luego toma del Territorio Salvaje la carta de la especie objetivo con mayor Puntuación Final. Colócala bocarriba. Obtén 1 Huevo de esa especie.",
        "Coloca al Capitán en el mapa (gana PV por hábitats adyacentes al objetivo). Toma la carta objetivo del Territorio Salvaje bocarriba. Obtén 1 Huevo."
    ),
    TingentCard(7, R.drawable.cm_card_07, false,
        "Tripulante + carta bocabajo 📥",
        "Capitán + carta bocarriba 📋",
        "Coloca un Tripulante en el mapa. Luego roba 1 carta del mazo de Criaturas y colócala bocabajo. El marcador NO avanza.",
        "Coloca al Capitán en el mapa. Toma la carta objetivo del Territorio Salvaje bocarriba. Obtén 1 Huevo. El marcador NO avanza."
    ),
    TingentCard(8, R.drawable.cm_card_08, true,
        "Cazamariposas 🦋 + carta bocarriba 📋",
        "Capitán → máx. Seta 🍄 + Huevo 🥚",
        "Activa el icono de efecto especial. Toma la carta objetivo del Territorio Salvaje bocarriba. Obtén 1 Huevo de esa especie.",
        "Coloca al Capitán adyacente al mayor nº de Setas. Gana 1PV por cada Seta adyacente. Obtén 1 Huevo del suministro."
    ),
    TingentCard(9, R.drawable.cm_card_09, false,
        "Tripulante + carta bocabajo 📥",
        "Capitán + carta bocarriba 📋",
        "Coloca un Tripulante en el mapa. Roba 1 carta del mazo bocabajo. El marcador NO avanza.",
        "Coloca al Capitán en el mapa. Toma la carta objetivo del Territorio Salvaje bocarriba. Obtén 1 Huevo. El marcador NO avanza."
    ),
    TingentCard(10, R.drawable.cm_card_10, true,
        "Capitán → máx. Coral 🪸 + robar carta",
        "Capitán → máx. Flor 🌸",
        "Coloca al Capitán adyacente al mayor nº de hábitats de Coral. Gana 1PV por cada Coral adyacente. Roba 1 carta del Territorio Salvaje bocarriba.",
        "Coloca al Capitán adyacente al mayor nº de hábitats de Flor. Gana 1PV por cada Flor adyacente."
    ),
    TingentCard(11, R.drawable.cm_card_11, false,
        "Tripulante + Huevo del mapa 🥚",
        "Capitán + 2 Huevos 🥚🥚",
        "Coloca un Tripulante adyacente a un Huevo en el mapa. Tingent obtiene ese Huevo. El marcador NO avanza.",
        "Coloca al Capitán adyacente a Huevos. Tingent obtiene hasta 2 Huevos del mapa. El marcador NO avanza."
    ),
    TingentCard(12, R.drawable.cm_card_12, false,
        "Tripulante + carta bocabajo 📥",
        "Capitán + 2 cartas bocarriba 📋📋",
        "Coloca un Tripulante en el mapa. Roba 1 carta del mazo bocabajo. El marcador NO avanza.",
        "Coloca al Capitán en el mapa. Toma 2 cartas del Territorio Salvaje bocarriba (ambas de la especie objetivo si es posible). El marcador NO avanza."
    )
)

// ── Acciones del tracker ──────────────────────────────────────────────────────

enum class TrackerAction {
    NONE,           // Sin acción
    FACEUP,         // Toma carta del Territorio Salvaje bocarriba + Huevo
    FACEDOWN,       // Roba carta del mazo bocabajo
    DISCOVERY,      // Descubrimiento: loseta Hábitat + Huevo del lago
    TIME_ADVANCE    // Avanza marcador de Tiempo
}

data class TrackerActionInfo(
    val type: TrackerAction,
    val emoji: String,
    val label: String,
    val detail: String
)

val TRACKER_ACTION_INFO = mapOf(
    TrackerAction.NONE to TrackerActionInfo(TrackerAction.NONE, "", "", ""),
    TrackerAction.FACEUP to TrackerActionInfo(
        TrackerAction.FACEUP, "📋", "Carta bocarriba",
        "Toma del Territorio Salvaje la carta de la especie objetivo con mayor Puntuación Final. Colócala bocarriba. Obtén 1 Huevo de esa especie."
    ),
    TrackerAction.FACEDOWN to TrackerActionInfo(
        TrackerAction.FACEDOWN, "📥", "Carta bocabajo",
        "Roba 1 carta del mazo de Criaturas y colócala bocabajo en el área de Tingent. Gana PV por ella al final según la dificultad."
    ),
    TrackerAction.DISCOVERY to TrackerActionInfo(
        TrackerAction.DISCOVERY, "🗺️", "Descubrimiento",
        "Toma una loseta de Hábitat y colócala en el hexágono de Lago más a la izquierda con Huevo. Tingent obtiene ese Huevo."
    ),
    TrackerAction.TIME_ADVANCE to TrackerActionInfo(
        TrackerAction.TIME_ADVANCE, "⏱️", "Avanza Tiempo",
        "Avanza el marcador de la Tabla de Tiempo un espacio. Aplica inmediatamente el efecto del icono de Tiempo correspondiente."
    )
)

// Grid 4×6 = 24 posiciones. Mapeado desde las fotos del tablero físico.
// ⚠️ Verificar contra el tablero físico y corregir si es necesario.

private val TRACK_PEREZOSO = listOf( // 1★
    TrackerAction.FACEUP, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.NONE, TrackerAction.NONE,
    TrackerAction.FACEDOWN, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.NONE, TrackerAction.NONE,
    // --- zona marrón desde pos 13 ---
    TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.NONE, TrackerAction.DISCOVERY, TrackerAction.TIME_ADVANCE, TrackerAction.NONE,
    TrackerAction.FACEDOWN, TrackerAction.NONE, TrackerAction.FACEDOWN, TrackerAction.FACEDOWN, TrackerAction.FACEDOWN, TrackerAction.NONE
)

private val TRACK_FACILON = listOf( // 2★
    TrackerAction.FACEUP, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.NONE, TrackerAction.NONE,
    TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.NONE, TrackerAction.NONE,
    // --- zona marrón desde pos 10 ---
    TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.NONE, TrackerAction.DISCOVERY, TrackerAction.TIME_ADVANCE,
    TrackerAction.FACEDOWN, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEDOWN, TrackerAction.NONE
)

private val TRACK_SALVAJE = listOf( // 3★
    TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEUP, TrackerAction.NONE, TrackerAction.NONE,
    TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.DISCOVERY, TrackerAction.NONE,
    // --- zona marrón desde pos 7 ---
    TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.TIME_ADVANCE,
    TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN
)

private val TRACK_TERRORIFICO = listOf( // 4★
    TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.DISCOVERY, TrackerAction.NONE,
    TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.TIME_ADVANCE,
    // --- zona marrón desde pos 4 ---
    TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEUP,
    TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN, TrackerAction.FACEDOWN, TrackerAction.FACEUP, TrackerAction.FACEDOWN
)

// ── Dificultades ──────────────────────────────────────────────────────────────

data class TingentDifficulty(
    val name: String,
    val stars: Int,
    val startingPts: Int,
    val brownThreshold: Int,
    val ptsPerFaceDown: Int,
    val track: List<TrackerAction>
)

private val DIFFICULTIES = listOf(
    TingentDifficulty("Perezoso",    1, 1, 13, 1, TRACK_PEREZOSO),
    TingentDifficulty("Facilón",     2, 2, 10, 2, TRACK_FACILON),
    TingentDifficulty("Salvaje",     3, 3,  7, 3, TRACK_SALVAJE),
    TingentDifficulty("Terrorífico", 4, 4,  4, 3, TRACK_TERRORIFICO)
)

// ── Fases ─────────────────────────────────────────────────────────────────────

private enum class CMPhase { SETUP, PLAYING }

// ── Estado de puntuación (persiste entre cambios de tab) ──────────────────────

private class CMScoringState {
    var youEndCards   by mutableStateOf(0)
    var youCreatures  by mutableStateOf(0)
    var youAch6       by mutableStateOf(0)
    var youAch12      by mutableStateOf(0)
    var youAch15      by mutableStateOf(0)
    var youTrophies   by mutableStateOf(0)
    var youResources  by mutableStateOf(0)
    var tingEndUp     by mutableStateOf(0)
    var tingCreatUp   by mutableStateOf(0)
    var tingAch       by mutableStateOf(0)
    var tingTrophies  by mutableStateOf(0)
    var tingEggs      by mutableStateOf(0)
    var tingDown      by mutableStateOf(0)
}

// ── Colores del juego ─────────────────────────────────────────────────────────

private val YellowZone  = Color(0xFFD4A017)
private val BrownZone   = Color(0xFF7B4A1E)
private val CMBackground = Color(0xFF1A140A)
private val CMCard      = Color(0xFF2C2018)
private val CMBorder    = Color(0xFF5C4A2A)

// ── Pantalla principal ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMSoloModeScreen(onBack: () -> Unit = {}) {

    val context = LocalContext.current
    val soundPlayer = remember { CardSoundPlayer(context) }
    DisposableEffect(Unit) { onDispose { soundPlayer.release() } }

    var phase by remember { mutableStateOf(CMPhase.SETUP) }
    var difficulty by remember { mutableStateOf(DIFFICULTIES[0]) }
    var deck by remember { mutableStateOf(TINGENT_CARDS.shuffled()) }
    var deckIndex by remember { mutableIntStateOf(0) }
    var trackerPos by remember { mutableIntStateOf(1) }
    var inBrownZone by remember { mutableStateOf(false) }
    var currentCard by remember { mutableStateOf<TingentCard?>(null) }
    var currentTrackerAction by remember { mutableStateOf(TrackerAction.NONE) }
    var turnCount  by remember { mutableIntStateOf(1) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val cmScoring   = remember { CMScoringState() }

    fun drawCard() {
        if (deckIndex >= deck.size) {
            deck = TINGENT_CARDS.shuffled()
            deckIndex = 0
        }
        val card = deck[deckIndex++]
        currentCard = card
        currentTrackerAction = TrackerAction.NONE

        // Avanzar tracker si la carta tiene engranaje rojo
        if (card.advancesTracker) {
            if (trackerPos < 24) trackerPos++
            if (trackerPos >= difficulty.brownThreshold && !inBrownZone) {
                inBrownZone = true
                deck = TINGENT_CARDS.shuffled()
                deckIndex = 0
            }
            // Comprobar si la nueva posición tiene acción
            val posIdx = trackerPos - 1
            if (posIdx < difficulty.track.size) {
                currentTrackerAction = difficulty.track[posIdx]
            }
        }
        turnCount++
    }

    val zoneColor = if (inBrownZone) BrownZone else YellowZone
    val zoneName  = if (inBrownZone) "Zona Marrón" else "Zona Amarilla"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("🦋  Criaturas Maravillosas",
                        style = MaterialTheme.typography.titleMedium,
                        color = GhostWhite, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTab != 0) selectedTab = 0
                        else if (phase == CMPhase.PLAYING) phase = CMPhase.SETUP
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = YellowZone)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CMBackground)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = CMBackground, tonalElevation = 0.dp) {
                listOf(
                    Triple("Setup",      Icons.Default.Settings,   0),
                    Triple("Solitario",  Icons.Default.SmartToy,     1),
                    Triple("Puntuación", Icons.Default.EmojiEvents,  2),
                    Triple("Reglas",     Icons.Default.MenuBook,     3)
                ).forEach { (label, icon, idx) ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick  = { selectedTab = idx },
                        icon  = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = YellowZone,
                            selectedTextColor   = YellowZone,
                            unselectedIconColor = GhostWhite.copy(alpha = 0.4f),
                            unselectedTextColor = GhostWhite.copy(alpha = 0.4f),
                            indicatorColor      = YellowZone.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = CMBackground
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(listOf(CMBackground, Color(0xFF100C06))))
        ) {
            when (selectedTab) {
                0 -> CMSetupTab(Modifier.padding(padding))
                1 -> when (phase) {
                    CMPhase.SETUP -> CMSetupScreen(
                        difficulties = DIFFICULTIES,
                        selected = difficulty,
                        onSelect = { difficulty = it },
                        onStart = {
                            soundPlayer.playShuffle()
                            deck = TINGENT_CARDS.shuffled()
                            deckIndex = 0
                            trackerPos = 1
                            inBrownZone = false
                            currentCard = null
                            currentTrackerAction = TrackerAction.NONE
                            turnCount = 1
                            phase = CMPhase.PLAYING
                        }
                    )
                    CMPhase.PLAYING -> CMPlayingScreen(
                        difficulty = difficulty,
                        trackerPos = trackerPos,
                        inBrownZone = inBrownZone,
                        zoneColor = zoneColor,
                        zoneName = zoneName,
                        currentCard = currentCard,
                        trackerAction = currentTrackerAction,
                        turnCount = turnCount,
                        onDrawCard = { soundPlayer.playDeal(); drawCard() }
                    )
                }
                1 -> CMScoringContent(cmScoring, difficulty)
                2 -> CMRulesContent()
            }
        }
    }
}

// ── Pantalla de configuración ─────────────────────────────────────────────────

@Composable
private fun CMSetupScreen(
    difficulties: List<TingentDifficulty>,
    selected: TingentDifficulty,
    onSelect: (TingentDifficulty) -> Unit,
    onStart: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🦋", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text("Modo Solitario",
            style = MaterialTheme.typography.headlineMedium,
            color = YellowZone, fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center)
        Text("vs. Tingent", style = MaterialTheme.typography.bodyLarge,
            color = GhostWhite.copy(alpha = 0.5f))

        Spacer(Modifier.height(32.dp))
        Text("Nivel de dificultad",
            style = MaterialTheme.typography.titleSmall,
            color = GhostWhite.copy(alpha = 0.6f))
        Spacer(Modifier.height(12.dp))

        difficulties.forEach { diff ->
            val isSelected = diff.name == selected.name
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .clickable { onSelect(diff) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) YellowZone.copy(alpha = 0.15f) else CMCard
                ),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) YellowZone else CMBorder
                )
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "★".repeat(diff.stars) + "☆".repeat(4 - diff.stars),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) YellowZone else GhostWhite.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Tingent ${diff.name}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) GhostWhite else GhostWhite.copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        Text("${diff.startingPts} pt inicial · ${diff.ptsPerFaceDown} pv/carta bocabajo",
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostWhite.copy(alpha = 0.4f))
                    }
                    if (isSelected) {
                        Text("✓", color = YellowZone, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YellowZone)
        ) {
            Text("▶  Empezar partida",
                color = Color(0xFF100C06), fontWeight = FontWeight.Black)
        }
    }
}

// ── Pantalla de juego ─────────────────────────────────────────────────────────

@Composable
private fun CMPlayingScreen(
    difficulty: TingentDifficulty,
    trackerPos: Int,
    inBrownZone: Boolean,
    zoneColor: Color,
    zoneName: String,
    currentCard: TingentCard?,
    trackerAction: TrackerAction,
    turnCount: Int,
    onDrawCard: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Tracker de posición ───────────────────────────────────────────────
        TrackerBar(
            pos = trackerPos,
            brownThreshold = difficulty.brownThreshold,
            inBrownZone = inBrownZone,
            track = difficulty.track
        )

        Spacer(Modifier.height(12.dp))

        // ── Indicador de zona ─────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(12.dp).clip(RoundedCornerShape(3.dp))
                    .background(zoneColor)
            )
            Text(zoneName,
                style = MaterialTheme.typography.labelLarge,
                color = zoneColor, fontWeight = FontWeight.Bold)
            Text("· Turno $turnCount",
                style = MaterialTheme.typography.labelLarge,
                color = GhostWhite.copy(alpha = 0.4f))
        }

        Spacer(Modifier.height(16.dp))

        // ── Carta actual ──────────────────────────────────────────────────────
        if (currentCard != null) {
            val action = if (inBrownZone) currentCard.brownAction else currentCard.yellowAction
            val detail = if (inBrownZone) currentCard.brownDetail else currentCard.yellowDetail

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Imagen carta
                Box(
                    Modifier.weight(0.42f).aspectRatio(0.72f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, zoneColor, RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(currentCard.imageRes),
                        contentDescription = "Carta Tingent",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .align(if (inBrownZone) Alignment.BottomCenter else Alignment.TopCenter)
                            .background(zoneColor.copy(alpha = 0.85f))
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(zoneName,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp),
                            color = GhostWhite, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(0.58f).verticalScroll(rememberScrollState())) {
                    // Acción resumida
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CMCard),
                        border = BorderStroke(1.dp, zoneColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Acción",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                color = zoneColor, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(action,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GhostWhite.copy(alpha = 0.9f),
                                lineHeight = 20.sp)
                        }
                    }

                    // Detalle explicación
                    if (detail.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = zoneColor.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostWhite.copy(alpha = 0.75f),
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(8.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ¿Avanza el tracker?
                    val advanceColor = if (currentCard.advancesTracker)
                        Color(0xFFE24B4A) else GhostWhite.copy(alpha = 0.3f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(advanceColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(if (currentCard.advancesTracker) "⚙️" else "—",
                            fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (currentCard.advancesTracker) "Marcador avanza"
                            else "Marcador se queda",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                            color = advanceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Estado inicial — pide robar carta
            Box(
                Modifier.fillMaxWidth().height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CMCard)
                    .border(1.dp, CMBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🃏", fontSize = 44.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Pulsa para robar la primera carta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostWhite.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center)
                }
            }
        }

        // ── Acción adicional del tracker ──────────────────────────────────────
        if (trackerAction != TrackerAction.NONE) {
            val info = TRACKER_ACTION_INFO[trackerAction]!!
            Spacer(Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A05)),
                border = BorderStroke(1.5.dp, Color(0xFFFF6B00)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(info.emoji, fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚙️ Acción del tracker",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                color = Color(0xFFFF6B00), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Text(info.label,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                color = Color(0xFFFFB347), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(info.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostWhite.copy(alpha = 0.8f),
                            lineHeight = 17.sp)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Botón robar carta ─────────────────────────────────────────────────
        Button(
            onClick = onDrawCard,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = zoneColor)
        ) {
            Text(
                if (currentCard == null) "Robar primera carta de Tingent"
                else "Siguiente turno · Robar carta",
                color = Color(0xFF100C06),
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ── Tracker bar visual ────────────────────────────────────────────────────────

@Composable
private fun TrackerBar(pos: Int, brownThreshold: Int, inBrownZone: Boolean, track: List<TrackerAction> = emptyList()) {
    val total = 24
    Card(
        colors = CardDefaults.cardColors(containerColor = CMCard),
        border = BorderStroke(1.dp, CMBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tracker Tingent",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = GhostWhite.copy(alpha = 0.5f))
                Text("Pos. $pos / $total",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = if (inBrownZone) BrownZone else YellowZone,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            // Grid 4×6
            val cells = (1..total)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (row in 0..3) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (col in 0..5) {
                            val cellPos = row * 6 + col + 1
                            val isCurrent = cellPos == pos
                            val isDone = cellPos < pos
                            val isBrown = cellPos >= brownThreshold
                            // Colores más contrastados para distinguir zonas
                            val bgColor = when {
                                isCurrent && isBrown -> BrownZone          // marrón sólido: posición actual zona marrón
                                isCurrent            -> YellowZone         // dorado sólido: posición actual zona amarilla
                                isDone && isBrown    -> BrownZone.copy(alpha = 0.55f)
                                isDone               -> YellowZone.copy(alpha = 0.55f)
                                isBrown              -> Color(0xFF3D2010)  // marrón oscuro visible para zona marrón futura
                                else                 -> Color(0xFF3D3000)  // amarillo oscuro visible para zona amarilla futura
                            }
                            val cellAction = if (track.size >= cellPos) track[cellPos - 1] else TrackerAction.NONE
                            val actionEmoji = when(cellAction) {
                                TrackerAction.FACEUP       -> "📋"
                                TrackerAction.FACEDOWN     -> "📥"
                                TrackerAction.DISCOVERY    -> "🗺"
                                TrackerAction.TIME_ADVANCE -> "⏱"
                                TrackerAction.NONE         -> ""
                            }
                            Box(
                                Modifier.weight(1f).aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(bgColor)
                                    .then(
                                        if (isCurrent) Modifier.border(
                                            2.dp,
                                            if (isBrown) Color(0xFFFF9944) else Color(0xFFFFDD44),
                                            RoundedCornerShape(4.dp)
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isCurrent -> Text("▲",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 8.sp),
                                        color = Color(0xFF100C06))
                                    !isDone && actionEmoji.isNotEmpty() -> Text(actionEmoji,
                                        fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp))
                        .background(YellowZone))
                    Spacer(Modifier.width(4.dp))
                    Text("Zona amarilla", fontSize = 10.sp,
                        color = GhostWhite.copy(alpha = 0.4f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp))
                        .background(BrownZone))
                    Spacer(Modifier.width(4.dp))
                    Text("Zona marrón", fontSize = 10.sp,
                        color = GhostWhite.copy(alpha = 0.4f))
                }
            }
        }
    }
}

// ── Tab: Puntuación ───────────────────────────────────────────────────────────

@Composable
private fun CMScoringContent(s: CMScoringState, difficulty: TingentDifficulty) {
    val youTotal = s.youEndCards + s.youCreatures +
        s.youAch6 * 6 + s.youAch12 * 12 + s.youAch15 * 15 +
        s.youTrophies * 3 + s.youResources / 4
    val tingTotal = s.tingEndUp * 5 + s.tingCreatUp + s.tingAch +
        s.tingTrophies * 3 + s.tingEggs + s.tingDown * difficulty.ptsPerFaceDown
    val win = youTotal > tingTotal
    val tie = youTotal == tingTotal

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Conteo de Puntuación",
            style = MaterialTheme.typography.titleMedium,
            color = YellowZone, fontWeight = FontWeight.Black)
        Text("Introduce los valores al terminar la partida",
            style = MaterialTheme.typography.bodySmall,
            color = GhostWhite.copy(alpha = 0.4f))

        // Sección Jugador
        CMScoreSection("🧑 Tú", youTotal, Color(0xFF4AEE7A)) {
            CMScoreRow("Cartas Final de Partida (PV directo)", s.youEndCards, s.youEndCards,
                onInc = { s.youEndCards++ },  onDec = { if (s.youEndCards  > 0) s.youEndCards--  })
            CMScoreRow("Cartas Criatura · suma sellos rojos 🔴", s.youCreatures, s.youCreatures,
                onInc = { s.youCreatures++ }, onDec = { if (s.youCreatures > 0) s.youCreatures-- })
            CMScoreRow("Logros 6 PV  (n=${s.youAch6})",  s.youAch6,  s.youAch6  * 6,
                onInc = { s.youAch6++ },  onDec = { if (s.youAch6  > 0) s.youAch6--  })
            CMScoreRow("Logros 12 PV  (n=${s.youAch12})", s.youAch12, s.youAch12 * 12,
                onInc = { s.youAch12++ }, onDec = { if (s.youAch12 > 0) s.youAch12-- })
            CMScoreRow("Logros 15 PV  (n=${s.youAch15})", s.youAch15, s.youAch15 * 15,
                onInc = { s.youAch15++ }, onDec = { if (s.youAch15 > 0) s.youAch15-- })
            CMScoreRow("Trofeos ×3 PV  (n=${s.youTrophies})", s.youTrophies, s.youTrophies * 3,
                onInc = { s.youTrophies++ }, onDec = { if (s.youTrophies > 0) s.youTrophies-- })
            CMScoreRow("Recursos + Cazamariposas  (÷4)", s.youResources, s.youResources / 4,
                onInc = { s.youResources++ }, onDec = { if (s.youResources > 0) s.youResources-- })
        }

        // Sección Tingent
        CMScoreSection("🤖 Tingent · ${difficulty.name}", tingTotal, BrownZone) {
            CMScoreRow("Cartas FP bocarriba ×5 PV  (n=${s.tingEndUp})", s.tingEndUp, s.tingEndUp * 5,
                onInc = { s.tingEndUp++ },    onDec = { if (s.tingEndUp    > 0) s.tingEndUp--    })
            CMScoreRow("Cartas Criatura bocarriba · suma PV", s.tingCreatUp, s.tingCreatUp,
                onInc = { s.tingCreatUp++ },  onDec = { if (s.tingCreatUp  > 0) s.tingCreatUp--  })
            CMScoreRow("Logros completados · suma PV", s.tingAch, s.tingAch,
                onInc = { s.tingAch++ },      onDec = { if (s.tingAch      > 0) s.tingAch--      })
            CMScoreRow("Trofeos ×3 PV  (n=${s.tingTrophies})", s.tingTrophies, s.tingTrophies * 3,
                onInc = { s.tingTrophies++ }, onDec = { if (s.tingTrophies > 0) s.tingTrophies-- })
            CMScoreRow("Huevos ×1 PV  (n=${s.tingEggs})", s.tingEggs, s.tingEggs,
                onInc = { s.tingEggs++ },     onDec = { if (s.tingEggs     > 0) s.tingEggs--     })
            CMScoreRow(
                "Cartas bocabajo ×${difficulty.ptsPerFaceDown} PV  (n=${s.tingDown})",
                s.tingDown, s.tingDown * difficulty.ptsPerFaceDown,
                onInc = { s.tingDown++ }, onDec = { if (s.tingDown > 0) s.tingDown-- }
            )
        }

        // Resultado
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (win) Color(0xFF2A6B3A).copy(alpha = 0.15f)
                                 else if (tie) YellowZone.copy(alpha = 0.1f)
                                 else BrownZone.copy(alpha = 0.15f)
            ),
            border = BorderStroke(2.dp, if (win) Color(0xFF4AEE7A) else if (tie) YellowZone else BrownZone),
            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (win) "🎉 ¡GANASTE!" else if (tie) "🤝 EMPATE" else "💀 TINGENT GANA",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (win) Color(0xFF4AEE7A) else if (tie) YellowZone else BrownZone,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(6.dp))
                Text("Tú: $youTotal PV  ·  Tingent: $tingTotal PV",
                    style = MaterialTheme.typography.bodyMedium, color = GhostWhite)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CMScoreSection(
    title: String, total: Int, color: Color,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CMCard),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall,
                    color = color, fontWeight = FontWeight.Bold)
                Text("$total PV", style = MaterialTheme.typography.titleMedium,
                    color = color, fontWeight = FontWeight.Black)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = color.copy(alpha = 0.25f)
            )
            content()
        }
    }
}

@Composable
private fun CMScoreRow(
    label: String, count: Int, pvContrib: Int,
    onInc: () -> Unit, onDec: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = GhostWhite.copy(alpha = 0.75f),
            modifier = Modifier.weight(1f), lineHeight = 15.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDec, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Remove, null, tint = GhostWhite.copy(alpha = 0.55f),
                    modifier = Modifier.size(14.dp))
            }
            Text("$count", style = MaterialTheme.typography.bodyMedium,
                color = GhostWhite, fontWeight = FontWeight.Bold,
                modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
            IconButton(onClick = onInc, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, null, tint = GhostWhite.copy(alpha = 0.55f),
                    modifier = Modifier.size(14.dp))
            }
        }
        Text("+${pvContrib}PV",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
            color = YellowZone, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
    }
}

// ── Tab: Reglas ───────────────────────────────────────────────────────────────

@Composable
private fun CMRulesContent() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Referencia Rápida", style = MaterialTheme.typography.titleMedium,
            color = YellowZone, fontWeight = FontWeight.Black)

        CMRuleCard("🦋 Turno de Tingent") {
            listOf(
                "1. Roba una carta del mazo de Tingent",
                "2. Aplica la acción de la zona actual (amarilla o marrón)",
                "3. Si la carta tiene ⚙️ rojo → avanza el tracker",
                "4. Si el tracker llega al umbral → entra en Zona Marrón y baraja de nuevo",
                "5. Si el tracker aterriza en una casilla especial → ejecuta también esa acción"
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }

        CMRuleCard("🟡→🟤 Transición de Zona") {
            Text(
                "Cuando el tracker llega al umbral de dificultad, Tingent pasa a Zona Marrón. " +
                "Todas las cartas usan el lado marrón desde ese momento. El mazo se baraja de nuevo.",
                style = MaterialTheme.typography.bodySmall, color = GhostWhite.copy(alpha = 0.75f)
            )
        }

        CMRuleCard("📋 Acciones especiales del Tracker") {
            listOf(
                "📋 Carta bocarriba → toma del Territorio la especie objetivo con mayor PV + 1 huevo",
                "📥 Carta bocabajo → roba del mazo bocabajo (puntúa al final según dificultad)",
                "🗺️ Descubrimiento → coloca loseta Hábitat en lago con huevo; Tingent obtiene el huevo",
                "⏱️ Avance de Tiempo → avanza el marcador de Tiempo; aplica el efecto inmediatamente"
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }

        CMRuleCard("🏆 Puntuación de Tingent") {
            listOf(
                "Cartas Final de Partida bocarriba × 5 PV",
                "Cartas de Criatura bocarriba: suma de sus PV impresos",
                "Logros completados: suma de sus PV",
                "Trofeos × 3 PV",
                "Huevos × 1 PV",
                "Cartas bocabajo × [1 / 2 / 3 PV según dificultad]"
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }

        CMRuleCard("🧑 Puntuación del Jugador") {
            listOf(
                "Cartas Final de Partida: PV impresos",
                "Cartas de Criatura: suma sellos rojos",
                "Logros: 6 / 12 / 15 PV cada uno",
                "Trofeos × 3 PV",
                "Recursos + Cazamariposas ÷ 4 (redondea abajo)"
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.75f), modifier = Modifier.padding(vertical = 1.dp)) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CMRuleCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CMCard),
        border = BorderStroke(1.dp, CMBorder),
        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                color = YellowZone, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

// ─── Tab Setup ─────────────────────────────────────────────────────
@Composable
internal fun CMSetupTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Preparación · Criaturas Maravillosas", color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text("2 a 4 jugadores · Colocación de trabajadores + tableau",
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        CMSetupBlock("🗺️ Tablero general",
            "• Monta el tablero hexagonal de la isla y coloca los tokens de hábitat en sus espacios.\n" +
            "• Mezcla las cartas de criaturas y forma el mercado visible (cartas bocarriba).\n" +
            "• Saca aleatoriamente 7 fichas de objetivo y colócalas visibles para todos.\n" +
            "• Coloca el tablero de suministro con los recursos de inicio.")
        CMSetupBlock("👤 Cada jugador recibe",
            "• 1 tablero personal de doble capa.\n" +
            "• Elige 1 Capitán de los disponibles → coloca su tablero de Capitán junto al tablero personal.\n" +
            "• 3 tripulantes propios (en tu color).\n" +
            "• Elige 4 cartas de inicio de entre las 8 disponibles.\n" +
            "• Recursos de inicio según orden de turno:\n" +
            "  1º jugador → sin bonus extra\n" +
            "  2º jugador → +1 recurso\n" +
            "  3º jugador → +2 recursos\n" +
            "  4º jugador → +3 recursos")
        CMSetupBlock("🎯 Solo mode — Tingent",
            "• Coloca el marcador de Tingent en la posición 1 del tracker.\n" +
            "• Mezcla el mazo de cartas de Tingent (12 cartas).\n" +
            "• Tingent NO tiene tablero personal ni recursos — solo el tracker y su mazo.\n" +
            "• El jugador configura la dificultad antes de empezar (ver tab Solitario).")
        CMSetupBlock("🔄 Orden de turno",
            "• El jugador con la criatura de menor valor empieza.\n" +
            "• Tingent actúa después de cada turno del jugador según la carta revelada.")
    }
}

@Composable
private fun CMSetupBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF251C0D)),
        border = androidx.compose.foundation.BorderStroke(1.dp, YellowZone.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = YellowZone, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
