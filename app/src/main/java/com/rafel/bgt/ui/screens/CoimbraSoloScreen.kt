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

private val CoiAmber  = Color(0xFFD4900A)
private val CoiGold   = Color(0xFFF5B800)

// ─── Tablas de dados (built inside composable for localization) ───

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

    val ratingIndex get() = when {
        total < 130 -> 0
        total < 160 -> 1
        total < 190 -> 2
        total < 220 -> 3
        else        -> 4
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
                        Text(stringResource(R.string.coi_title), color = GhostWhite, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.coi_subtitle),
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
                    Triple(0, Icons.Default.Settings, stringResource(R.string.nav_setup)),
                    Triple(1, Icons.Default.SmartToy, stringResource(R.string.nav_solo)),
                    Triple(2, Icons.Default.EmojiEvents, stringResource(R.string.nav_scoring)),
                    Triple(3, Icons.Default.MenuBook, stringResource(R.string.nav_rules))
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
    val diceTable = listOf(
        Triple("1", "🟣", "Morado"),
        Triple("2", "🟢", "Verde"),
        Triple("3", "🟠", "Naranja bajo"),
        Triple("4", "⬜", "Gris bajo"),
        Triple("5", "🔴", "Naranja alto"),
        Triple("6", "⬜", "Gris alto"),
    )
    val cityTable = listOf(
        Triple("1–2", "🏰", "Ciudad Alta"),
        Triple("3–4", "🏛️", "Ciudad Central"),
        Triple("5–6", "🏠", "Ciudad Baja"),
    )
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ronda
        CoiCard {
            CoiSectionHeader(stringResource(R.string.coi_solo_tab_title))
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
                    Text(stringResource(R.string.coi_of_four), color = GhostWhite.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { if (s.round < 4) s.round++ }, modifier = Modifier.size(44.dp),
                    enabled = s.round < 4) {
                    Icon(Icons.Default.Add, null,
                        tint = if (s.round < 4) CoiAmber else CardBorder,
                        modifier = Modifier.size(24.dp))
                }
            }
            Text(stringResource(R.string.coi_bot_warning),
                color = CoiAmber.copy(alpha = 0.8f), fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall)
        }

        // Tabla de dados del Bot
        CoiCard {
            CoiSectionHeader(stringResource(R.string.coi_dice_title),
                stringResource(R.string.coi_dice_subtitle))
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            diceTable.forEach { (num, emoji, name) ->
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
            Text(stringResource(R.string.coi_dice_fallback),
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.coi_dice_lowhi),
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        }

        // Colocación en ciudad
        CoiCard {
            CoiSectionHeader(stringResource(R.string.coi_city_title),
                stringResource(R.string.coi_city_subtitle))
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            cityTable.forEach { (rango, emoji, name) ->
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
                Text(stringResource(R.string.coi_city_restrictions), color = CoiAmber.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.coi_city_r1),
                    color = GhostWhite.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.coi_city_r2),
                    color = GhostWhite.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.coi_city_r3),
                    color = GhostWhite.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Castillo
        CoiCard {
            CoiSectionHeader(stringResource(R.string.coi_castle_title))
            Text(stringResource(R.string.coi_castle_hint),
                color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.coi_castle_check),
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.coi_castle_ge), color = GhostWhite.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center)
                            Text(stringResource(R.string.coi_castle_ge_result), color = CoiAmber, fontWeight = FontWeight.Bold,
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
                            Text(stringResource(R.string.coi_castle_lt), color = GhostWhite.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center)
                            Text(stringResource(R.string.coi_castle_lt_result), color = BloodRed, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center)
                        }
                    }
                }
                Text(stringResource(R.string.coi_castle_steal_note),
                    color = GhostWhite.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Cartas
        CoiCard {
            CoiSectionHeader(stringResource(R.string.coi_cards_title))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.coi_cards_body1),
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.coi_cards_body2),
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
                val cardRolls = listOf(
                    stringResource(R.string.coi_cards_roll_4),
                    stringResource(R.string.coi_cards_roll_3),
                    stringResource(R.string.coi_cards_roll_2),
                    stringResource(R.string.coi_cards_roll_1)
                )
                cardRolls.forEach { txt ->
                    Text("• $txt", color = GhostWhite.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
                Text(stringResource(R.string.coi_cards_influence),
                    color = CoiAmber, fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.coi_cards_no_effect),
                    color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Coronas
        CoiCard {
            CoiSectionHeader(stringResource(R.string.coi_crowns_title))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.coi_crowns_body1),
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.coi_crowns_body2),
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.coi_crowns_body3),
                    color = GhostWhite.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Peregrinaciones
        CoiCard {
            CoiSectionHeader(stringResource(R.string.coi_pilgrimages_title))
            Text(stringResource(R.string.coi_pilgrimages_body),
                color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
        }

        // Cartas de personaje
        CoiCard {
            CoiSectionHeader(stringResource(R.string.coi_char_cards_title))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.coi_char_cards_body1),
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.coi_char_cards_body2),
                    color = GhostWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.coi_char_cards_body3),
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
            val ratingLabels = listOf(
                stringResource(R.string.coi_rank_bad),
                stringResource(R.string.coi_rank_ok),
                stringResource(R.string.coi_rank_good),
                stringResource(R.string.coi_rank_great),
                stringResource(R.string.coi_rank_amazing)
            )
            Column(Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.coi_score_total), color = GhostWhite.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                Text("${s.total}", color = CoiGold, fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displaySmall)
                Text(ratingLabels[s.ratingIndex], color = s.ratingColor, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                // Escala visual
                RatingBar(s.total)
            }
        }

        // Categorías de puntuación
        CoiCard {
            Text(stringResource(R.string.coi_score_breakdown), color = CoiAmber, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

            CoiCounter(stringResource(R.string.coi_score_cards), s.ptCartas,
                onInc = { s.ptCartas++ }, onDec = { if (s.ptCartas > 0) s.ptCartas-- },
                pts = "${s.ptCartas} PV",
                subtitle = stringResource(R.string.coi_score_cards_sub))
            CoiCounter(stringResource(R.string.coi_score_pilgrimages), s.ptPeregrin,
                onInc = { s.ptPeregrin++ }, onDec = { if (s.ptPeregrin > 0) s.ptPeregrin-- },
                pts = "${s.ptPeregrin} PV",
                subtitle = stringResource(R.string.coi_score_pilgrimages_sub))
            CoiCounter(stringResource(R.string.coi_score_influence), s.ptInfluencia,
                onInc = { s.ptInfluencia++ }, onDec = { if (s.ptInfluencia > 0) s.ptInfluencia-- },
                pts = "${s.ptInfluencia} PV",
                subtitle = stringResource(R.string.coi_score_influence_sub))
            CoiCounter(stringResource(R.string.coi_score_coins), s.ptMonedas,
                onInc = { s.ptMonedas++ }, onDec = { if (s.ptMonedas > 0) s.ptMonedas-- },
                pts = "${s.ptMonedas} PV",
                subtitle = stringResource(R.string.coi_score_coins_sub))
            CoiCounter(stringResource(R.string.coi_score_favors), s.ptFavores,
                onInc = { s.ptFavores++ }, onDec = { if (s.ptFavores > 0) s.ptFavores-- },
                pts = "${s.ptFavores} PV",
                subtitle = stringResource(R.string.coi_score_favors_sub))
            CoiCounter(stringResource(R.string.coi_score_others), s.ptOtros,
                onInc = { s.ptOtros++ }, onDec = { if (s.ptOtros > 0) s.ptOtros-- },
                pts = "${s.ptOtros} PV",
                subtitle = stringResource(R.string.coi_score_others_sub))
        }

        // Tabla de clasificación
        CoiCard {
            Text(stringResource(R.string.coi_ranking_title), color = CoiAmber, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            listOf(
                Triple("< 130",   stringResource(R.string.coi_rank_bad),     BloodRed),
                Triple("130–160", stringResource(R.string.coi_rank_ok),      GhostWhite),
                Triple("160–190", stringResource(R.string.coi_rank_good),    CoiAmber),
                Triple("190–220", stringResource(R.string.coi_rank_great),   CoiGold),
                Triple("> 220",   stringResource(R.string.coi_rank_amazing), HalloweenOrange),
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
        Text(stringResource(R.string.coi_rules_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.coi_rules_subtitle),
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        CoiRuleBlock(stringResource(R.string.coi_rules_prep_title), stringResource(R.string.coi_rules_prep_body))
        CoiRuleBlock(stringResource(R.string.coi_rules_dice_title), stringResource(R.string.coi_rules_dice_body))
        CoiRuleBlock(stringResource(R.string.coi_rules_city_title), stringResource(R.string.coi_rules_city_body))
        CoiRuleBlock(stringResource(R.string.coi_rules_castle_title), stringResource(R.string.coi_rules_castle_body))
        CoiRuleBlock(stringResource(R.string.coi_rules_cards_title), stringResource(R.string.coi_rules_cards_body))
        CoiRuleBlock(stringResource(R.string.coi_rules_crowns_title), stringResource(R.string.coi_rules_crowns_body))
        CoiRuleBlock(stringResource(R.string.coi_rules_steal_title), stringResource(R.string.coi_rules_steal_body))
        CoiRuleBlock(stringResource(R.string.coi_rules_results_title), stringResource(R.string.coi_rules_results_body))

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
        Text(stringResource(R.string.coi_setup_title), color = GhostWhite, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.coi_setup_subtitle),
            color = GhostWhite.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

        CoiRuleBlock(stringResource(R.string.coi_setup_board_title), stringResource(R.string.coi_setup_board_body))
        CoiRuleBlock(stringResource(R.string.coi_setup_player_title), stringResource(R.string.coi_setup_player_body))
        CoiRuleBlock(stringResource(R.string.coi_setup_solo_title), stringResource(R.string.coi_setup_solo_body))
        CoiRuleBlock(stringResource(R.string.coi_setup_turn_title), stringResource(R.string.coi_setup_turn_body))
    }
}
