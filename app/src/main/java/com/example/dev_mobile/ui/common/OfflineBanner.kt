package com.example.dev_mobile.ui.common

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Couleurs offline ──────────────────────────────────────────────────────────
private val OfflineOrange = Color(0xFFE65100)
private val OfflineAmber  = Color(0xFFFFF3E0)
private val OnlineGreen   = Color(0xFF1B5E20)
private val OnlineGreenBg = Color(0xFFE8F5E9)

/**
 * Bannière qui s'affiche en haut de l'écran quand l'app est hors ligne.
 * Affiche aussi brièvement un message "De retour en ligne" lors de la reconnexion.
 */
@Composable
fun OfflineBanner(isOnline: Boolean) {
    // On garde en mémoire le dernier état pour détecter la reconnexion
    var wasOffline by remember { mutableStateOf(false) }
    var showReconnected by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            wasOffline = true
            showReconnected = false
        } else if (wasOffline) {
            showReconnected = true
            kotlinx.coroutines.delay(3000)
            showReconnected = false
        }
    }

    Column {
        // Bannière hors ligne
        AnimatedVisibility(
            visible = !isOnline,
            enter   = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit    = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OfflineOrange)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("📡", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Hors ligne — Données en cache affichées",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        // Flash "De retour en ligne"
        AnimatedVisibility(
            visible = showReconnected,
            enter   = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit    = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OnlineGreen)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("✅", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "De retour en ligne — Synchronisation en cours",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Badge compact pour indiquer le statut offline dans les cards ou headers.
 */
@Composable
fun OfflineBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape  = RoundedCornerShape(6.dp),
        color  = OfflineAmber
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📡", fontSize = 10.sp)
            Spacer(Modifier.width(3.dp))
            Text(
                "Cache",
                fontSize = 10.sp,
                color = OfflineOrange,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Message affiché quand l'écran est hors ligne et que le cache est vide.
 */
@Composable
fun OfflineEmptyState(
    message: String = "Aucune donnée en cache",
    subtitle: String = "Connectez-vous pour charger les données"
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📡", fontSize = 56.sp)
            Text(
                "Hors ligne",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Text(message, fontSize = 14.sp, color = Color(0xFF8A8FA3))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = OfflineAmber,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = OfflineOrange,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Snackbar pour les actions bloquées en mode offline.
 */
@Composable
fun OfflineActionBlockedSnackbar(
    snackbarHostState: SnackbarHostState
) {
    SnackbarHost(snackbarHostState) { data ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = OfflineAmber,
            contentColor = OfflineOrange,
            action = {
                TextButton(onClick = { data.dismiss() }) {
                    Text("OK", color = OfflineOrange, fontWeight = FontWeight.Bold)
                }
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📡 ", fontSize = 16.sp)
                Text(data.visuals.message, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}