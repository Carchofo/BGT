package com.rafel.bgt.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafel.bgt.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimerScreen(onAccept: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TBG – Tools Board Games",
                        style = MaterialTheme.typography.titleLarge,
                        color = GhostWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        bottomBar = {
            Surface(
                color = MidnightBlue,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HalloweenOrange,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Aceptar y Continuar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MidnightBlue
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Cabecera
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Games,
                        contentDescription = null,
                        tint = HalloweenOrange,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Bienvenido",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GhostWhite,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Compañero digital para tus juegos de mesa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostWhite.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Text(
                    "Antes de continuar, por favor lee esta información:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GhostWhite.copy(alpha = 0.65f)
                )
            }

            item {
                DisclaimerCard(
                    icon = Icons.Default.VerifiedUser,
                    title = "Aplicación independiente",
                    body = "TBG – Tools Board Games es una aplicación creada de forma independiente. " +
                            "No está afiliada, patrocinada ni respaldada por Board&Dice, AEG, " +
                            "Capstone Games, Samaruc Games, Bad Comet, Plan B Games ni Blue Orange Games."
                )
            }

            item {
                DisclaimerCard(
                    icon = Icons.Default.Info,
                    title = "Propiedad intelectual",
                    body = "Los nombres de los juegos (Tiletum, Cascadia, Maracaibo, Castle Combo, " +
                            "Coimbra, Criaturas Maravillosas) son propiedad de sus respectivas " +
                            "editoriales. Su mención es únicamente identificativa."
                )
            }

            item {
                DisclaimerCard(
                    icon = Icons.Default.Extension,
                    title = "Requiere el juego físico",
                    body = "Esta app es un complemento y no sustituye al juego de mesa original. " +
                            "Necesitas el juego físico para usarla. " +
                            "No incluye reglas completas ni contenido protegido por copyright."
                )
            }

            item {
                DisclaimerCard(
                    icon = Icons.Default.People,
                    title = "Modos solitarios fan-made",
                    body = "Los modos solitarios están basados en variantes creadas por la " +
                            "comunidad de jugadores y publicadas gratuitamente en BoardGameGeek. " +
                            "Todo el crédito corresponde a sus autores originales."
                )
            }

            item {
                Text(
                    "Al pulsar «Aceptar y Continuar» confirmas que has leído este aviso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostWhite.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DisclaimerCard(
    icon: ImageVector,
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HalloweenOrange,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = GhostWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostWhite.copy(alpha = 0.72f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
