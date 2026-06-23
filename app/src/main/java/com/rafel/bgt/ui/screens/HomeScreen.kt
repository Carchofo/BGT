package com.rafel.bgt.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.annotation.StringRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafel.bgt.BugReporter
import com.rafel.bgt.R
import com.rafel.bgt.ui.theme.*
import com.rafel.bgt.ui.util.SoundSettings
import com.rafel.bgt.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

enum class ViewMode { LIST, GRID }

enum class GameFeature(val emoji: String, @StringRes val labelRes: Int) {
    SOLO("🤖", R.string.feature_solo),
    SCORING("🏆", R.string.feature_scoring),
    RULES("📖", R.string.feature_rules)
}

data class GameItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val hasBanner: Boolean = false,
    val bannerRes: Int? = null,
    val available: Boolean = false,
    val route: String = "",
    val tag: String? = null,
    val features: Set<GameFeature> = emptySet()
)

private val GAMES = listOf(
    GameItem(
        id = "spooktacular", title = "Spooktacular",
        subtitle = "Terror en el Silver Cinema • 1-5 jugadores",
        hasBanner = true, available = true, route = "solo_mode", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_spooktacular_new,
        features = setOf(GameFeature.SOLO, GameFeature.SCORING)
    ),
    GameItem(
        id = "cm", title = "Criaturas Maravillosas",
        subtitle = "Exploración de isla • Modo solitario vs Tingent",
        hasBanner = true, available = true, route = "cm_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_cm_new,
        features = setOf(GameFeature.SOLO, GameFeature.SCORING, GameFeature.RULES)
    ),
    GameItem(
        id = "tiletum", title = "Tiletum",
        subtitle = "Mercaderes del Renacimiento • Modo solitario vs Titus",
        hasBanner = true, available = true, route = "tiletum_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_tiletum_img,
        features = setOf(GameFeature.SOLO, GameFeature.SCORING, GameFeature.RULES)
    ),
    GameItem(
        id = "maracaibo", title = "Piratas de Maracaibo",
        subtitle = "El Caribe, s. XVII • Modo solitario vs Jordán",
        hasBanner = true, available = true, route = "maracaibo_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_maracaibo,
        features = setOf(GameFeature.SOLO, GameFeature.SCORING, GameFeature.RULES)
    ),
    GameItem(
        id = "castle_combo", title = "Castle Combo",
        subtitle = "Fiesta de los Combos • Modo solitario vs Anton",
        hasBanner = true, available = true, route = "castle_combo_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_castle_combo,
        features = setOf(GameFeature.SOLO, GameFeature.SCORING, GameFeature.RULES)
    ),
    GameItem(
        id = "cascadia", title = "Cascadia",
        subtitle = "Noroeste del Pacífico • Calculadora de puntuación",
        hasBanner = true, available = true, route = "cascadia_score", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_cascadia,
        features = setOf(GameFeature.SCORING)
    ),
    GameItem(
        id = "coimbra", title = "Coimbra",
        subtitle = "Portugal medieval • Modo solitario Bot Interferencia",
        hasBanner = true, available = true, route = "coimbra_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_coimbra,
        features = setOf(GameFeature.SOLO, GameFeature.RULES)
    ),
    GameItem(
        id = "friday", title = "Friday",
        subtitle = "Solo puro • Construye tu mazo para sobrevivir",
        hasBanner = false, available = true, route = "friday_solo", tag = "DISPONIBLE",
        features = setOf(GameFeature.SOLO, GameFeature.RULES, GameFeature.SCORE)
    ),
    GameItem(id = "g5", title = "Próximamente", subtitle = ""),
    GameItem(id = "g6", title = "Próximamente", subtitle = ""),
    GameItem(id = "g7", title = "Próximamente", subtitle = ""),
    GameItem(id = "g8", title = "Próximamente", subtitle = ""),
)

