package com.example.dev_mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.network.FestivalDashboardDto
import com.example.dev_mobile.session.UserSession
import com.example.dev_mobile.ui.common.OfflineBadge
import com.example.dev_mobile.ui.common.OfflineEmptyState

private val AccentBlue   = Color(0xFF4A7FC1)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8FA3)
private val PageBg       = Color(0xFFF4F7FC)
private val CardBg       = Color(0xFFFFFFFF)
private val DividerCol   = Color(0xFFF0F3F8)
private val GoldColor    = Color(0xFFF59E0B)
private val SuccessGreen = Color(0xFF388E3C)
private val ErrorRed     = Color(0xFFD32F2F)

@Composable
fun DashboardScreen(
    festivalNom: String,
    isOnline: Boolean = true,
    onNavigateToReservations: (() -> Unit)? = null,
    onNavigateToFestivals: (() -> Unit)? = null,
    onNavigateToReservants: (() -> Unit)? = null,
    vm: DashboardViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    // Recharger automatiquement dès que la connexion revient
    LaunchedEffect(isOnline) {
        if (isOnline) vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .verticalScroll(rememberScrollState())
    ) {
        WelcomeBanner(festivalNom = festivalNom)

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else if (state.festival == null) {
            if (!isOnline) {
                OfflineEmptyState(
                    message = "Aucun festival en cache",
                    subtitle = "Connectez-vous pour charger les données"
                )
            } else {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Aucun festival courant", fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(Modifier.height(6.dp))
                        Text("Créez un festival pour commencer", fontSize = 13.sp, color = TextGray)
                        Spacer(Modifier.height(16.dp))
                        if (onNavigateToFestivals != null && UserSession.canAdmin()) {
                            Button(
                                onClick = onNavigateToFestivals,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) { Text("Gérer les festivals", color = Color.White) }
                        }
                    }
                }
            }
        } else {
            val festival = state.festival!!
            val stats    = vm.getReservationStats()

            FestivalCourantCard(
                festival    = festival,
                isFromCache = state.isFromCache,
                onClick     = onNavigateToFestivals
            )

            Spacer(Modifier.height(4.dp))
            SectionTitle("Vue d'ensemble")
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard("📋", "${stats["total"] ?: 0}",   "Réservations", AccentBlue,    Modifier.weight(1f), onNavigateToReservations)
                KpiCard("✅", "${stats["reserve"] ?: 0}", "Confirmées",   SuccessGreen,  Modifier.weight(1f), onNavigateToReservations)
                KpiCard("🪑", "${vm.getTotalTablesReservees()}", "Tables", Color(0xFF7B1FA2), Modifier.weight(1f), null)
                KpiCard("💰", "%.0f€".format(vm.getMontantTotal()), "Montant", GoldColor, Modifier.weight(1f), null)
            }

            Spacer(Modifier.height(8.dp))
            SectionTitle("Workflow de contact")
            WorkflowProgressCard(stats = stats, onClick = onNavigateToReservations)

            if ((stats["presents"] ?: 0) > 0 || (stats["absents"] ?: 0) > 0) {
                Spacer(Modifier.height(4.dp))
                SectionTitle("Présence")
                PresenceCard(stats = stats)
            }

            if (UserSession.canManage()) {
                Spacer(Modifier.height(4.dp))
                SectionTitle("Stocks physiques")
                StocksCard(festival = festival)
            }

            Spacer(Modifier.height(4.dp))
            SectionTitle("Accès rapides")
            QuickActionsRow(
                onReservations = onNavigateToReservations,
                onFestivals    = onNavigateToFestivals,
                onReservants   = onNavigateToReservants
            )

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { vm.load() }) {
                    Text("🔄 Rafraîchir", fontSize = 12.sp, color = TextGray)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WelcomeBanner(festivalNom: String) {
    val roleColor = Color(UserSession.getRoleColor())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF1A1A2E), Color(0xFF2D3561))))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(roleColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text((UserSession.login?.take(1) ?: "?").uppercase(), fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = roleColor)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Bonjour, ${UserSession.login ?: ""}", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text(UserSession.getRoleLabel(), fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }
            Surface(shape = RoundedCornerShape(12.dp), color = roleColor.copy(alpha = 0.2f)) {
                Text("🎮", fontSize = 20.sp, modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
private fun FestivalCourantCard(
    festival: FestivalDashboardDto,
    isFromCache: Boolean,
    onClick: (() -> Unit)?
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).clip(CircleShape).background(GoldColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text("⭐", fontSize = 22.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(festival.nom, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = GoldColor.copy(alpha = 0.15f)) {
                            Text("COURANT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    val dateRange = buildString {
                        festival.date_debut?.take(10)?.let { append("Du $it") }
                        festival.date_fin?.take(10)?.let { append(" au $it") }
                    }
                    if (dateRange.isNotBlank()) {
                        Text(dateRange, fontSize = 12.sp, color = TextGray)
                    } else {
                        Text("Dates non définies", fontSize = 12.sp, color = TextGray)
                    }
                    // Badge cache
                    if (isFromCache) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OfflineBadge()
                            Spacer(Modifier.width(6.dp))
                            Text("Données depuis le cache local", fontSize = 10.sp, color = TextGray)
                        }
                    }
                }
                if (onClick != null) Text("›", fontSize = 22.sp, color = TextGray)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FestivalMetaStat("🗺️", "${festival.nb_zones_tarifaires}", "Zones tar.", Modifier.weight(1f))
                FestivalMetaStat("🗺️", "${festival.nb_zones_plan}",       "Zones plan", Modifier.weight(1f))
                FestivalMetaStat("🪑", "${festival.espace_tables_total}", "Espace",     Modifier.weight(1f))
                FestivalMetaStat("💰", "%.0f€".format(festival.montant_total_factures), "Facturé", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FestivalMetaStat(icon: String, value: String, label: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = PageBg) {
        Column(Modifier.padding(horizontal = 6.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 16.sp)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(label, fontSize = 9.sp, color = TextGray, maxLines = 1)
        }
    }
}

@Composable
private fun KpiCard(icon: String, value: String, label: String, color: Color, modifier: Modifier = Modifier, onClick: (() -> Unit)?) {
    Card(
        modifier  = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 18.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(label, fontSize = 10.sp, color = TextGray, maxLines = 1)
        }
    }
}

@Composable
private fun WorkflowProgressCard(stats: Map<String, Int>, onClick: (() -> Unit)?) {
    val total = stats["total"] ?: 0
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (total == 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📋", fontSize = 24.sp); Spacer(Modifier.width(12.dp))
                    Text("Aucune réservation pour ce festival", fontSize = 14.sp, color = TextGray)
                }
            } else {
                WorkflowStep("📞", "Contacté",           stats["contacte"] ?: 0,             total, Color(0xFF1976D2))
                WorkflowStep("💬", "En discussion",      stats["en_discussion"] ?: 0,        total, Color(0xFFF57F17))
                WorkflowStep("✅", "Réservé",            stats["reserve"] ?: 0,              total, SuccessGreen)
                WorkflowStep("📋", "Liste demandée",     stats["liste_jeux_demandee"] ?: 0,  total, Color(0xFF7B1FA2))
                WorkflowStep("📄", "Liste obtenue",      stats["liste_jeux_obtenue"] ?: 0,   total, Color(0xFF3949AB))
                WorkflowStep("🎮", "Jeux reçus",         stats["jeux_recus"] ?: 0,           total, Color(0xFF00838F))
            }
        }
    }
}

