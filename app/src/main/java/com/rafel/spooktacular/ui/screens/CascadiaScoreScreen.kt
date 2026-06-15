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

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafel.spooktacular.R
import com.rafel.spooktacular.ui.theme.*

private val CascGreen = Color(0xFF43A047)

// ─── Tablas de puntuación ─────────────────────────────────────────
private val BEAR_A_PTS   = intArrayOf(0, 2, 4, 9, 16, 25, 36)
private val SALMON_A_PTS = intArrayOf(0, 2, 4, 6, 9, 11, 13, 15)
private val SALMON_B_PTS = intArrayOf(0, 2, 4, 6, 9, 11)
private val SALMON_C_PTS = intArrayOf(0, 0, 0, 3, 5, 7)
private val ELK_C_PTS    = intArrayOf(0, 1, 3, 5, 7, 9, 11, 13)
private val HAWK_A_PTS   = intArrayOf(0, 2, 5, 8, 11, 14, 17, 20)
private val HAWK_B_PTS   = intArrayOf(0, 3, 7, 12, 16)
private val FOX_A_PTS    = intArrayOf(0, 1, 2, 3, 4, 5)
private val FOX_B_PTS    = intArrayOf(0, 1, 2, 3)
private val FOX_C_PTS    = intArrayOf(0, 1, 2, 3, 4)
private val FOX_D_PTS    = intArrayOf(0, 1, 2, 3, 4)

// ─── Estado ───────────────────────────────────────────────────────
private class CascadiaScoreState {
    // Osos
    var bearVariant by mutableStateOf(0)
    var bearPairs   by mutableStateOf(0)  // A
    var bearG3b     by mutableStateOf(0)  // B
    var bear1c      by mutableStateOf(0); var bear2c by mutableStateOf(0); var bear3c by mutableStateOf(0)  // C
    var bear2d      by mutableStateOf(0); var bear3d by mutableStateOf(0); var bear4d by mutableStateOf(0)  // D

    // Salmones
    var salmonVariant by mutableStateOf(0)
    val salA = Array(7) { mutableStateOf(0) }  // A: cadenas tamaño 1-7
    val salB = Array(5) { mutableStateOf(0) }  // B: cadenas tamaño 1-5
    val salC = Array(3) { mutableStateOf(0) }  // C: cadenas tamaño 3-5
    var salDPts by mutableStateOf(0)

    // Alces
    var elkVariant by mutableStateOf(0)
    var elkL2 by mutableStateOf(0); var elkL3 by mutableStateOf(0); var elkL4 by mutableStateOf(0)  // A
    var elkBPts by mutableStateOf(0)
    val elkC = Array(7) { mutableStateOf(0) }  // C: grupos tamaño 1-7
    var elkRings by mutableStateOf(0)  // D

    // Halcones
    var hawkVariant by mutableStateOf(0)
    var hawkIso     by mutableStateOf(0)
    var hawkBCount  by mutableStateOf(0)
    var hawkLOS     by mutableStateOf(0)
    var hawkDPts    by mutableStateOf(0)

    // Zorros
    var foxVariant by mutableStateOf(0)
    val foxA = Array(6) { mutableStateOf(0) }
    val foxB = Array(4) { mutableStateOf(0) }
    val foxC = Array(5) { mutableStateOf(0) }
    val foxD = Array(5) { mutableStateOf(0) }

    // Hábitats
    var hMtn by mutableStateOf(0); var hFor by mutableStateOf(0); var hPra by mutableStateOf(0)
    var hWet by mutableStateOf(0); var hRiv by mutableStateOf(0); var hBonus by mutableStateOf(0)

    // Fichas de naturaleza
    var nature by mutableStateOf(0)

