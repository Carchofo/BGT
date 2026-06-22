package com.rafel.bgt.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafel.bgt.ui.theme.*

private data class Credit(val game: String, val mode: String, val author: String, val bggUser: String)

private val CREDITS = listOf(
    Credit("Maracaibo", "Modo Jordán", "Alexander Pfister", "oficial — incluido en el juego"),
    Credit("Tiletum", "Modo Titus", "Dávid Turczi", "oficial — incluido en el juego"),
    Credit("Castle Combo", "Modo Anton", "ben_uez", "BGG: ben_uez"),
    Credit("Criaturas Maravillosas", "Modo Tingent", "Dávid Turczi", "oficial — incluido en el juego"),
    Credit("Coimbra", "Bot de interferencia", "Dave Smith", "BGG: skybowl"),
    Credit("Cascadia", "Calculadora de puntuación", "Comunidad BGG", "Reglas oficiales"),
    Credit("Spooktacular", "Killtron-3000", "Samaruc Games", "oficial — incluido en el juego"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de BGT", color = GhostWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = GhostWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        containerColor = MidnightBlue
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(MidnightBlue, Color(0xFF0D0D1A)))),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        Icons.Default.Games, null,
                        tint = HalloweenOrange,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "BGT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GhostWhite,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Board Game Tools",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostWhite.copy(alpha = 0.55f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Asistente gratuito para jugar solo tus juegos de mesa favoritos.\nSin anuncios. Sin suscripciones. Siempre gratis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            item {
                DonationCard(onKofi = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/bgtapp"))
                    )
                })
            }

            item {
                SectionHeader("🎲 Créditos — Modos Solitarios")
            }

            items(CREDITS.size) { i ->
                val c = CREDITS[i]
                CreditCard(c)
            }

            item {
                SectionHeader("🔗 Links")
            }

            item {
                LinkRow(
                    icon = Icons.Default.Code,
                    label = "Código fuente",
                    sub = "github.com/Carchofo/BGT",
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Carchofo/BGT"))
                        )
                    }
                )
            }

            item {
                LinkRow(
                    icon = Icons.Default.Forum,
                    label = "BoardGameGeek",
                    sub = "Comunidad de juegos de mesa",
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://boardgamegeek.com"))
                        )
                    }
                )
            }

            item {
                InfoRow(
                    icon = Icons.Default.PersonOutline,
                    label = "Desarrollado por",
                    value = "Rafel · jugador solitario, padre primerizo"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DonationCard(onKofi: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, HalloweenOrange.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("☕ ¿Te es útil BGT?", style = MaterialTheme.typography.titleMedium, color = GhostWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Esto lo construyo en mi tiempo libre, con un bebé en casa.\nSi te ayuda a disfrutar más tus juegos, un café ayuda mucho.",
                style = MaterialTheme.typography.bodySmall,
                color = GhostWhite.copy(alpha = 0.70f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onKofi,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HalloweenOrange)
            ) {
                Icon(Icons.Default.Favorite, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Invitar a un café en Ko-fi", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Completamente opcional. BGT es y será siempre gratuita.",
                style = MaterialTheme.typography.labelSmall,
                color = GhostWhite.copy(alpha = 0.35f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = HalloweenOrange,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun CreditCard(credit: Credit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(credit.game, style = MaterialTheme.typography.titleSmall, color = GhostWhite, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(credit.mode, style = MaterialTheme.typography.labelSmall, color = HalloweenOrange.copy(alpha = 0.8f))
            }
            Spacer(Modifier.height(2.dp))
            Text("Autor: ${credit.author}", style = MaterialTheme.typography.bodySmall, color = GhostWhite.copy(alpha = 0.65f))
            Text(credit.bggUser, style = MaterialTheme.typography.labelSmall, color = GhostWhite.copy(alpha = 0.38f))
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, sub: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = HalloweenOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = GhostWhite, fontWeight = FontWeight.SemiBold)
                Text(sub, style = MaterialTheme.typography.labelSmall, color = GhostWhite.copy(alpha = 0.45f))
            }
            Icon(Icons.Default.OpenInNew, null, tint = GhostWhite.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = GhostWhite.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = GhostWhite.copy(alpha = 0.4f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = GhostWhite.copy(alpha = 0.70f))
        }
    }
}
