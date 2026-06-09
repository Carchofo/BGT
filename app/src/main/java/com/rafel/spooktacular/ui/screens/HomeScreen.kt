package com.rafel.spooktacular.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafel.spooktacular.R
import com.rafel.spooktacular.ui.theme.*
import com.rafel.spooktacular.ui.util.SoundSettings

enum class ViewMode { LIST, GRID }

data class GameItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val hasBanner: Boolean = false,
    val bannerRes: Int? = null,
    val available: Boolean = false,
    val route: String = "",
    val tag: String? = null
)

private val GAMES = listOf(
    GameItem(
        id = "spooktacular", title = "Spooktacular",
        subtitle = "Terror en el Silver Cinema • 1-5 jugadores",
        hasBanner = true, available = true, route = "solo_mode", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_spooktacular_new
    ),
    GameItem(
        id = "cm", title = "Criaturas Maravillosas",
        subtitle = "Exploración de isla • Modo solitario vs Tingent",
        hasBanner = true, available = true, route = "cm_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_cm_new
    ),
    GameItem(
        id = "tiletum", title = "Tiletum",
        subtitle = "Mercaderes del Renacimiento • Modo solitario vs Titus",
        hasBanner = true, available = true, route = "tiletum_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_tiletum_img
    ),
    GameItem(
        id = "maracaibo", title = "Piratas de Maracaibo",
        subtitle = "El Caribe, s. XVII • Modo solitario vs Jordán",
        hasBanner = true, available = true, route = "maracaibo_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_maracaibo
    ),
    GameItem(
        id = "castle_combo", title = "Castle Combo",
        subtitle = "Fiesta de los Combos • Modo solitario vs Anton",
        hasBanner = true, available = true, route = "castle_combo_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_castle_combo
    ),
    GameItem(
        id = "cascadia", title = "Cascadia",
        subtitle = "Noroeste del Pacífico • Calculadora de puntuación",
        hasBanner = true, available = true, route = "cascadia_score", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_cascadia
    ),
    GameItem(
        id = "coimbra", title = "Coimbra",
        subtitle = "Portugal medieval • Modo solitario Bot Interferencia",
        hasBanner = true, available = true, route = "coimbra_solo", tag = "DISPONIBLE",
        bannerRes = R.drawable.banner_coimbra
    ),
    GameItem(id = "g4", title = "Próximamente", subtitle = ""),
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
    onNavigate: (String) -> Unit = {}
) {
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current

    // Filter logic
    val filteredGames = remember(searchQuery, showFavoritesOnly, favoriteIds) {
        var result = GAMES_SORTED.filter { it.available }
        if (showFavoritesOnly) result = result.filter { it.id in favoriteIds }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            result = result.filter { game ->
                game.title.contains(q, ignoreCase = true) ||
                game.subtitle.contains(q, ignoreCase = true)
            }
        }
        // Show "Próximamente" slots only when not filtering
        if (searchQuery.isBlank() && !showFavoritesOnly) {
            result + GAMES_SORTED.filter { !it.available }
        } else {
            result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎮  Juegos",
                        style = MaterialTheme.typography.titleLarge,
                        color = GhostWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Toggle favoritos
                    IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (showFavoritesOnly) "Ver todos" else "Ver favoritos",
                            tint = if (showFavoritesOnly) HalloweenOrange else GhostWhite.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Toggle sonido
                    val context = LocalContext.current
                    val isMuted by SoundSettings.isMuted
                    IconButton(onClick = { SoundSettings.toggle(context) }) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Activar sonido" else "Silenciar",
                            tint = if (isMuted) GhostWhite.copy(alpha = 0.35f) else GhostWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    ViewToggle(current = viewMode, onToggle = { viewMode = it })
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        containerColor = MidnightBlue
    ) { padding ->
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
                        "Buscar juego...",
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
                                contentDescription = "Limpiar",
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
                    ViewMode.LIST -> GameListView(filteredGames, favoriteIds, { id -> favoriteIds = if (id in favoriteIds) favoriteIds - id else favoriteIds + id }, onNavigate)
                    ViewMode.GRID -> GameGridView(filteredGames, favoriteIds, { id -> favoriteIds = if (id in favoriteIds) favoriteIds - id else favoriteIds + id }, onNavigate)
                }
            }
        }
    }
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
            text = if (isFavoritesFilter) "No tienes favoritos aún" else "Sin resultados",
            style = MaterialTheme.typography.titleMedium,
            color = GhostWhite.copy(alpha = 0.35f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isFavoritesFilter)
                "Pulsa la ★ en cualquier juego para guardarlo aquí"
            else
                "Prueba con otro término de búsqueda",
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
                        game.tag?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                color = HalloweenOrange,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(Modifier.height(3.dp))
                        }
                        Text(
                            game.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = GhostWhite,
                            fontWeight = FontWeight.Black
                        )
                        if (game.subtitle.isNotEmpty()) {
                            Spacer(Modifier.height(3.dp))
                            Text(
                                game.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostWhite.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.Lock, null,
                            tint = GhostWhite.copy(alpha = 0.2f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Próximamente",
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
                            contentDescription = if (isFavorite) "Quitar favorito" else "Añadir favorito",
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
                        contentDescription = if (isFavorite) "Quitar favorito" else "Añadir favorito",
                        tint = if (isFavorite) HalloweenOrange else GhostWhite.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    game.tag?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 8.sp),
                            color = HalloweenOrange,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(
                        game.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = GhostWhite,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                        "Próximamente",
                        style = MaterialTheme.typography.labelLarge,
                        color = GhostWhite.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}