    // Cálculos
    val bearScore: Int get() = when (bearVariant) {
        0 -> BEAR_A_PTS.getOrElse(bearPairs) { BEAR_A_PTS.last() }
        1 -> bearG3b * 10
        2 -> bear1c * 2 + bear2c * 5 + bear3c * 8 + if (bear1c > 0 && bear2c > 0 && bear3c > 0) 3 else 0
        3 -> bear2d * 5 + bear3d * 8 + bear4d * 13
        else -> 0
    }
    val salmonScore: Int get() = when (salmonVariant) {
        0 -> (1..7).sumOf { sz -> salA[sz - 1].value * SALMON_A_PTS[sz] }
        1 -> (1..5).sumOf { sz -> salB[sz - 1].value * SALMON_B_PTS[sz] }
        2 -> (3..5).sumOf { sz -> salC[sz - 3].value * SALMON_C_PTS[sz] }
        3 -> salDPts
        else -> 0
    }
    val elkScore: Int get() = when (elkVariant) {
        0 -> elkL2 * 5 + elkL3 * 9 + elkL4 * 13
        1 -> elkBPts
        2 -> (1..7).sumOf { sz -> elkC[sz - 1].value * ELK_C_PTS[sz] }
        3 -> elkRings * 23
        else -> 0
    }
    val hawkScore: Int get() = when (hawkVariant) {
        0 -> HAWK_A_PTS.getOrElse(hawkIso) { HAWK_A_PTS.last() }
        1 -> HAWK_B_PTS.getOrElse(hawkBCount) { HAWK_B_PTS.last() }
        2 -> hawkLOS * 3
        3 -> hawkDPts
        else -> 0
    }
    val foxScore: Int get() = when (foxVariant) {
        0 -> (0..5).sumOf { i -> foxA[i].value * FOX_A_PTS[i] }
        1 -> (0..3).sumOf { i -> foxB[i].value * FOX_B_PTS[i] }
        2 -> (0..4).sumOf { i -> foxC[i].value * FOX_C_PTS[i] }
        3 -> (0..4).sumOf { i -> foxD[i].value * FOX_D_PTS[i] }
        else -> 0
    }
    val habitatScore: Int get() = hMtn + hFor + hPra + hWet + hRiv + hBonus * 2
    val total: Int get() = bearScore + salmonScore + elkScore + hawkScore + foxScore + habitatScore + nature
}

// ─── Pantalla principal ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CascadiaScoreScreen(onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val s = remember { CascadiaScoreState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.casc_title), color = GhostWhite, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.casc_subtitle), color = CascGreen.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = GhostWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0E0E1F), tonalElevation = 0.dp) {
                listOf(
                    Triple(0, Icons.Default.Settings, stringResource(R.string.nav_setup)),
                    Triple(1, Icons.Default.EmojiEvents, stringResource(R.string.nav_scoring)),
                    Triple(2, Icons.Default.MenuBook, stringResource(R.string.nav_rules))
                ).forEach { (idx, icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == idx, onClick = { selectedTab = idx },
                        icon = { Icon(icon, null) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CascGreen, selectedTextColor = CascGreen,
                            indicatorColor = CascGreen.copy(alpha = 0.15f),
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
            0 -> CascSetupTab(Modifier.padding(padding))
            1 -> CascScoreTab(s, Modifier.padding(padding))
            2 -> CascRulesTab(Modifier.padding(padding))
        }
    }
}

// ─── Tab puntuación ────────────────────────────────────────────────
@Composable
private fun CascScoreTab(s: CascadiaScoreState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total
        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CascGreen.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, CascGreen.copy(alpha = 0.4f))
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.casc_score_total), color = GhostWhite.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                Text("${s.total} PV", color = CascGreen, fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium)
            }
        }

        CascBearCard(s)
        CascSalmonCard(s)
        CascElkCard(s)
        CascHawkCard(s)
        CascFoxCard(s)
        CascHabitatCard(s)
        CascNatureCard(s)
        Spacer(Modifier.height(8.dp))
    }
}

