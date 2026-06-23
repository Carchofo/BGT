package com.rafel.bgt.ui.screens

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
import com.rafel.bgt.R
import com.rafel.bgt.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafel.bgt.ui.viewmodels.CastleComboViewModel

private val CCGold   = Color(0xFFDAA520)
private val CCBrown  = Color(0xFF8B4513)
private val CCCream  = Color(0xFFFFF8DC)

// ─── Estado compartido ────────────────────────────────────────────
class CastleComboState {
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
fun CastleComboSoloScreen(onBack: () -> Unit = {}, vm: CastleComboViewModel = viewModel()) {
    val s = vm.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.cc_title), color = GhostWhite, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.cc_subtitle),
                            color = CCGold.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (vm.selectedTab != 0) vm.selectedTab = 0 else onBack()
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
                    Triple(0, Icons.Default.Settings, stringResource(R.string.nav_setup)),
                    Triple(1, Icons.Default.SmartToy, stringResource(R.string.nav_solo)),
                    Triple(2, Icons.Default.EmojiEvents, stringResource(R.string.nav_scoring)),
                    Triple(3, Icons.Default.MenuBook, stringResource(R.string.nav_rules))
                ).forEach { (idx, icon, label) ->
                    NavigationBarItem(
                        selected = vm.selectedTab == idx, onClick = { vm.selectedTab = idx },
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
        when (vm.selectedTab) {
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
            CCSectionHeader(stringResource(R.string.cc_difficulty))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    stringResource(R.string.cc_difficulty_easy),
                    stringResource(R.string.cc_difficulty_normal),
                    stringResource(R.string.cc_difficulty_hard)
                ).forEachIndexed { i, label ->
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
            CCSectionHeader(stringResource(R.string.cc_turn_label))
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
                    Text(stringResource(R.string.cc_of_nine), color = GhostWhite.copy(alpha = 0.5f),
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
                val label = if (isAntonTurn) stringResource(R.string.cc_anton_turn) else stringResource(R.string.cc_your_turn)
                val color = if (isAntonTurn) CCGold else GhostWhite
                Text(label, color = color, fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        // Recursos de Anton
        CCCard {
            CCSectionHeader(stringResource(R.string.cc_anton_resources_title))
            Text(stringResource(R.string.cc_anton_resources_hint),
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            CCCounter(stringResource(R.string.cc_diff_row_coins), s.antonCoins,
                onInc = { s.antonCoins++ }, onDec = { if (s.antonCoins > 0) s.antonCoins-- },
                subtitle = antonCoinRule(s.difficulty))
            CCCounter(stringResource(R.string.cc_diff_row_keys), s.antonKeys,
                onInc = { s.antonKeys++ }, onDec = { if (s.antonKeys > 0) s.antonKeys-- },
                subtitle = if (s.difficulty < 2) stringResource(R.string.cc_coin_rule_normal) else stringResource(R.string.cc_coin_rule_hard))
            CCCounter(stringResource(R.string.cc_diff_row_discounts), s.antonDiscounts,
                onInc = { s.antonDiscounts++ }, onDec = { if (s.antonDiscounts > 0) s.antonDiscounts-- },
                subtitle = "${antonDiscountRate(s.difficulty)} PV cada uno · descuento doble = 2 descuentos")
        }

        // Guía del turno de Anton
        CCCard {
            CCSectionHeader(stringResource(R.string.cc_anton_guide_title))
            AntonTurnGuide()
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DifficultyTable(difficulty: Int) {
    val headers = listOf("", stringResource(R.string.cc_difficulty_easy), stringResource(R.string.cc_difficulty_normal), stringResource(R.string.cc_difficulty_hard))
    val rows = listOf(
        listOf(stringResource(R.string.cc_diff_row_keys), stringResource(R.string.cc_diff_keys_easy), stringResource(R.string.cc_diff_keys_normal), stringResource(R.string.cc_diff_keys_hard)),
        listOf(stringResource(R.string.cc_diff_row_coins), stringResource(R.string.cc_diff_coins_easy), stringResource(R.string.cc_diff_coins_normal), stringResource(R.string.cc_diff_coins_hard)),
        listOf(stringResource(R.string.cc_diff_row_discounts), stringResource(R.string.cc_diff_discounts_easy), stringResource(R.string.cc_diff_discounts_normal), stringResource(R.string.cc_diff_discounts_hard)),
        listOf(stringResource(R.string.cc_diff_row_if_missing), stringResource(R.string.cc_diff_if_missing_easy), stringResource(R.string.cc_diff_if_missing_normal), stringResource(R.string.cc_diff_if_missing_hard)),
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
        stringResource(R.string.cc_guide_step_1),
        stringResource(R.string.cc_guide_step_2),
        stringResource(R.string.cc_guide_step_3),
        stringResource(R.string.cc_guide_step_4),
        stringResource(R.string.cc_guide_step_5),
        stringResource(R.string.cc_guide_step_6),
        stringResource(R.string.cc_guide_step_7)
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

private fun antonDiscountRate(d: Int) = when (d) { 0 -> 1; 1 -> 2; else -> 3 }

@Composable
private fun antonCoinRule(d: Int) = when (d) {
    0 -> stringResource(R.string.cc_coin_rule_easy)
    1 -> stringResource(R.string.cc_coin_rule_normal)
    else -> stringResource(R.string.cc_coin_rule_hard)
}

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
                        tie        -> stringResource(R.string.cc_result_tie)
                        playerWins -> stringResource(R.string.cc_result_win)
                        else       -> stringResource(R.string.cc_result_anton_wins)
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
            CCSectionHeader(stringResource(R.string.cc_score_title), "${s.playerTotal} PV")
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            CCCounter(stringResource(R.string.cc_scrolls_player), s.playerCards,
                onInc = { s.playerCards++ }, onDec = { if (s.playerCards > 0) s.playerCards-- },
                pts = "${s.playerCards} PV")
            CCCounter(stringResource(R.string.cc_keys_player), s.playerKeys,
                onInc = { s.playerKeys++ }, onDec = { if (s.playerKeys > 0) s.playerKeys-- },
                pts = "${s.playerKeys} PV")
        }

        // Puntuación de Anton
        CCCard {
            CCSectionHeader(stringResource(R.string.cc_score_anton_title), "${s.antonTotal} PV")
            Text(stringResource(R.string.cc_difficulty_display, listOf(
                stringResource(R.string.cc_difficulty_easy),
                stringResource(R.string.cc_difficulty_normal),
                stringResource(R.string.cc_difficulty_hard)
            )[s.difficulty]),
                color = CCGold.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            CCCounter(stringResource(R.string.cc_scrolls_anton), s.antonCards,
                onInc = { s.antonCards++ }, onDec = { if (s.antonCards > 0) s.antonCards-- },
                pts = "${s.antonCards} PV")
            CCCounter(if (s.difficulty < 2) stringResource(R.string.cc_keys_anton_normal) else stringResource(R.string.cc_keys_anton_hard), s.antonKeys,
                onInc = { s.antonKeys++ }, onDec = { if (s.antonKeys > 0) s.antonKeys-- },
                pts = "${s.antonKeyVP()} PV")
            CCCounter(stringResource(R.string.cc_coins_anton, antonCoinRule(s.difficulty)), s.antonCoins,
                onInc = { s.antonCoins++ }, onDec = { if (s.antonCoins > 0) s.antonCoins-- },
                pts = "${s.antonCoinVP()} PV")
            CCCounter(stringResource(R.string.cc_discounts_anton, antonDiscountRate(s.difficulty)), s.antonDiscounts,
                onInc = { s.antonDiscounts++ }, onDec = { if (s.antonDiscounts > 0) s.antonDiscounts-- },
                pts = "${s.antonDiscountVP()} PV")
            HorizontalDivider(color = CardBorder.copy(alpha = 0.3f))
            Text(stringResource(R.string.cc_notes_if_missing,
                if (s.difficulty == 0) stringResource(R.string.cc_notes_if_missing_easy) else stringResource(R.string.cc_notes_if_missing_hard)
            ), color = GhostWhite.copy(alpha = 0.45f), style = MaterialTheme.typography.bodySmall)
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
        Text(stringResource(R.string.cc_rules_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.cc_rules_subtitle),
            color = GhostWhite.copy(alpha = 0.45f), style = MaterialTheme.typography.bodySmall)

        CCRuleBlock(stringResource(R.string.cc_rules_prep_title), stringResource(R.string.cc_rules_prep_body))
        CCRuleBlock(stringResource(R.string.cc_rules_anton_turn_title), stringResource(R.string.cc_rules_anton_turn_body))
        CCRuleBlock(stringResource(R.string.cc_rules_effects_title), stringResource(R.string.cc_rules_effects_body))
        CCRuleBlock(stringResource(R.string.cc_rules_scoring_title), stringResource(R.string.cc_rules_scoring_body))
        CCRuleBlock(stringResource(R.string.cc_rules_summary_title), stringResource(R.string.cc_rules_summary_body))

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
        Text(stringResource(R.string.cc_setup_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.cc_setup_subtitle),
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        CCSetupBlock(stringResource(R.string.cc_setup_board_title), stringResource(R.string.cc_setup_board_body))
        CCSetupBlock(stringResource(R.string.cc_setup_player_title), stringResource(R.string.cc_setup_player_body))
        CCSetupBlock(stringResource(R.string.cc_setup_solo_title), stringResource(R.string.cc_setup_solo_body))
        CCSetupBlock(stringResource(R.string.cc_setup_turn_title), stringResource(R.string.cc_setup_turn_body))
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