// Pre-sorted: available games A-Z, then "Próximamente" slots at end
private val GAMES_SORTED = GAMES.filter { it.available }.sortedBy { it.title } +
        GAMES.filter { !it.available }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    onAbout: () -> Unit = {},
    vm: HomeViewModel = viewModel()
) {
    val favoriteIds by vm.favoriteIds.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var activeFeatures by remember { mutableStateOf(emptySet<GameFeature>()) }
    var showBugDialog by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isFiltering = searchQuery.isNotBlank() || showFavoritesOnly || activeFeatures.isNotEmpty()

    // Filter logic
    val filteredGames = remember(searchQuery, showFavoritesOnly, favoriteIds, activeFeatures) {
        var result = GAMES_SORTED.filter { it.available }
        if (showFavoritesOnly) result = result.filter { it.id in favoriteIds }
        if (activeFeatures.isNotEmpty()) result = result.filter { game ->
            activeFeatures.all { it in game.features }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            result = result.filter { game ->
                game.title.contains(q, ignoreCase = true) ||
                game.subtitle.contains(q, ignoreCase = true)
            }
        }
        if (!isFiltering) {
            result + GAMES_SORTED.filter { !it.available }
        } else {
            result
        }
    }

    val isMuted by SoundSettings.isMuted

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Chips de feature en lugar del título
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameFeature.entries.forEach { feature ->
                            val selected = feature in activeFeatures
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    activeFeatures = if (selected) activeFeatures - feature else activeFeatures + feature
                                },
                                label = { Text("${feature.emoji} ${stringResource(feature.labelRes)}", style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HalloweenOrange.copy(alpha = 0.18f),
                                    selectedLabelColor = HalloweenOrange,
                                    containerColor = CardBackground,
                                    labelColor = GhostWhite.copy(alpha = 0.55f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = CardBorder,
                                    selectedBorderColor = HalloweenOrange.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Acerca de",
                            tint = GhostWhite.copy(alpha = 0.45f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { showBugDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Reportar bug",
                            tint = GhostWhite.copy(alpha = 0.45f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (showFavoritesOnly) stringResource(R.string.cd_show_all) else stringResource(R.string.cd_show_favorites),
                            tint = if (showFavoritesOnly) HalloweenOrange else GhostWhite.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { SoundSettings.toggle(context) }) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) stringResource(R.string.cd_unmute) else stringResource(R.string.cd_mute),
                            tint = if (isMuted) GhostWhite.copy(alpha = 0.35f) else GhostWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    ViewToggle(current = viewMode, onToggle = { viewMode = it })
                    Spacer(Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        containerColor = MidnightBlue
    ) { padding ->
        if (showBugDialog) {
            BugReportDialog(
                onDismiss = { showBugDialog = false },
                onSend = { game, msg ->
                    showBugDialog = false
                    scope.launch { BugReporter.send(context, game, msg) }
                }
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(MidnightBlue, Color(0xFF0D0D1A))))
        ) {
            // ── Barra de búsqueda ──────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.search_placeholder),
                        color = GhostWhite.copy(alpha = 0.35f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = GhostWhite.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; keyboard?.hide() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_clear_search),
                                tint = GhostWhite.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HalloweenOrange,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    cursorColor = HalloweenOrange,
                    focusedTextColor = GhostWhite,
                    unfocusedTextColor = GhostWhite
                )
            )

            // ── Lista / Grid / Empty ───────────────────────────────
            if (filteredGames.isEmpty()) {
                EmptyState(showFavoritesOnly)
            } else {
                when (viewMode) {
                    ViewMode.LIST -> GameListView(filteredGames, favoriteIds, vm::toggleFavorite, onNavigate)
                    ViewMode.GRID -> GameGridView(filteredGames, favoriteIds, vm::toggleFavorite, onNavigate)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BugReportDialog(
    onDismiss: () -> Unit,
    onSend: (game: String, message: String) -> Unit
) {
    val games = listOf("General", "Spooktacular", "Criaturas Maravillosas", "Tiletum", "Maracaibo", "Castle Combo", "Cascadia", "Coimbra")
    var selectedGame by remember { mutableStateOf(games[0]) }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🐛 Reportar bug", fontWeight = FontWeight.Bold, color = GhostWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedGame,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Juego", color = GhostWhite.copy(alpha = 0.6f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HalloweenOrange,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = GhostWhite,
                            unfocusedTextColor = GhostWhite
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        games.forEach { game ->
                            DropdownMenuItem(
                                text = { Text(game) },
                                onClick = { selectedGame = game; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Descripción del problema", color = GhostWhite.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HalloweenOrange,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = GhostWhite,
                        unfocusedTextColor = GhostWhite,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (message.isNotBlank()) onSend(selectedGame, message) },
                enabled = message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HalloweenOrange)
            ) { Text("Enviar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = GhostWhite.copy(alpha = 0.6f)) }
        },
        containerColor = CardBackground
    )
}

@Composable
private fun EmptyState(isFavoritesFilter: Boolean) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isFavoritesFilter) Icons.Default.StarBorder else Icons.Default.SearchOff,
            contentDescription = null,
            tint = GhostWhite.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isFavoritesFilter) stringResource(R.string.empty_no_favorites_title) else stringResource(R.string.empty_no_results_title),
            style = MaterialTheme.typography.titleMedium,
            color = GhostWhite.copy(alpha = 0.35f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isFavoritesFilter)
                stringResource(R.string.empty_no_favorites_body)
            else
                stringResource(R.string.empty_no_results_body),
            style = MaterialTheme.typography.bodySmall,
            color = GhostWhite.copy(alpha = 0.22f)
        )
    }
}

@Composable
private fun ViewToggle(current: ViewMode, onToggle: (ViewMode) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
    ) {
        ViewMode.entries.forEach { mode ->
            val selected = current == mode
            IconButton(
                onClick = { onToggle(mode) },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selected) HalloweenOrange.copy(alpha = 0.25f) else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = if (mode == ViewMode.LIST) Icons.Default.ViewAgenda else Icons.Default.GridView,
                    contentDescription = null,
                    tint = if (selected) HalloweenOrange else GhostWhite.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun GameListView(
    games: List<GameItem>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(games, key = { it.id }) { game ->
            GameCardList(
                game = game,
                isFavorite = game.id in favoriteIds,
                onToggleFavorite = { onToggleFavorite(game.id) },
                onClick = { if (game.available) onNavigate(game.route) }
            )
        }
    }
}

@Composable
private fun GameGridView(
    games: List<GameItem>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(games, key = { it.id }) { game ->
            GameCardGrid(
                game = game,
                isFavorite = game.id in favoriteIds,
                onToggleFavorite = { onToggleFavorite(game.id) },
                onClick = { if (game.available) onNavigate(game.route) }
            )
        }
    }
}

@Composable
private fun GameCardList(
    game: GameItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "s"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.dp,
            if (game.available) Brush.horizontalGradient(listOf(HalloweenOrange, SpookyPurple))
            else Brush.horizontalGradient(listOf(CardBorder, CardBorder))
        ),
        elevation = CardDefaults.cardElevation(if (game.available) 6.dp else 1.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (game.hasBanner && game.bannerRes != null) {
                Image(
                    painter = painterResource(game.bannerRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(listOf(NightBlack.copy(alpha = 0.6f), Color.Transparent))
                    )
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(Color(0xFF1C1C2E), Color(0xFF252535)))
                    )
                )
            }

            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    if (game.available) {
                        if (game.features.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                game.features.forEach { feature ->
                                    Text(
                                        "${feature.emoji} ${stringResource(feature.labelRes)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = HalloweenOrange.copy(alpha = 0.75f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            game.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = GhostWhite,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Icon(
                            Icons.Default.Lock, null,
                            tint = GhostWhite.copy(alpha = 0.2f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.label_coming_soon),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GhostWhite.copy(alpha = 0.25f)
                        )
                    }
                }
                if (game.available) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isFavorite) stringResource(R.string.cd_remove_favorite) else stringResource(R.string.cd_add_favorite),
                            tint = if (isFavorite) HalloweenOrange else GhostWhite.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        Icons.Default.PlayArrow, null,
                        tint = HalloweenOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCardGrid(
    game: GameItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "s"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.dp,
            if (game.available) Brush.linearGradient(listOf(HalloweenOrange, SpookyPurple))
            else Brush.linearGradient(listOf(CardBorder, CardBorder))
        ),
        elevation = CardDefaults.cardElevation(if (game.available) 6.dp else 1.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (game.hasBanner && game.bannerRes != null) {
                Image(
                    painter = painterResource(game.bannerRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, NightBlack.copy(alpha = 0.7f)))
                    )
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(Color(0xFF1C1C2E), Color(0xFF252535)))
                    )
                )
            }

            if (game.available) {
                // Estrella favorito (esquina superior derecha)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) stringResource(R.string.cd_remove_favorite) else stringResource(R.string.cd_add_favorite),
                        tint = if (isFavorite) HalloweenOrange else GhostWhite.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Feature emojis — esquina superior izquierda
                if (game.features.isNotEmpty()) {
                    Row(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        game.features.forEach { feature ->
                            Text(feature.emoji, fontSize = 13.sp)
                        }
                    }
                }
                // Título — parte inferior
                Text(
                    game.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GhostWhite,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            } else {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Lock, null,
                        tint = GhostWhite.copy(alpha = 0.18f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.label_coming_soon),
                        style = MaterialTheme.typography.labelLarge,
                        color = GhostWhite.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}