// ─── Selector de variante ─────────────────────────────────────────
@Composable
private fun VariantChips(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("A", "B", "C", "D").forEachIndexed { i, label ->
            val sel = selected == i
            Surface(
                onClick = { onSelect(i) }, shape = RoundedCornerShape(8.dp),
                color = if (sel) CascGreen else CardBackground,
                border = BorderStroke(1.dp, if (sel) CascGreen else CardBorder)
            ) {
                Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = if (sel) Color.Black else GhostWhite.copy(alpha = 0.6f),
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ─── Contador +/- ────────────────────────────────────────────────
@Composable
private fun CascCounter(
    label: String, value: Int,
    onInc: () -> Unit, onDec: () -> Unit,
    pts: Int? = null
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = GhostWhite.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (pts != null) {
                Text("=$pts PV", color = CascGreen.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End,
                    modifier = Modifier.width(54.dp))
                Spacer(Modifier.width(6.dp))
            }
            IconButton(onClick = onDec, modifier = Modifier.size(32.dp), enabled = value > 0) {
                Icon(Icons.Default.Remove, null,
                    tint = if (value > 0) CascGreen else CardBorder, modifier = Modifier.size(18.dp))
            }
            Text("$value", color = GhostWhite, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp))
            IconButton(onClick = onInc, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null, tint = CascGreen, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─── Card de animal ───────────────────────────────────────────────
@Composable
private fun AnimalCard(
    emoji: String, name: String, score: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(emoji, style = MaterialTheme.typography.titleLarge)
                    Text(name, color = GhostWhite, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                }
                Text("$score PV", color = CascGreen, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

@Composable
private fun CascHint(text: String) {
    Text(text, color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
}

// ─── Osos ─────────────────────────────────────────────────────────
@Composable
private fun CascBearCard(s: CascadiaScoreState) {
    AnimalCard("🐻", stringResource(R.string.casc_bears), s.bearScore) {
        VariantChips(s.bearVariant) { s.bearVariant = it }
        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
        when (s.bearVariant) {
            0 -> {
                CascHint(stringResource(R.string.casc_bear_a_hint))
                CascCounter(stringResource(R.string.casc_bear_a_counter), s.bearPairs,
                    onInc = { s.bearPairs++ }, onDec = { if (s.bearPairs > 0) s.bearPairs-- },
                    pts = BEAR_A_PTS.getOrElse(s.bearPairs) { BEAR_A_PTS.last() })
                Text("1=2 · 2=4 · 3=9 · 4=16 · 5=25", color = GhostWhite.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelSmall)
            }
            1 -> {
                CascHint(stringResource(R.string.casc_bear_b_hint))
                CascCounter(stringResource(R.string.casc_bear_b_counter), s.bearG3b,
                    onInc = { s.bearG3b++ }, onDec = { if (s.bearG3b > 0) s.bearG3b-- },
                    pts = s.bearG3b * 10)
            }
            2 -> {
                CascHint(stringResource(R.string.casc_bear_c_hint))
                CascCounter(stringResource(R.string.casc_groups_of_1), s.bear1c,
                    onInc = { s.bear1c++ }, onDec = { if (s.bear1c > 0) s.bear1c-- }, pts = s.bear1c * 2)
                CascCounter(stringResource(R.string.casc_groups_of_2_5), s.bear2c,
                    onInc = { s.bear2c++ }, onDec = { if (s.bear2c > 0) s.bear2c-- }, pts = s.bear2c * 5)
                CascCounter(stringResource(R.string.casc_groups_of_3_8), s.bear3c,
                    onInc = { s.bear3c++ }, onDec = { if (s.bear3c > 0) s.bear3c-- }, pts = s.bear3c * 8)
                if (s.bear1c > 0 && s.bear2c > 0 && s.bear3c > 0)
                    Text(stringResource(R.string.casc_bear_c_bonus), color = CascGreen,
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
            3 -> {
                CascHint(stringResource(R.string.casc_bear_d_hint))
                CascCounter(stringResource(R.string.casc_groups_of_2_5), s.bear2d,
                    onInc = { s.bear2d++ }, onDec = { if (s.bear2d > 0) s.bear2d-- }, pts = s.bear2d * 5)
                CascCounter(stringResource(R.string.casc_groups_of_3_8), s.bear3d,
                    onInc = { s.bear3d++ }, onDec = { if (s.bear3d > 0) s.bear3d-- }, pts = s.bear3d * 8)
                CascCounter(stringResource(R.string.casc_groups_of_4_13), s.bear4d,
                    onInc = { s.bear4d++ }, onDec = { if (s.bear4d > 0) s.bear4d-- }, pts = s.bear4d * 13)
            }
        }
    }
}

// ─── Salmones ─────────────────────────────────────────────────────
@Composable
private fun CascSalmonCard(s: CascadiaScoreState) {
    AnimalCard("🐟", stringResource(R.string.casc_salmon), s.salmonScore) {
        VariantChips(s.salmonVariant) { s.salmonVariant = it }
        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
        when (s.salmonVariant) {
            0 -> {
                CascHint(stringResource(R.string.casc_sal_a_hint))
                for (sz in 1..7) {
                    CascCounter("$sz (${SALMON_A_PTS[sz]} PV)", s.salA[sz - 1].value,
                        onInc = { s.salA[sz - 1].value++ },
                        onDec = { if (s.salA[sz - 1].value > 0) s.salA[sz - 1].value-- },
                        pts = s.salA[sz - 1].value * SALMON_A_PTS[sz])
                }
            }
            1 -> {
                CascHint(stringResource(R.string.casc_sal_b_hint))
                for (sz in 1..5) {
                    CascCounter("$sz (${SALMON_B_PTS[sz]} PV)", s.salB[sz - 1].value,
                        onInc = { s.salB[sz - 1].value++ },
                        onDec = { if (s.salB[sz - 1].value > 0) s.salB[sz - 1].value-- },
                        pts = s.salB[sz - 1].value * SALMON_B_PTS[sz])
                }
            }
            2 -> {
                CascHint(stringResource(R.string.casc_sal_c_hint))
                for (sz in 3..5) {
                    CascCounter("$sz (${SALMON_C_PTS[sz]} PV)", s.salC[sz - 3].value,
                        onInc = { s.salC[sz - 3].value++ },
                        onDec = { if (s.salC[sz - 3].value > 0) s.salC[sz - 3].value-- },
                        pts = s.salC[sz - 3].value * SALMON_C_PTS[sz])
                }
            }
            3 -> {
                CascHint(stringResource(R.string.casc_sal_d_hint))
                CascCounter(stringResource(R.string.casc_sal_total), s.salDPts,
                    onInc = { s.salDPts++ }, onDec = { if (s.salDPts > 0) s.salDPts-- })
            }
        }
    }
}

// ─── Alces ────────────────────────────────────────────────────────
@Composable
private fun CascElkCard(s: CascadiaScoreState) {
    AnimalCard("🦌", stringResource(R.string.casc_elk), s.elkScore) {
        VariantChips(s.elkVariant) { s.elkVariant = it }
        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
        when (s.elkVariant) {
            0 -> {
                CascHint(stringResource(R.string.casc_elk_a_hint))
                CascCounter(stringResource(R.string.casc_elk_line_2), s.elkL2,
                    onInc = { s.elkL2++ }, onDec = { if (s.elkL2 > 0) s.elkL2-- }, pts = s.elkL2 * 5)
                CascCounter(stringResource(R.string.casc_elk_line_3), s.elkL3,
                    onInc = { s.elkL3++ }, onDec = { if (s.elkL3 > 0) s.elkL3-- }, pts = s.elkL3 * 9)
                CascCounter(stringResource(R.string.casc_elk_line_4), s.elkL4,
                    onInc = { s.elkL4++ }, onDec = { if (s.elkL4 > 0) s.elkL4-- }, pts = s.elkL4 * 13)
            }
            1 -> {
                CascHint(stringResource(R.string.casc_elk_b_hint))
                CascCounter(stringResource(R.string.casc_elk_total), s.elkBPts,
                    onInc = { s.elkBPts++ }, onDec = { if (s.elkBPts > 0) s.elkBPts-- })
            }
            2 -> {
                CascHint(stringResource(R.string.casc_elk_c_hint))
                for (sz in 1..7) {
                    CascCounter("$sz (${ELK_C_PTS[sz]} PV)", s.elkC[sz - 1].value,
                        onInc = { s.elkC[sz - 1].value++ },
                        onDec = { if (s.elkC[sz - 1].value > 0) s.elkC[sz - 1].value-- },
                        pts = s.elkC[sz - 1].value * ELK_C_PTS[sz])
                }
            }
            3 -> {
                CascHint(stringResource(R.string.casc_elk_d_hint))
                CascCounter(stringResource(R.string.casc_elk_rings), s.elkRings,
                    onInc = { s.elkRings++ }, onDec = { if (s.elkRings > 0) s.elkRings-- },
                    pts = s.elkRings * 23)
            }
        }
    }
}

// ─── Halcones ─────────────────────────────────────────────────────
@Composable
private fun CascHawkCard(s: CascadiaScoreState) {
    AnimalCard("🦅", stringResource(R.string.casc_hawks), s.hawkScore) {
        VariantChips(s.hawkVariant) { s.hawkVariant = it }
        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
        when (s.hawkVariant) {
            0 -> {
                CascHint(stringResource(R.string.casc_hawk_a_hint))
                CascCounter(stringResource(R.string.casc_hawk_a_counter), s.hawkIso,
                    onInc = { s.hawkIso++ }, onDec = { if (s.hawkIso > 0) s.hawkIso-- },
                    pts = HAWK_A_PTS.getOrElse(s.hawkIso) { HAWK_A_PTS.last() })
                Text("1=2 · 2=5 · 3=8 · 4=11 · 5=14 · 6=17", color = GhostWhite.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelSmall)
            }
            1 -> {
                CascHint(stringResource(R.string.casc_hawk_b_hint))
                CascCounter(stringResource(R.string.casc_hawk_b_counter), s.hawkBCount,
                    onInc = { s.hawkBCount++ }, onDec = { if (s.hawkBCount > 0) s.hawkBCount-- },
                    pts = HAWK_B_PTS.getOrElse(s.hawkBCount) { HAWK_B_PTS.last() })
                Text("1=3 · 2=7 · 3=12 · 4=16", color = GhostWhite.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelSmall)
            }
            2 -> {
                CascHint(stringResource(R.string.casc_hawk_c_hint))
                CascCounter(stringResource(R.string.casc_hawk_c_counter), s.hawkLOS,
                    onInc = { s.hawkLOS++ }, onDec = { if (s.hawkLOS > 0) s.hawkLOS-- },
                    pts = s.hawkLOS * 3)
            }
            3 -> {
                CascHint(stringResource(R.string.casc_hawk_d_hint))
                CascCounter(stringResource(R.string.casc_hawk_total), s.hawkDPts,
                    onInc = { s.hawkDPts++ }, onDec = { if (s.hawkDPts > 0) s.hawkDPts-- })
            }
        }
    }
}

// ─── Zorros ───────────────────────────────────────────────────────
@Composable
private fun CascFoxCard(s: CascadiaScoreState) {
    AnimalCard("🦊", stringResource(R.string.casc_foxes), s.foxScore) {
        VariantChips(s.foxVariant) { s.foxVariant = it }
        HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
        when (s.foxVariant) {
            0 -> {
                CascHint(stringResource(R.string.casc_fox_a_hint))
                for (i in 0..5) {
                    CascCounter("Zorros con $i tipo${if (i != 1) "s" else ""} adj. (${FOX_A_PTS[i]} PV c/u)",
                        s.foxA[i].value,
                        onInc = { s.foxA[i].value++ },
                        onDec = { if (s.foxA[i].value > 0) s.foxA[i].value-- },
                        pts = s.foxA[i].value * FOX_A_PTS[i])
                }
            }
            1 -> {
                CascHint(stringResource(R.string.casc_fox_b_hint))
                for (i in 0..3) {
                    CascCounter("Zorros con $i pareja${if (i != 1) "s" else ""} adj. (${FOX_B_PTS[i]} PV c/u)",
                        s.foxB[i].value,
                        onInc = { s.foxB[i].value++ },
                        onDec = { if (s.foxB[i].value > 0) s.foxB[i].value-- },
                        pts = s.foxB[i].value * FOX_B_PTS[i])
                }
            }
            2 -> {
                CascHint(stringResource(R.string.casc_fox_c_hint))
                for (i in 0..4) {
                    CascCounter("Zorros con máx. $i igual${if (i != 1) "es" else ""} adj. (${FOX_C_PTS[i]} PV c/u)",
                        s.foxC[i].value,
                        onInc = { s.foxC[i].value++ },
                        onDec = { if (s.foxC[i].value > 0) s.foxC[i].value-- },
                        pts = s.foxC[i].value * FOX_C_PTS[i])
                }
            }
            3 -> {
                CascHint(stringResource(R.string.casc_fox_d_hint))
                for (i in 0..4) {
                    CascCounter("Parejas con $i par${if (i != 1) "es" else ""} únicos adj. (${FOX_D_PTS[i]} PV c/u)",
                        s.foxD[i].value,
                        onInc = { s.foxD[i].value++ },
                        onDec = { if (s.foxD[i].value > 0) s.foxD[i].value-- },
                        pts = s.foxD[i].value * FOX_D_PTS[i])
                }
            }
        }
    }
}

// ─── Hábitats ─────────────────────────────────────────────────────
@Composable
private fun CascHabitatCard(s: CascadiaScoreState) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🌿", style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.casc_habitats), color = GhostWhite, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                }
                Text("${s.habitatScore} PV", color = CascGreen, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
            }
            CascHint(stringResource(R.string.casc_hab_hint))
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            CascCounter(stringResource(R.string.casc_hab_mtn), s.hMtn,
                onInc = { s.hMtn++ }, onDec = { if (s.hMtn > 0) s.hMtn-- }, pts = s.hMtn)
            CascCounter(stringResource(R.string.casc_hab_for), s.hFor,
                onInc = { s.hFor++ }, onDec = { if (s.hFor > 0) s.hFor-- }, pts = s.hFor)
            CascCounter(stringResource(R.string.casc_hab_pra), s.hPra,
                onInc = { s.hPra++ }, onDec = { if (s.hPra > 0) s.hPra-- }, pts = s.hPra)
            CascCounter(stringResource(R.string.casc_hab_wet), s.hWet,
                onInc = { s.hWet++ }, onDec = { if (s.hWet > 0) s.hWet-- }, pts = s.hWet)
            CascCounter(stringResource(R.string.casc_hab_riv), s.hRiv,
                onInc = { s.hRiv++ }, onDec = { if (s.hRiv > 0) s.hRiv-- }, pts = s.hRiv)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
            CascCounter(stringResource(R.string.casc_hab_bonus), s.hBonus,
                onInc = { s.hBonus++ }, onDec = { if (s.hBonus > 0) s.hBonus-- }, pts = s.hBonus * 2)
        }
    }
}

// ─── Fichas de naturaleza ─────────────────────────────────────────
@Composable
private fun CascNatureCard(s: CascadiaScoreState) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🍄", style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.casc_nature_tokens), color = GhostWhite, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                }
                Text("${s.nature} PV", color = CascGreen, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
            }
            CascCounter(stringResource(R.string.casc_nature_counter), s.nature,
                onInc = { s.nature++ }, onDec = { if (s.nature > 0) s.nature-- }, pts = s.nature)
        }
    }
}

// ─── Tab de reglas ────────────────────────────────────────────────
@Composable
private fun CascRulesTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.casc_rules_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        CascRuleCard(stringResource(R.string.casc_rules_bears_title), stringResource(R.string.casc_rules_bears_body))
        CascRuleCard(stringResource(R.string.casc_rules_salmon_title), stringResource(R.string.casc_rules_salmon_body))
        CascRuleCard(stringResource(R.string.casc_rules_elk_title), stringResource(R.string.casc_rules_elk_body))
        CascRuleCard(stringResource(R.string.casc_rules_hawk_title), stringResource(R.string.casc_rules_hawk_body))
        CascRuleCard(stringResource(R.string.casc_rules_fox_title), stringResource(R.string.casc_rules_fox_body))
        CascRuleCard(stringResource(R.string.casc_rules_hab_title), stringResource(R.string.casc_rules_hab_body))
        CascRuleCard(stringResource(R.string.casc_rules_nature_title), stringResource(R.string.casc_rules_nature_body))
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CascRuleCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = CascGreen, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ─── Tab Setup ─────────────────────────────────────────────────────
@Composable
private fun CascSetupTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.casc_setup_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.casc_setup_subtitle),
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        CascSetupBlock(stringResource(R.string.casc_setup_board_title), stringResource(R.string.casc_setup_board_body))
        CascSetupBlock(stringResource(R.string.casc_setup_player_title), stringResource(R.string.casc_setup_player_body))
        CascSetupBlock(stringResource(R.string.casc_setup_wildlife_title), stringResource(R.string.casc_setup_wildlife_body))
        CascSetupBlock(stringResource(R.string.casc_setup_start_title), stringResource(R.string.casc_setup_start_body))
    }
}

@Composable
private fun CascSetupBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1F12)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CascGreen.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = CascGreen, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall)
            Text(body, color = GhostWhite.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
