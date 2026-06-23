package com.rafel.bgt

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafel.bgt.R
import kotlinx.coroutines.launch
import com.rafel.bgt.ui.screens.CascadiaScoreScreen
import com.rafel.bgt.ui.screens.CastleComboSoloScreen
import com.rafel.bgt.ui.screens.CoimbraSoloScreen
import com.rafel.bgt.ui.screens.FridaySoloScreen
import com.rafel.bgt.ui.screens.CMSoloModeScreen
import com.rafel.bgt.ui.screens.TiletumSoloScreen
import com.rafel.bgt.ui.screens.MaracaiboSoloScreen
import com.rafel.bgt.ui.screens.AboutScreen
import com.rafel.bgt.ui.screens.HomeScreen
import com.rafel.bgt.ui.screens.MonstersScreen
import com.rafel.bgt.ui.screens.ScoreBoardScreen
import com.rafel.bgt.ui.screens.SoloModeScreen
import com.rafel.bgt.ui.theme.GhostWhite
import com.rafel.bgt.ui.theme.HalloweenOrange
import com.rafel.bgt.ui.theme.BGTTheme
import com.rafel.bgt.ui.util.SoundSettings

private const val PREFS_NAME = "tbg_prefs"
private const val KEY_DISCLAIMER = "disclaimer_accepted"

class MainActivity : ComponentActivity() {

    private var pendingDownloadId = -1L
    private var pendingVersion = ""

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == pendingDownloadId && pendingVersion.isNotEmpty()) {
                ApkInstaller.install(context, pendingVersion)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SoundSettings.init(this)
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(downloadReceiver, filter)
        }

        setContent {
            BGTTheme {
                var showDisclaimer by remember {
                    mutableStateOf(!prefs.getBoolean(KEY_DISCLAIMER, false))
                }
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        val currentVersion = packageManager
                            .getPackageInfo(packageName, 0).versionName ?: "1.0"
                        updateInfo = UpdateChecker.check(currentVersion)
                    }
                }

                BGTApp()

                if (showDisclaimer) {
                    DisclaimerDialog(
                        onAccept = {
                            prefs.edit().putBoolean(KEY_DISCLAIMER, true).apply()
                            showDisclaimer = false
                        }
                    )
                }

                updateInfo?.let { info ->
                    UpdateDialog(
                        version = info.version,
                        releaseNotes = info.releaseNotes,
                        onUpdate = {
                            pendingVersion = info.version
                            pendingDownloadId = ApkInstaller.download(
                                this@MainActivity, info.downloadUrl, info.version
                            )
                            updateInfo = null
                        },
                        onDismiss = { updateInfo = null }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisclaimerDialog(onAccept: () -> Unit) {
    val sections = listOf(
        stringResource(R.string.disclaimer_section1_title) to stringResource(R.string.disclaimer_section1_body),
        stringResource(R.string.disclaimer_section2_title) to stringResource(R.string.disclaimer_section2_body),
        stringResource(R.string.disclaimer_section3_title) to stringResource(R.string.disclaimer_section3_body),
        stringResource(R.string.disclaimer_section4_title) to stringResource(R.string.disclaimer_section4_body),
    )
    AlertDialog(
        onDismissRequest = { /* obligatorio aceptar */ },
        title = {
            Text(
                stringResource(R.string.disclaimer_title),
                fontWeight = FontWeight.Bold,
                color = GhostWhite
            )
        },
        text = {
            LazyColumn {
                items(sections) { (title, body) ->
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        color = HalloweenOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        body,
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider(color = GhostWhite.copy(alpha = 0.1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = HalloweenOrange)
            ) {
                Text(stringResource(R.string.disclaimer_accept_btn), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = com.rafel.bgt.ui.theme.CardBackground
    )
}

@Composable
private fun UpdateDialog(
    version: String,
    releaseNotes: String,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🎲 BGT $version disponible", fontWeight = FontWeight.Bold, color = GhostWhite)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                if (releaseNotes.isNotBlank()) {
                    item {
                        Text(
                            "Novedades:",
                            style = MaterialTheme.typography.labelMedium,
                            color = HalloweenOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            releaseNotes.take(600),
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostWhite.copy(alpha = 0.80f)
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
                item {
                    Text(
                        "¿Descargar e instalar ahora?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostWhite.copy(alpha = 0.70f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpdate,
                colors = ButtonDefaults.buttonColors(containerColor = HalloweenOrange)
            ) {
                Text("Actualizar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ahora no", color = GhostWhite.copy(alpha = 0.55f))
            }
        },
        containerColor = CardBackground
    )
}

@Composable
fun BGTApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home")              { HomeScreen(onNavigate = { navController.navigate(it) }, onAbout = { navController.navigate("about") }) }
        composable("solo_mode")         { SoloModeScreen(onBack = { navController.popBackStack() }) }
        composable("monsters")          { MonstersScreen(onBack = { navController.popBackStack() }) }
        composable("score_board")       { ScoreBoardScreen(onBack = { navController.popBackStack() }) }
        composable("cm_solo")           { CMSoloModeScreen(onBack = { navController.popBackStack() }) }
        composable("tiletum_solo")      { TiletumSoloScreen(onBack = { navController.popBackStack() }) }
        composable("maracaibo_solo")    { MaracaiboSoloScreen(onBack = { navController.popBackStack() }) }
        composable("cascadia_score")    { CascadiaScoreScreen(onBack = { navController.popBackStack() }) }
        composable("castle_combo_solo") { CastleComboSoloScreen(onBack = { navController.popBackStack() }) }
        composable("coimbra_solo")      { CoimbraSoloScreen(onBack = { navController.popBackStack() }) }
        composable("friday_solo")       { FridaySoloScreen(onBack = { navController.popBackStack() }) }
        composable("about")             { AboutScreen(onBack = { navController.popBackStack() }) }
    }
}
