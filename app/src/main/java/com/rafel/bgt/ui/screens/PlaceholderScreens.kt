package com.rafel.bgt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafel.bgt.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonstersScreen(onBack: () -> Unit = {}) {
    BGTScaffold("Guía de Monstruos", onBack) {
        ComingSoonContent("📖", "Guía de Monstruos",
            "Descripción detallada de los 20 monstruos: habilidades, estrategias y dificultad.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreBoardScreen(onBack: () -> Unit = {}) {
    BGTScaffold("Marcador", onBack) {
        ComingSoonContent("🏆", "Marcador",
            "Historial de partidas, récords personales y estadísticas.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BGTScaffold(
    title: String, onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge, color = GhostWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = HalloweenOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        containerColor = MidnightBlue
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(MidnightBlue, CardBackground)))
                .padding(padding),
            content = content
        )
    }
}

@Composable
private fun ComingSoonContent(emoji: String, title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 80.sp)
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium,
            color = HalloweenOrange, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(description, style = MaterialTheme.typography.bodyLarge,
            color = GhostWhite.copy(alpha = 0.7f), textAlign = TextAlign.Center, lineHeight = 24.sp)
        Spacer(Modifier.height(32.dp))
        Card(colors = CardDefaults.cardColors(containerColor = HalloweenOrangeDark.copy(alpha = 0.2f))) {
            Text("🔧  En desarrollo", style = MaterialTheme.typography.labelLarge,
                color = PumpkinYellow, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
        }
    }
}