@Composable
private fun WorkflowStep(icon: String, label: String, count: Int, total: Int, color: Color) {
    if (count == 0) return
    val ratio = if (total > 0) count.toFloat() / total else 0f
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 14.sp, modifier = Modifier.width(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = TextDark, modifier = Modifier.width(130.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.15f))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(ratio).clip(RoundedCornerShape(4.dp)).background(color))
        }
        Spacer(Modifier.width(8.dp))
        Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.width(24.dp))
    }
}

@Composable
private fun PresenceCard(stats: Map<String, Int>) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PresencePill("✅", "Présents",  stats["presents"] ?: 0, Color(0xFFE8F5E9), SuccessGreen, Modifier.weight(1f))
            PresencePill("❌", "Absents",   stats["absents"] ?: 0,  Color(0xFFFFEBEE), ErrorRed,     Modifier.weight(1f))
        }
    }
}

@Composable
private fun PresencePill(icon: String, label: String, count: Int, bg: Color, fg: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = bg) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 18.sp); Spacer(Modifier.width(10.dp))
            Column {
                Text("$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = fg)
                Text(label, fontSize = 11.sp, color = fg.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun StocksCard(festival: FestivalDashboardDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Tables physiques disponibles", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextGray)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StockItem("📦", "Standard", festival.stock_tables_standard, AccentBlue,          Modifier.weight(1f))
                StockItem("📦", "Grandes",  festival.stock_tables_grandes,  Color(0xFF7B1FA2),   Modifier.weight(1f))
                StockItem("📦", "Mairie",   festival.stock_tables_mairie,   Color(0xFF00838F),   Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(10.dp))
            Text("Chaises", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StockItem("🪑", "Standard", festival.stock_chaises_standard, Color(0xFF1976D2), Modifier.weight(1f))
                StockItem("🪑", "Mairie",   festival.stock_chaises_mairie,   SuccessGreen,      Modifier.weight(1f))
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF8E1)) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚡", fontSize = 16.sp)
                        Text("%.2f €".format(festival.prix_prise_electrique), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldColor)
                        Text("/ prise", fontSize = 9.sp, color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun StockItem(icon: String, label: String, value: Int, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.1f)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 16.sp)
            Text("$value", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, color = TextGray)
        }
    }
}

@Composable
private fun QuickActionsRow(onReservations: (() -> Unit)?, onFestivals: (() -> Unit)?, onReservants: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (onReservations != null)
            QuickActionCard("📋", "Réservations", AccentBlue,          Modifier.weight(1f), onReservations)
        if (onFestivals != null && UserSession.canAdmin())
            QuickActionCard("📅", "Festivals",    GoldColor,            Modifier.weight(1f), onFestivals)
        if (onReservants != null)
            QuickActionCard("👥", "Réservants",   Color(0xFF7B1FA2),   Modifier.weight(1f), onReservants)
    }
}

@Composable
private fun QuickActionCard(icon: String, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 24.sp); Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextGray,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
}