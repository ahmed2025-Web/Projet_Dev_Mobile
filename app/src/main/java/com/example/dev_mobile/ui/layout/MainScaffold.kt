package com.example.dev_mobile.ui.layout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dev_mobile.session.UserSession
import com.example.dev_mobile.ui.common.OfflineBanner
import com.example.dev_mobile.ui.navigation.AppDestination
import com.example.dev_mobile.ui.navigation.MenuConfig

private val AccentBlue  = Color(0xFF4A7FC1)
private val TextDark    = Color(0xFF1A1A2E)
private val TextGray    = Color(0xFF8A8FA3)
private val ActiveBg    = Color(0xFFE8F0FB)
private val OverlayBg   = Color(0x66000000)
private val SidebarBg   = Color(0xFFFFFFFF)
private val HeaderBg    = Color(0xFFFFFFFF)
private val PageBg      = Color(0xFFF4F7FC)

@Composable
fun MainScaffold(
    currentDestination: AppDestination,
    festivalNom: String,
    isOnline: Boolean,                          // ← nouveau paramètre
    onDestinationChange: (AppDestination) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    var sidebarOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(PageBg)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Bannière offline (sticky en haut) ─────────────────────────────
            OfflineBanner(isOnline = isOnline)

            // ── Header de l'app ───────────────────────────────────────────────
            AppHeader(
                festivalNom = festivalNom,
                isOnline    = isOnline,
                onMenuClick = { sidebarOpen = true }
            )

            Box(modifier = Modifier.fillMaxSize()) { content() }
        }

        // Overlay sombre
        AnimatedVisibility(visible = sidebarOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayBg)
                    .clickable { sidebarOpen = false }
            )
        }

        // Sidebar
        AnimatedVisibility(
            visible = sidebarOpen,
            enter   = slideInHorizontally(initialOffsetX = { -it }),
            exit    = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            AppSidebar(
                currentDestination  = currentDestination,
                isOnline            = isOnline,
                onDestinationChange = { onDestinationChange(it); sidebarOpen = false },
                onLogout            = onLogout,
                onClose             = { sidebarOpen = false }
            )
        }
    }
}

@Composable
private fun AppHeader(
    festivalNom: String,
    isOnline: Boolean,
    onMenuClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBg)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Text("☰", fontSize = 22.sp, color = TextDark)
            }
            Text("📅", fontSize = 15.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text       = festivalNom,
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextDark,
                modifier   = Modifier.weight(1f)
            )
            // Indicateur connectivité compact
            ConnectivityDot(isOnline = isOnline)
            Spacer(Modifier.width(12.dp))
        }
        HorizontalDivider(color = Color(0xFFE8EDF5))
    }
}

@Composable
private fun ConnectivityDot(isOnline: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF388E3C) else Color(0xFFE65100))
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (isOnline) "En ligne" else "Hors ligne",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isOnline) Color(0xFF388E3C) else Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun AppSidebar(
    currentDestination: AppDestination,
    isOnline: Boolean,
    onDestinationChange: (AppDestination) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(SidebarBg)
    ) {
        // Header sidebar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎮", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("FestiJeux", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("Gestion de festivals", fontSize = 12.sp, color = TextGray)
            }
            IconButton(onClick = onClose) {
                Text("✕", fontSize = 18.sp, color = TextGray)
            }
        }

        // Statut connectivité dans la sidebar
        if (!isOnline) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFF3E0)
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📡", fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Mode hors ligne", fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100))
                        Text("Consultation uniquement", fontSize = 10.sp, color = Color(0xFF8A8FA3))
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEF2F8))
        Spacer(modifier = Modifier.height(8.dp))

        // Items menu
        Column(modifier = Modifier.weight(1f)) {
            MenuConfig.getMenuItems().forEach { dest ->
                SidebarItem(
                    destination = dest,
                    isActive    = currentDestination.route == dest.route,
                    onClick     = { onDestinationChange(dest) }
                )
            }
        }

        // Bas : Logout + Profil
        Column {
            HorizontalDivider(color = Color(0xFFEEF2F8))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🚪", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Se déconnecter",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD32F2F)
                )
            }

            HorizontalDivider(color = Color(0xFFF4F7FC))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFBFCFE))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F0FB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = (UserSession.login?.take(1) ?: "?").uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color    = AccentBlue
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(UserSession.login ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text(UserSession.getRoleLabel(), fontSize = 11.sp, color = TextGray)
                }
            }
        }
    }
}

@Composable
private fun SidebarItem(
    destination: AppDestination,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) ActiveBg else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(destination.icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text       = destination.label,
            fontSize   = 14.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isActive) AccentBlue else TextDark
        )
    }
}