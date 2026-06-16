package com.rafel.bgt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rafel.bgt.R
import com.rafel.bgt.ui.theme.*
import com.rafel.bgt.ui.util.CardSoundPlayer

private val PirateGold  = Color(0xFFD4A017)
private val NavyDeep    = Color(0xFF0A1628)
private val NavyMid     = Color(0xFF0D2240)
private val NavyCard    = Color(0xFF111D30)

// ── Estado de puntuación del jugador ──────────────────────────────────────────

private class MrScoringState {
    var yourRivers           by mutableStateOf(0)
    var yourDoubloons        by mutableStateOf(0)
    var yourUpgradeVP        by mutableStateOf(0)
    var yourResidenceVP      by mutableStateOf(0)
    var yourTreasureVP       by mutableStateOf(0)
    var yourBuriedTreasures  by mutableStateOf(0)
    var yourMissionVP        by mutableStateOf(0)
    var yourMissionBonus     by mutableStateOf(false)
}

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaracaiboSoloScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val soundPlayer = remember { CardSoundPlayer(context) }
    DisposableEffect(Unit) { onDispose { soundPlayer.release() } }

    var setup       by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val mrScoring   = remember { MrScoringState() }
    var bCards      by remember { mutableStateOf(0) }
    var withExp by remember { mutableStateOf(false) }

    // Jordan state
    var jordanMarker     by remember { mutableStateOf(0) }  // 0-3 (hideout position)
    var jordanUpgrades   by remember { mutableStateOf(0) }
    var jordanMissions   by remember { mutableStateOf(1) }  // starts with 1
    var jordanResidences by remember { mutableStateOf(0) }
    var jordanFigurehead by remember { mutableStateOf(false) }
    var jordanRivers     by remember { mutableStateOf(0) }
    var jordanTreasures  by remember { mutableStateOf(0) }
    var jordanVP         by remember { mutableStateOf(0) }
    var round            by remember { mutableStateOf(1) }
    var jordanInGulf     by remember { mutableStateOf(false) }

    var showPanel   by remember { mutableStateOf(false) }
    var showUpgrade by remember { mutableStateOf(false) }

    // Derived strengths based on marker position
    val m              = jordanMarker.coerceIn(0, 3)
    val raidVP         = intArrayOf(0, 2, 2, 3)[m]
    val raidTreasures  = if (m == 3) 2 else 1
    val exploreVP      = intArrayOf(3, 3, 4, 6)[m]
    val exploreExtra   = intArrayOf(0, 1, 2, 2)[m]
    val checkIsland    = m >= 2   // Jordan checks if he has more treasures than you

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.mr_title), color = GhostWhite,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTab != 0) selectedTab = 0 else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, null, tint = GhostWhite)
                    }
                },
                actions = {
                    if (selectedTab == 0 && !setup) {
                        Text(
                            stringResource(R.string.mr_round, round),
                            color = PirateGold, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDeep)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = NavyDeep, tonalElevation = 0.dp) {
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
                            selectedIconColor   = PirateGold,
                            selectedTextColor   = PirateGold,
                            unselectedIconColor = GhostWhite.copy(alpha = 0.4f),
                            unselectedTextColor = GhostWhite.copy(alpha = 0.4f),
                            indicatorColor      = PirateGold.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        containerColor = NavyDeep
    ) { pad ->
        Box(
            Modifier.fillMaxSize().padding(pad)
                .background(Brush.verticalGradient(listOf(NavyDeep, Color(0xFF050C18))))
        ) {
            if (selectedTab == 0) {
                MrSetupTab(Modifier.padding(pad))
            } else if (selectedTab == 2) {
                MrScoringContent(
                    mrScoring, jordanVP, jordanRivers, jordanUpgrades,
                    jordanMissions, jordanResidences, jordanTreasures
                )
            } else if (selectedTab == 3) {
                MrRulesContent()
            } else if (setup) {
                MaracaiboSetup(
                    bCards, withExp,
                    onBCardsChange    = { bCards = it },
                    onExpansionToggle = { withExp = it },
                    onStart           = { soundPlayer.playShuffle(); setup = false }
                )
            } else {
                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Gulf toggle ──────────────────────────────────────────
                    MaracaiboGulfToggle(jordanInGulf) { jordanInGulf = it }

                    // ── Jordan tracker ───────────────────────────────────────
                    MaracaiboTracker(
                        jordanMarker, jordanUpgrades, jordanMissions,
                        jordanResidences, jordanFigurehead,
                        jordanRivers, jordanTreasures, jordanVP
                    )

                    // ── Current Raid / Explore strength ──────────────────────
                    MaracaiboStrength(raidVP, raidTreasures, exploreVP, exploreExtra, checkIsland)

                    // ── Jordan turn button ────────────────────────────────────
                    Button(
                        onClick = { if (!showPanel) soundPlayer.playDeal(); showPanel = !showPanel },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showPanel) SpookyPurple else HalloweenOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (showPanel) Icons.Default.Close else Icons.Default.PlayArrow,
                            null, modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (showPanel) stringResource(R.string.mr_btn_close_turn) else stringResource(R.string.mr_btn_jordan_turn),
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }

                    // ── Turn panel ────────────────────────────────────────────
                    AnimatedVisibility(showPanel,
                        enter = expandVertically() + fadeIn(),
                        exit  = shrinkVertically() + fadeOut()
                    ) {
                        MaracaiboTurnPanel(
                            jordanInGulf, raidVP, raidTreasures, exploreVP,
                            exploreExtra, m, checkIsland, withExp,
                            onRaid = {
                                jordanVP += raidVP
                                jordanTreasures += raidTreasures
                                showPanel = false
                            },
                            onExplore = { rivers ->
                                jordanVP += exploreVP
                                jordanRivers += rivers
                                showPanel = false
                            },
                            onMission  = { jordanMissions++;          showPanel = false },
                            onUpgrade  = { jordanUpgrades++;          showPanel = false; showUpgrade = true },
                            onMaracaibo = {
                                jordanVP += 6; jordanUpgrades++
                                jordanInGulf = false
                                showPanel = false; showUpgrade = true
                            }
                        )
                    }

                    // ── Upgrade effect ────────────────────────────────────────
                    AnimatedVisibility(showUpgrade,
                        enter = expandVertically() + fadeIn(),
                        exit  = shrinkVertically() + fadeOut()
                    ) {
                        MaracaiboUpgradeDialog(jordanMarker, jordanFigurehead) { idx ->
                            when (idx) {
                                0 -> if (jordanMarker < 3) jordanMarker++
                                1 -> jordanFigurehead = true
                                2 -> jordanMissions++
                                3 -> {
                                    val isFirst = jordanResidences == 0
                                    jordanResidences++
                                    if (isFirst) jordanVP += 2
                                }
                            }
                            showUpgrade = false
                        }
                    }

                    HorizontalDivider(color = GhostWhite.copy(alpha = 0.08f))

                    // ── Round / scoring buttons ───────────────────────────────
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                showPanel = false
                                if (round < 3) round++ else selectedTab = 1
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, PirateGold.copy(0.5f)),
                            shape  = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                if (round < 3) stringResource(R.string.mr_btn_end_round, round) else stringResource(R.string.mr_btn_final_score),
                                color = PirateGold, fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, SpookyPurple.copy(0.5f)),
                            shape  = RoundedCornerShape(10.dp)
                        ) {
                            Text(stringResource(R.string.mr_btn_calc_vp), color = SpookyPurple)
                        }
                    }

                    // ── Jordan rules reference ────────────────────────────────
                    MaracaiboRulesCard()
                    Spacer(Modifier.height(40.dp))
                }
            } // closes else { Column }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SETUP SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboSetup(
    bCards: Int, withExp: Boolean,
    onBCardsChange: (Int) -> Unit,
    onExpansionToggle: (Boolean) -> Unit,
    onStart: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text("⚓", fontSize = 54.sp)
        Text(stringResource(R.string.mr_solo_title), color = PirateGold,
            fontWeight = FontWeight.Black, fontSize = 24.sp, textAlign = TextAlign.Center)
        Text(stringResource(R.string.mr_solo_subtitle), color = GhostWhite.copy(0.65f),
            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)

        HorizontalDivider(color = PirateGold.copy(0.25f))

        // Difficulty
        Surface(color = NavyCard, shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PirateGold.copy(0.3f))) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(stringResource(R.string.mr_difficulty_label), color = PirateGold, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    when (bCards) {
                        0    -> "🟢 Corsario — Solo cartas A (más fácil)"
                        in 1..2 -> "🟡 Bucanero — $bCards carta${if (bCards > 1) "s" else ""} B"
                        in 3..4 -> "🟠 Pirata — $bCards cartas B"
                        else -> "🔴 Kraken — $bCards cartas B (más difícil)"
                    },
                    color = GhostWhite, style = MaterialTheme.typography.bodyMedium
                )
                Text(stringResource(R.string.mr_b_cards_count, bCards),
                    color = GhostWhite.copy(0.5f), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                Slider(
                    value = bCards.toFloat(),
                    onValueChange = { onBCardsChange(it.toInt()) },
                    valueRange = 0f..6f, steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = PirateGold,
                        activeTrackColor = PirateGold,
                        inactiveTrackColor = GhostWhite.copy(0.2f)
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(0 to "Fácil", 3 to "Normal", 6 to "Difícil").forEach { (v, lbl) ->
                        OutlinedButton(
                            onClick = { onBCardsChange(v) },
                            border = BorderStroke(1.dp, if (bCards == v) PirateGold else GhostWhite.copy(0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(lbl,
                                color = if (bCards == v) PirateGold else GhostWhite.copy(0.45f),
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Expansion toggle
        Surface(color = NavyCard, shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GhostWhite.copy(0.12f))) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.mr_expansion_title), color = GhostWhite, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.mr_expansion_subtitle),
                        color = GhostWhite.copy(0.5f), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = withExp, onCheckedChange = onExpansionToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HalloweenOrange,
                        checkedTrackColor = HalloweenOrange.copy(0.3f)
                    )
                )
            }
        }

        // Setup steps
        Surface(color = GhostWhite.copy(0.03f), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(stringResource(R.string.mr_prep_title), color = GhostWhite.copy(0.55f),
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
                val steps = listOf(
                    "Prepara para 2 jugadores — tú controlas un color, Jordán el otro",
                    "Da la vuelta al tablero de barco y escondite de Jordán (lado solitario)",
                    "Pon el marcador de Jordán en la posición más a la IZQUIERDA del escondite",
                    "Separa cartas Solo A y B. Reemplaza $bCards carta${if (bCards != 1) "s" else ""} A al azar por B",
                    "Baraja el mazo resultante (6 cartas) boca abajo junto al barco de Jordán",
                    "Jordán roba 1 carta de Misión al azar (boca abajo) en la preparación",
                    "Mercado de Mascarones: solo 3 fichas (no 4)",
                    "Tú juegas siempre el primer turno de cada ronda"
                )
                steps.forEachIndexed { i, step ->
                    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                        Surface(color = PirateGold.copy(0.18f), shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.size(20.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${i+1}", color = PirateGold,
                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(step, color = GhostWhite.copy(0.65f),
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PirateGold),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(stringResource(R.string.btn_set_sail), color = NavyDeep, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GULF TOGGLE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboGulfToggle(jordanInGulf: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        color = if (jordanInGulf) Color(0xFF0D2E4A) else NavyCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (jordanInGulf) PirateGold.copy(0.6f) else GhostWhite.copy(0.1f))
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🌊", fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.mr_jordan_in_gulf_title), color = GhostWhite, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (jordanInGulf) stringResource(R.string.mr_jordan_in_gulf_active)
                    else stringResource(R.string.mr_jordan_in_gulf_inactive),
                    color = GhostWhite.copy(0.5f), style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = jordanInGulf, onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PirateGold,
                    checkedTrackColor = PirateGold.copy(0.3f)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// JORDAN TRACKER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboTracker(
    marker: Int, upgrades: Int, missions: Int, residences: Int,
    figurehead: Boolean, rivers: Int, treasures: Int, vp: Int
) {
    Surface(color = NavyCard, shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, PirateGold.copy(0.3f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏴‍☠️", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.mr_jordan_title), color = PirateGold, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                Surface(color = HalloweenOrange.copy(0.18f), shape = RoundedCornerShape(8.dp)) {
                    Text(stringResource(R.string.mr_jordan_vp, vp), color = HalloweenOrange, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.mr_hideout_marker),
                color = GhostWhite.copy(0.5f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { i ->
                    val active = i == marker
                    Surface(
                        color = if (active) PirateGold else GhostWhite.copy(0.08f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(if (active) 34.dp else 28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${i+1}",
                                color = if (active) NavyDeep else GhostWhite.copy(0.35f),
                                fontWeight = if (active) FontWeight.Black else FontWeight.Normal,
                                fontSize = if (active) 14.sp else 12.sp)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("Pos. ${marker+1}/4", color = PirateGold.copy(0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterVertically))
            }

            Spacer(Modifier.height(12.dp))

            val stats = listOf(
                "⚓ Mejoras" to "$upgrades", "📜 Misiones" to "$missions",
                "🏠 Residencias" to "$residences", "🗺️ Ríos" to "$rivers",
                "💎 Tesoros" to "$treasures", "🪆 Mascarón" to if (figurehead) "Sí" else "No"
            )
            stats.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (label, value) ->
                        Surface(color = GhostWhite.copy(0.04f), shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)) {
                            Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, style = MaterialTheme.typography.labelSmall,
                                    color = GhostWhite.copy(0.45f), textAlign = TextAlign.Center)
                                Text(value, fontWeight = FontWeight.Bold, color = GhostWhite, fontSize = 16.sp)
                            }
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RAID / EXPLORE STRENGTH PANEL
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboStrength(
    raidVP: Int, raidTreasures: Int, exploreVP: Int, exploreExtra: Int, checkIsland: Boolean
) {
    Surface(color = NavyCard, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GhostWhite.copy(0.1f))) {
        Row(Modifier.fillMaxWidth().padding(14.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.mr_current_raid), color = HalloweenOrange,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                if (raidVP > 0) Text("+$raidVP PV", color = GhostWhite,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("+$raidTreasures tesoro${if (raidTreasures > 1) "s" else ""}",
                    color = PirateGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (checkIsland) Text(stringResource(R.string.mr_verify_treasures), color = GhostWhite.copy(0.4f),
                    style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
            Box(Modifier.width(1.dp).height(55.dp).background(GhostWhite.copy(0.1f))
                .align(Alignment.CenterVertically))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.mr_current_explore), color = Color(0xFF4FC3F7),
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Text("+$exploreVP PV", color = GhostWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (exploreExtra > 0) Text("+$exploreExtra espacio${if (exploreExtra > 1) "s" else ""} extra",
                    color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TURN PANEL
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboTurnPanel(
    jordanInGulf: Boolean,
    raidVP: Int, raidTreasures: Int,
    exploreVP: Int, exploreExtra: Int,
    markerPos: Int, checkIsland: Boolean, withExp: Boolean,
    onRaid: () -> Unit,
    onExplore: (Int) -> Unit,
    onMission: () -> Unit,
    onUpgrade: () -> Unit,
    onMaracaibo: () -> Unit
) {
    var isBCard       by remember { mutableStateOf(false) }
    var bonusDone     by remember { mutableStateOf(false) }
    var exploreRivers by remember { mutableStateOf(0) }

    Surface(color = Color(0xFF0B1E3A), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, HalloweenOrange.copy(0.5f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Text(stringResource(R.string.mr_jordan_turn), color = HalloweenOrange,
                fontWeight = FontWeight.Black, fontSize = 18.sp)

            if (jordanInGulf) {
                // ── Special: Gulf → Maracaibo
                Surface(color = PirateGold.copy(0.12f), shape = RoundedCornerShape(10.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(stringResource(R.string.mr_jordan_in_gulf_note), color = PirateGold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.mr_gulf_detail),
                            color = GhostWhite.copy(0.8f), style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Button(onClick = onMaracaibo, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PirateGold),
                    shape = RoundedCornerShape(10.dp)) {
                    Text(stringResource(R.string.btn_execute_maracaibo), color = NavyDeep, fontWeight = FontWeight.Bold)
                }

            } else {
                // ── B card toggle
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mr_is_b_card), color = GhostWhite, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                    Switch(
                        checked = isBCard, onCheckedChange = { isBCard = it; bonusDone = false },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HalloweenOrange,
                            checkedTrackColor = HalloweenOrange.copy(0.3f)
                        )
                    )
                }

                if (isBCard && !bonusDone) {
                    // ── Bonus action (B cards only)
                    Surface(color = HalloweenOrange.copy(0.08f), shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HalloweenOrange.copy(0.3f))) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(stringResource(R.string.mr_bonus_action_title), color = HalloweenOrange,
                                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.mr_bonus_action_body),
                                color = GhostWhite.copy(0.6f), style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("⚓ Mejora" to onUpgrade, "🗡️ Incursión" to onRaid,
                                    "🧭 Exploración" to { onExplore(0) }).forEach { (lbl, action) ->
                                    OutlinedButton(
                                        onClick = { action(); bonusDone = true },
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(1.dp, HalloweenOrange.copy(0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Text(lbl, color = HalloweenOrange,
                                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ── Main actions in priority order
                    Text(
                        stringResource(R.string.mr_main_actions_hint),
                        color = GhostWhite.copy(0.55f), style = MaterialTheme.typography.bodySmall
                    )

                    HorizontalDivider(color = GhostWhite.copy(0.08f))

                    // 1. Raid
                    MaracaiboActionCard(
                        emoji = "🗡️", label = "1º  Incursión",
                        condition = "¿Hay localización con Incursión a ≤3 movimientos?",
                        result = buildString {
                            if (raidVP > 0) append("+$raidVP PV · ")
                            append("+$raidTreasures tesoro${if (raidTreasures > 1) "s" else ""}")
                            if (checkIsland) append("\nSi Jordán > tú en ese tesoro → +1 cubo en esa isla")
                            append("\nDado tesoro: 4-6=isla con más cubos · 1-3=con menos")
                        },
                        color = HalloweenOrange,
                        onClick = onRaid
                    )

                    // 2. Explore
                    MaracaiboActionCard(
                        emoji = "🧭", label = "2º  Exploración",
                        condition = "¿Hay localización con Exploración a ≤3 movimientos?",
                        result = buildString {
                            append("+$exploreVP PV")
                            if (exploreExtra > 0) append(" · +$exploreExtra espacio${if (exploreExtra > 1) "s" else ""} extra")
                            append("\nJordán elige siempre la ruta más larga en bifurcaciones")
                            append("\nNO obtiene beneficios de los espacios del Explorador")
                        },
                        color = Color(0xFF4FC3F7),
                        extraContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Ríos cruzados por Jordán: ", color = GhostWhite.copy(0.7f),
                                    style = MaterialTheme.typography.bodySmall)
                                IconButton(onClick = { if (exploreRivers > 0) exploreRivers-- },
                                    modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Remove, null, tint = GhostWhite.copy(0.7f),
                                        modifier = Modifier.size(16.dp))
                                }
                                Text("$exploreRivers", color = GhostWhite, fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                IconButton(onClick = { exploreRivers++ },
                                    modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Add, null, tint = GhostWhite.copy(0.7f),
                                        modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        onClick = { onExplore(exploreRivers) }
                    )

                    // 3. Mission
                    MaracaiboActionCard(
                        emoji = "📜", label = "3º  Misión",
                        condition = "¿Hay localización con acción de Misión a ≤3 movimientos?",
                        result = "Jordán toma la Misión más a la DERECHA del mercado\nRepone con la carta superior del mazo",
                        color = Color(0xFF81C784),
                        onClick = onMission
                    )

                    // 4. Upgrade
                    MaracaiboActionCard(
                        emoji = "⚓", label = "4º  Mejora de Barco",
                        condition = "¿Hay Mejora de Barco a ≤3 movimientos?\n(Prueba: Tripulación → Equipo → Barco en ese orden)",
                        result = "Jordán toma la carta GRATIS (ignora todos los efectos)\nRepone con carta de Nivel II",
                        color = SpookyPurple,
                        onClick = onUpgrade
                    )

                    // Expansion: Fort rule
                    if (withExp) {
                        Surface(color = Color(0xFF2A1A00), shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PirateGold.copy(0.3f))) {
                            Column(Modifier.fillMaxWidth().padding(10.dp)) {
                                Text("⚔️ Expansión: Fuertes (prioridad antes de todo)",
                                    color = PirateGold, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Si hay Localización con Fuerte a ≤3 movimientos, Jordán va ahí primero.\n" +
                                    "Si varios: elige el de menor resistencia (empate: el más alejado).\n" +
                                    "Acciones: 1) Loseta Valor Tesoro → +1 cubo en isla · 2) +1 mejora de barco · 3) Destruye el Fuerte → gana sus PV · 4) +7 PV",
                                    color = GhostWhite.copy(0.65f), style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = {
                                        // Fort counts as ship upgrade for marker purposes
                                        onUpgrade()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, PirateGold.copy(0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("⚔️  Jordán destruye un Fuerte (+7 PV ya registrados manualmente)",
                                        color = PirateGold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACTION CARD (expandable)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboActionCard(
    emoji: String, label: String, condition: String, result: String,
    color: Color, extraContent: (@Composable () -> Unit)? = null, onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(color = color.copy(0.07f), shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(0.28f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, color = color, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge)
                    Text(condition, color = GhostWhite.copy(0.5f),
                        style = MaterialTheme.typography.bodySmall)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = color.copy(0.55f), modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = color.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(result, color = GhostWhite.copy(0.9f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp))
                    }
                    extraContent?.invoke()
                    Button(
                        onClick = onClick, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.btn_jordan_action), color = NavyDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UPGRADE EFFECT DIALOG
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboUpgradeDialog(
    jordanMarker: Int, jordanFigurehead: Boolean, onEffect: (Int) -> Unit
) {
    val effects = listOf(
        Triple("📈", "Avanza marcador del escondite",
            if (jordanMarker >= 3) "Ya está en posición máxima (4). Sin efecto."
            else "Sube de posición ${jordanMarker+1} → ${jordanMarker+2} · Raid/Exploración más potentes"),
        Triple("🪆", "Obtiene Mascarón de Proa",
            if (jordanFigurehead) "Ya tiene mascarón. Sin efecto."
            else "Tira dado: 1-2 → 1er mascarón · 3-4 → 2º · 5-6 → 3er mascarón"),
        Triple("📜", "Obtiene carta de Misión",
            "Jordán toma la Misión más a la DERECHA del mercado\n(Repone deslizando y robando nueva)"),
        Triple("🏠", "Construye Residencia",
            "Asigna nº 1-4 a cada Residencia. Tira dado (re-tira 5, 6 o repetida).\nSi es la 1ª ficha de Jordán → +2 PV")
    )

    Surface(color = Color(0xFF150B2E), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, SpookyPurple.copy(0.6f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.mr_upgrade_title), color = SpookyPurple,
                fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(stringResource(R.string.mr_upgrade_hint),
                color = GhostWhite.copy(0.55f), style = MaterialTheme.typography.bodySmall)

            effects.forEachIndexed { idx, (em, title, desc) ->
                val disabled = (idx == 0 && jordanMarker >= 3) || (idx == 1 && jordanFigurehead)
                OutlinedButton(
                    onClick = { if (!disabled) onEffect(idx) },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, if (disabled) GhostWhite.copy(0.12f) else SpookyPurple.copy(0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !disabled
                ) {
                    Text(em, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title,
                            color = if (disabled) GhostWhite.copy(0.3f) else GhostWhite,
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodySmall)
                        Text(desc,
                            color = if (disabled) GhostWhite.copy(0.2f) else GhostWhite.copy(0.5f),
                            style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Start)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RULES REFERENCE CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboRulesCard() {
    var expanded by remember { mutableStateOf(false) }
    Surface(color = GhostWhite.copy(0.03f), shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GhostWhite.copy(0.07f))) {
        Column(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.mr_rules_reference), color = GhostWhite.copy(0.5f),
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = GhostWhite.copy(0.35f), modifier = Modifier.size(18.dp))
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    val rules = listOf(
                        "Jordán nunca gana ni paga doblones",
                        "Tú juegas siempre el 1er turno · Jordán siempre el último",
                        "Desempate de movimiento: columna más cercana a Maracaibo; luego la más alta",
                        "Dado de tesoro: 4-6 = isla con más cubos · 1-3 = con menos · Empate: Oro > Esmeralda > Perla",
                        "Si Jordán llega a la carta de alguien → toma 1 doblón del suministro",
                        "Si alguien llega a la carta de Jordán → le paga 1 doblón al suministro (si puede)",
                        "Al final de ronda: Jordán NO recibe ingresos",
                        "Cartas de Nivel II: Jordán ignora PV adicionales por mejoras, tesoros enterrados, etc.",
                        "Cartas B: bonus action se realiza ANTES de las acciones principales",
                        "Mazo de 6 cartas. Cuando se acaba, se baraja de nuevo"
                    )
                    rules.forEachIndexed { i, rule ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("${i+1}.", color = HalloweenOrange.copy(0.55f),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(18.dp))
                            Text(rule, color = GhostWhite.copy(0.5f),
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCORING SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MaracaiboScoring(
    jordanVP: Int, jordanUpgrades: Int, jordanMissions: Int,
    jordanResidences: Int, jordanRivers: Int, jordanTreasures: Int,
    onBack: () -> Unit
) {
    val riverVP     = jordanRivers * 4
    val upgradeVP   = jordanUpgrades * 6
    val residenceVP = jordanResidences * 15
    val missionVP   = jordanMissions * 5
    val missionBonus = if (jordanMissions >= 6) 10 else 0
    val treasureVP  = jordanTreasures * 2  // minimum 2VP; user adjusts based on island

    val total = jordanVP + riverVP + upgradeVP + residenceVP + missionVP + missionBonus + treasureVP

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("🏆  Puntuación Final", color = PirateGold,
            fontWeight = FontWeight.Black, fontSize = 22.sp)

        // Jordan score
        Surface(color = NavyCard, shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, PirateGold.copy(0.4f))) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Puntuación de Jordán", color = PirateGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))

                val items = listOf(
                    "PV acumulados durante la partida" to jordanVP,
                    "4 PV × $jordanRivers ríos cruzados" to riverVP,
                    "6 PV × $jordanUpgrades mejoras de barco" to upgradeVP,
                    "15 PV × $jordanResidences residencias" to residenceVP,
                    "5 PV × $jordanMissions misiones" to missionVP,
                    "Bonus ≥6 misiones" to missionBonus,
                    "Tesoros (~2 PV × $jordanTreasures, ajusta según isla)" to treasureVP
                )
                items.forEach { (label, value) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(label, color = GhostWhite.copy(0.65f),
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(if (value == 0) "—" else "+$value",
                            color = if (value > 0) GhostWhite else GhostWhite.copy(0.35f),
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = PirateGold.copy(0.3f))

                Row(Modifier.fillMaxWidth()) {
                    Text("TOTAL JORDÁN", color = PirateGold,
                        fontWeight = FontWeight.Black, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    Text("$total PV", color = PirateGold,
                        fontWeight = FontWeight.Black, fontSize = 17.sp)
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "⚠️ Tesoros: valor real 2-5 PV según cubos en la isla. Ajusta si es necesario.",
                    color = HalloweenOrange.copy(0.65f), style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Your scoring guide
        Surface(color = GhostWhite.copy(0.03f), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Tu puntuación (recordatorio)", color = GhostWhite.copy(0.6f),
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                val yours = listOf(
                    "4 PV × ríos cruzados en el Tablero de Exploración",
                    "1 PV × cada 5 doblones restantes",
                    "PV impresos en la esquina sup. dcha. de cada Mejora adquirida",
                    "PV condicionales de Mejoras (pergamino gris)",
                    "PV de Residencias construidas",
                    "Tesoros: 2-5 PV según cubos en esa isla",
                    "+2 PV por cada tesoro enterrado",
                    "Misiones: puntos de la condición más alta cumplida",
                    "+10 PV si puntuaste al menos 6 cartas de Misión"
                )
                yours.forEach { item ->
                    Row(Modifier.padding(vertical = 2.dp)) {
                        Text("•", color = HalloweenOrange, modifier = Modifier.width(12.dp),
                            style = MaterialTheme.typography.bodySmall)
                        Text(item, color = GhostWhite.copy(0.55f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Button(
            onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HalloweenOrange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("← Volver a la partida", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB: PUNTUACIÓN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MrScoringContent(
    s: MrScoringState,
    jordanVP: Int, jordanRivers: Int, jordanUpgrades: Int,
    jordanMissions: Int, jordanResidences: Int, jordanTreasures: Int
) {
    val jordanMissionBonus = if (jordanMissions >= 6) 10 else 0
    val jordanTotal = jordanVP + jordanRivers * 4 + jordanUpgrades * 6 +
        jordanMissions * 5 + jordanMissionBonus + jordanResidences * 15 + jordanTreasures * 2

    val yourTotal = s.yourRivers * 4 + s.yourDoubloons / 5 + s.yourUpgradeVP +
        s.yourResidenceVP + s.yourTreasureVP + s.yourBuriedTreasures * 2 +
        s.yourMissionVP + (if (s.yourMissionBonus) 10 else 0)

    val win = yourTotal > jordanTotal
    val tie = yourTotal == jordanTotal

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.mr_score_title), style = MaterialTheme.typography.titleMedium,
            color = PirateGold, fontWeight = FontWeight.Black)

        // ── Jordán (auto-calculado) ───────────────────────────────────────────
        Surface(color = NavyCard, shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PirateGold.copy(alpha = 0.4f))) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mr_score_jordan), style = MaterialTheme.typography.titleSmall,
                        color = PirateGold, fontWeight = FontWeight.Bold)
                    Text("$jordanTotal PV", color = PirateGold,
                        fontWeight = FontWeight.Black, fontSize = 17.sp)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = PirateGold.copy(0.25f))
                listOf(
                    "PV acumulados en partida"                  to jordanVP,
                    "$jordanRivers ríos × 4 PV"                to jordanRivers * 4,
                    "$jordanUpgrades mejoras × 6 PV"           to jordanUpgrades * 6,
                    "$jordanMissions misiones × 5 PV"          to jordanMissions * 5,
                    "Bonus ≥6 misiones"                        to jordanMissionBonus,
                    "$jordanResidences residencias × 15 PV"    to jordanResidences * 15,
                    "$jordanTreasures tesoros × 2 PV"          to jordanTreasures * 2,
                ).forEach { (label, pv) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(label, style = MaterialTheme.typography.bodySmall,
                            color = GhostWhite.copy(0.65f), modifier = Modifier.weight(1f))
                        Text(if (pv == 0) "—" else "+$pv",
                            color = if (pv > 0) GhostWhite else GhostWhite.copy(0.3f),
                            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("⚠️ Tesoros: valor real 2-5 PV según cubos en la isla. Ajusta si es necesario.",
                    style = MaterialTheme.typography.labelSmall,
                    color = HalloweenOrange.copy(alpha = 0.6f))
            }
        }

        // ── Jugador (contadores manuales) ─────────────────────────────────────
        Surface(color = NavyCard, shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF4AEE7A).copy(alpha = 0.4f))) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mr_score_you), style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF4AEE7A), fontWeight = FontWeight.Bold)
                    Text("$yourTotal PV", color = Color(0xFF4AEE7A),
                        fontWeight = FontWeight.Black, fontSize = 17.sp)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFF4AEE7A).copy(0.25f))

                MrScoreRow("Ríos cruzados × 4 PV  (n=${s.yourRivers})", s.yourRivers, s.yourRivers * 4,
                    onInc = { s.yourRivers++ },          onDec = { if (s.yourRivers          > 0) s.yourRivers--          })
                MrScoreRow("Doblones  (${s.yourDoubloons} ÷ 5)", s.yourDoubloons, s.yourDoubloons / 5,
                    onInc = { s.yourDoubloons++ },       onDec = { if (s.yourDoubloons       > 0) s.yourDoubloons--       })
                MrScoreRow("PV mejoras adquiridas (suma)", s.yourUpgradeVP, s.yourUpgradeVP,
                    onInc = { s.yourUpgradeVP++ },       onDec = { if (s.yourUpgradeVP       > 0) s.yourUpgradeVP--       })
                MrScoreRow("PV residencias construidas (suma)", s.yourResidenceVP, s.yourResidenceVP,
                    onInc = { s.yourResidenceVP++ },     onDec = { if (s.yourResidenceVP     > 0) s.yourResidenceVP--     })
                MrScoreRow("Tesoros (2-5 PV cada uno, total)", s.yourTreasureVP, s.yourTreasureVP,
                    onInc = { s.yourTreasureVP++ },      onDec = { if (s.yourTreasureVP      > 0) s.yourTreasureVP--      })
                MrScoreRow("Tesoros enterrados × 2 PV  (n=${s.yourBuriedTreasures})", s.yourBuriedTreasures, s.yourBuriedTreasures * 2,
                    onInc = { s.yourBuriedTreasures++ }, onDec = { if (s.yourBuriedTreasures > 0) s.yourBuriedTreasures-- })
                MrScoreRow("Misiones (PV condición más alta cumplida)", s.yourMissionVP, s.yourMissionVP,
                    onInc = { s.yourMissionVP++ },       onDec = { if (s.yourMissionVP       > 0) s.yourMissionVP--       })

                // Bonus ≥6 misiones toggle
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mr_mission_bonus_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite.copy(0.75f), modifier = Modifier.weight(1f))
                    Switch(
                        checked = s.yourMissionBonus,
                        onCheckedChange = { s.yourMissionBonus = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PirateGold,
                            checkedTrackColor = PirateGold.copy(0.3f)
                        )
                    )
                    Text(if (s.yourMissionBonus) "+10 PV" else "—",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                        color = if (s.yourMissionBonus) PirateGold else GhostWhite.copy(0.3f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                }
            }
        }

        // ── Resultado ─────────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (win) Color(0xFF2A6B3A).copy(alpha = 0.15f)
                                 else if (tie) PirateGold.copy(alpha = 0.1f)
                                 else Color(0xFF1A0808)
            ),
            border = BorderStroke(2.dp, if (win) Color(0xFF4AEE7A) else if (tie) PirateGold else Color(0xFFAA2222)),
            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (win) stringResource(R.string.mr_result_win) else if (tie) stringResource(R.string.mr_result_tie) else stringResource(R.string.mr_result_lose),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (win) Color(0xFF4AEE7A) else if (tie) PirateGold else Color(0xFFAA2222),
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.mr_you_vs_jordan, yourTotal, jordanTotal),
                    style = MaterialTheme.typography.bodyMedium, color = GhostWhite)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MrScoreRow(
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
            color = PirateGold, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB: REGLAS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MrRulesContent() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.mr_rules_title), style = MaterialTheme.typography.titleMedium,
            color = PirateGold, fontWeight = FontWeight.Black)
        MaracaiboRulesCard()
        Spacer(Modifier.height(8.dp))
    }
}

// ─── Tab Setup ─────────────────────────────────────────────────────
@Composable
internal fun MrSetupTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.mr_setup_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.mr_setup_subtitle),
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        MrSetupBlock(stringResource(R.string.mr_setup_board_title), stringResource(R.string.mr_setup_board_body))
        MrSetupBlock(stringResource(R.string.mr_setup_player_title), stringResource(R.string.mr_setup_player_body))
        MrSetupBlock(stringResource(R.string.mr_setup_doubloons_title), stringResource(R.string.mr_setup_doubloons_body))
        MrSetupBlock(stringResource(R.string.mr_setup_solo_title), stringResource(R.string.mr_setup_solo_body))
    }
}

@Composable
private fun MrSetupBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070F1C)),
        border = androidx.compose.foundation.BorderStroke(1.dp, PirateGold.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = PirateGold, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
