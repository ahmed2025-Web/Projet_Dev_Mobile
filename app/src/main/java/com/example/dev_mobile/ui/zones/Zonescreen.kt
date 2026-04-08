// app/src/main/java/com/example/dev_mobile/ui/zones/ZoneScreen.kt
package com.example.dev_mobile.ui.zones

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.network.ZonePlanDetailDto
import com.example.dev_mobile.network.ZoneTarifaireDetailDto
import com.example.dev_mobile.session.UserSession

// ── Palette ───────────────────────────────────────────────────────────────────
private val AccentBlue   = Color(0xFF4A7FC1)
private val AccentGreen  = Color(0xFF388E3C)
private val AccentOrange = Color(0xFFE65100)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8FA3)
private val PageBg       = Color(0xFFF4F7FC)
private val CardBg       = Color(0xFFFFFFFF)
private val DividerCol   = Color(0xFFF0F3F8)
private val ErrorRed     = Color(0xFFD32F2F)

private val canManage get() = UserSession.canManage()
private val canAdmin  get() = UserSession.canAdmin()

@Composable
fun ZonesScreen(vm: ZoneViewModel = viewModel()) {
    val state   by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.errorMessage?.let   { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                val isErr = data.visuals.message.startsWith("Erreur") ||
                        data.visuals.message.startsWith("Serveur") ||
                        data.visuals.message.startsWith("Impossible")
                Card(
                    modifier  = Modifier.padding(16.dp).fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = if (isErr) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isErr) "❌" else "✅", fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            data.visuals.message,
                            color      = if (isErr) ErrorRed else AccentGreen,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        containerColor = PageBg
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // ── En-tête ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Zones",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextDark
                        )
                        if (state.festivalNom.isNotEmpty()) {
                            Text(
                                state.festivalNom,
                                fontSize = 12.sp,
                                color    = AccentBlue
                            )
                        }
                    }
                    if (canManage) {
                        Button(
                            onClick = {
                                if (state.activeTab == ZoneTab.TARIFAIRES)
                                    vm.openCreateTarifaireDialog()
                                else
                                    vm.openCreatePlanDialog()
                            },
                            enabled = state.festivalId > 0,
                            shape  = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                        ) {
                            Text(
                                if (state.activeTab == ZoneTab.TARIFAIRES)
                                    "+ Zone tarifaire"
                                else
                                    "+ Zone du plan",
                                fontSize = 13.sp,
                                color    = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Tabs
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ZoneTabButton(
                        label    = "Zones tarifaires (${state.zonesTarifaires.size})",
                        selected = state.activeTab == ZoneTab.TARIFAIRES,
                        onClick  = { vm.setTab(ZoneTab.TARIFAIRES) },
                        modifier = Modifier.weight(1f)
                    )
                    ZoneTabButton(
                        label    = "Zones du plan (${state.zonesPlan.size})",
                        selected = state.activeTab == ZoneTab.PLAN,
                        onClick  = { vm.setTab(ZoneTab.PLAN) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = DividerCol)

            // ── Contenu ───────────────────────────────────────────────────────
            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }

                state.festivalId <= 0 -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Aucun festival courant",
                            color      = TextGray,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { vm.refresh() }) {
                            Text("Rafraîchir", color = AccentBlue)
                        }
                    }
                }

                state.activeTab == ZoneTab.TARIFAIRES ->
                    ZonesTarifairesTab(
                        zones        = state.zonesTarifaires,
                        festivalTotal = state.festivalEspaceTables,
                        canManage    = canManage,
                        canAdmin     = canAdmin,
                        onEdit       = { vm.openEditTarifaireDialog(it) },
                        onDelete     = { vm.deleteZoneTarifaire(it.id) },
                        onRefresh    = { vm.refresh() }
                    )

                else ->
                    ZonesPlanTab(
                        zones     = state.zonesPlan,
                        canManage = canManage,
                        canAdmin  = canAdmin,
                        onEdit    = { vm.openEditPlanDialog(it) },
                        onDelete  = { vm.deleteZonePlan(it.id) },
                        onRefresh = { vm.refresh() }
                    )
            }
        }
    }

    // ── Dialogues Zones Tarifaires ────────────────────────────────────────────

    if (state.showCreateTarifaireDialog) {
        ZoneTarifaireFormDialog(
            title     = "Nouvelle zone tarifaire",
            initial   = null,
            isLoading = state.isSubmittingTarifaire,
            onDismiss = { vm.closeCreateTarifaireDialog() },
            onConfirm = { nom, nb, prix, m2 ->
                vm.createZoneTarifaire(nom, nb, prix, m2)
            }
        )
    }

    if (state.showEditTarifaireDialog && state.tarifaireToEdit != null) {
        ZoneTarifaireFormDialog(
            title     = "Modifier la zone tarifaire",
            initial   = state.tarifaireToEdit,
            isLoading = state.isSubmittingTarifaire,
            onDismiss = { vm.closeEditTarifaireDialog() },
            onConfirm = { nom, nb, prix, m2 ->
                vm.updateZoneTarifaire(state.tarifaireToEdit!!.id, nom, nb, prix, m2)
            }
        )
    }

    // ── Dialogues Zones du Plan ───────────────────────────────────────────────

    if (state.showCreatePlanDialog) {
        ZonePlanFormDialog(
            title     = "Nouvelle zone du plan",
            initial   = null,
            isLoading = state.isSubmittingPlan,
            onDismiss = { vm.closeCreatePlanDialog() },
            onConfirm = { nom, nb -> vm.createZonePlan(nom, nb) }
        )
    }

    if (state.showEditPlanDialog && state.planToEdit != null) {
        ZonePlanFormDialog(
            title     = "Modifier la zone du plan",
            initial   = state.planToEdit,
            isLoading = state.isSubmittingPlan,
            onDismiss = { vm.closeEditPlanDialog() },
            onConfirm = { nom, nb -> vm.updateZonePlan(state.planToEdit!!.id, nom, nb) }
        )
    }
}

// ── Tab Zones Tarifaires ──────────────────────────────────────────────────────

@Composable
private fun ZonesTarifairesTab(
    zones: List<ZoneTarifaireDetailDto>,
    festivalTotal: Int,
    canManage: Boolean,
    canAdmin: Boolean,
    onEdit: (ZoneTarifaireDetailDto) -> Unit,
    onDelete: (ZoneTarifaireDetailDto) -> Unit,
    onRefresh: () -> Unit
) {
    if (zones.isEmpty()) {
        EmptyZoneState(
            icon      = "💰",
            message   = "Aucune zone tarifaire",
            subtitle  = "Créez des zones tarifaires pour ce festival",
            onRefresh = onRefresh
        )
        return
    }

    // Résumé en haut
    val totalZones    = zones.sumOf { it.nombre_tables_total }
    val totalReserv   = zones.sumOf { it.tables_reservees }
    val totalDispo    = zones.sumOf { it.tables_disponibles }

    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ZoneSummaryCard(
                items = listOf(
                    Triple("🗺️", "$totalZones", "Tables totales"),
                    Triple("📋", "$totalReserv", "Réservées"),
                    Triple("✅", "$totalDispo", "Disponibles")
                )
            )
        }

        items(zones, key = { it.id }) { zone ->
            ZoneTarifaireCard(
                zone      = zone,
                canManage = canManage,
                canAdmin  = canAdmin,
                onEdit    = { onEdit(zone) },
                onDelete  = { onDelete(zone) }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ZoneTarifaireCard(
    zone: ZoneTarifaireDetailDto,
    canManage: Boolean,
    canAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDlg by remember { mutableStateOf(false) }

    val occupationRatio = if (zone.nombre_tables_total > 0)
        zone.tables_reservees.toFloat() / zone.nombre_tables_total else 0f

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💰", fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        zone.nom,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextDark
                    )
                    Text(
                        "${zone.nombre_tables_total} tables · %.2f €/table".format(zone.prix_table),
                        fontSize = 12.sp,
                        color    = TextGray
                    )
                }
                if (canManage) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Text("✏️", fontSize = 16.sp)
                    }
                }
                if (canAdmin) {
                    IconButton(
                        onClick  = { showDeleteDlg = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("🗑", fontSize = 16.sp, color = ErrorRed)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(10.dp))

            // Barre d'occupation
            Text("Occupation", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress        = { occupationRatio },
                modifier        = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color           = if (occupationRatio > 0.8f) ErrorRed else AccentBlue,
                trackColor      = Color(0xFFE8EEF7)
            )
            Spacer(Modifier.height(6.dp))

            // Stats
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ZoneStatChip("📋", "${zone.tables_reservees}", "Réservées", Modifier.weight(1f))
                ZoneStatChip("✅", "${zone.tables_disponibles}", "Disponibles", Modifier.weight(1f))
                ZoneStatChip("💶", "%.2f €/m²".format(zone.prix_m2), "Prix m²", Modifier.weight(1.5f))
            }
        }
    }

    if (showDeleteDlg) {
        ConfirmDeleteZoneDialog(
            nom       = zone.nom,
            hasUsage  = zone.tables_reservees > 0,
            usageText = "Cette zone a ${zone.tables_reservees} table(s) réservée(s).",
            onConfirm = { onDelete(); showDeleteDlg = false },
            onDismiss = { showDeleteDlg = false }
        )
    }
}

// ── Tab Zones du Plan ─────────────────────────────────────────────────────────

@Composable
private fun ZonesPlanTab(
    zones: List<ZonePlanDetailDto>,
    canManage: Boolean,
    canAdmin: Boolean,
    onEdit: (ZonePlanDetailDto) -> Unit,
    onDelete: (ZonePlanDetailDto) -> Unit,
    onRefresh: () -> Unit
) {
    if (zones.isEmpty()) {
        EmptyZoneState(
            icon      = "🗺️",
            message   = "Aucune zone du plan",
            subtitle  = "Créez des zones du plan pour placer les jeux",
            onRefresh = onRefresh
        )
        return
    }

    val totalTables  = zones.sumOf { it.nombre_tables_total }
    val totalUtilis  = zones.sumOf { it.tables_utilisees ?: 0 }
    val totalJeux    = zones.sumOf { it.nb_jeux_places }

    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ZoneSummaryCard(
                items = listOf(
                    Triple("🗺️", "$totalTables", "Tables totales"),
                    Triple("🎮", "$totalJeux", "Jeux placés"),
                    Triple("📦", "$totalUtilis", "Tables utilisées")
                )
            )
        }

        items(zones, key = { it.id }) { zone ->
            ZonePlanCard(
                zone      = zone,
                canManage = canManage,
                canAdmin  = canAdmin,
                onEdit    = { onEdit(zone) },
                onDelete  = { onDelete(zone) }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ZonePlanCard(
    zone: ZonePlanDetailDto,
    canManage: Boolean,
    canAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDlg by remember { mutableStateOf(false) }

    val utilisees      = zone.tables_utilisees ?: 0
    val occupationRatio = if (zone.nombre_tables_total > 0)
        utilisees.toFloat() / zone.nombre_tables_total else 0f

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📍", fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        zone.nom,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextDark
                    )
                    Text(
                        "${zone.nombre_tables_total} tables · ${zone.nb_jeux_places} jeu(x) placé(s)",
                        fontSize = 12.sp,
                        color    = TextGray
                    )
                }
                if (canManage) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Text("✏️", fontSize = 16.sp)
                    }
                }
                if (canAdmin) {
                    IconButton(
                        onClick  = { showDeleteDlg = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("🗑", fontSize = 16.sp, color = ErrorRed)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(10.dp))

            // Barre d'occupation
            Text("Occupation", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress   = { occupationRatio },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color      = if (occupationRatio > 0.8f) ErrorRed else AccentGreen,
                trackColor = Color(0xFFE8F5E9)
            )
            Spacer(Modifier.height(6.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ZoneStatChip("📦", "$utilisees", "Utilisées", Modifier.weight(1f))
                ZoneStatChip(
                    "✅",
                    "${(zone.tables_disponibles ?: (zone.nombre_tables_total - utilisees))}",
                    "Libres",
                    Modifier.weight(1f)
                )
                ZoneStatChip("🎮", "${zone.nb_jeux_places}", "Jeux", Modifier.weight(1f))
            }
        }
    }

    if (showDeleteDlg) {
        ConfirmDeleteZoneDialog(
            nom       = zone.nom,
            hasUsage  = zone.nb_jeux_places > 0,
            usageText = "Cette zone contient ${zone.nb_jeux_places} jeu(x) placé(s).",
            onConfirm = { onDelete(); showDeleteDlg = false },
            onDismiss = { showDeleteDlg = false }
        )
    }
}

// ── Composants partagés ───────────────────────────────────────────────────────

@Composable
private fun ZoneTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = if (selected) Color(0xFFE8F0FB) else Color(0xFFF5F7FC),
        modifier = modifier
    ) {
        TextButton(onClick = onClick) {
            Text(
                text       = label,
                color      = if (selected) AccentBlue else TextGray,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize   = 12.sp,
                maxLines   = 1
            )
        }
    }
}

@Composable
private fun ZoneSummaryCard(items: List<Triple<String, String, String>>) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { (icon, value, label) ->
                Surface(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    color    = PageBg
                ) {
                    Column(
                        Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(icon, fontSize = 16.sp)
                        Text(
                            value,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextDark
                        )
                        Text(label, fontSize = 9.sp, color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneStatChip(icon: String, value: String, label: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = PageBg) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 14.sp)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(label, fontSize = 9.sp, color = TextGray)
        }
    }
}

@Composable
private fun EmptyZoneState(
    icon: String,
    message: String,
    subtitle: String,
    onRefresh: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(message, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextGray, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRefresh) { Text("Rafraîchir", color = AccentBlue) }
        }
    }
}

// ── Dialogues de formulaire ───────────────────────────────────────────────────

@Composable
private fun ZoneTarifaireFormDialog(
    title: String,
    initial: ZoneTarifaireDetailDto?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nom: String, nbTables: Int, prixTable: Double, prixM2: Double?) -> Unit
) {
    var nom        by remember { mutableStateOf(initial?.nom ?: "") }
    var nbTables   by remember { mutableStateOf(initial?.nombre_tables_total?.toString() ?: "") }
    var prixTable  by remember { mutableStateOf(initial?.prix_table?.toString() ?: "") }
    var prixM2     by remember { mutableStateOf(initial?.prix_m2?.toString() ?: "") }

    val canConfirm = nom.isNotBlank() &&
            nbTables.toIntOrNull() != null && (nbTables.toIntOrNull() ?: 0) > 0 &&
            prixTable.toDoubleOrNull() != null && !isLoading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value         = nom,
                    onValueChange = { nom = it },
                    label         = { Text("Nom de la zone *") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value          = nbTables,
                    onValueChange  = { nbTables = it },
                    label          = { Text("Nombre de tables *") },
                    singleLine     = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier       = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value          = prixTable,
                    onValueChange  = {
                        prixTable = it
                        // Auto-calculer le prix m² si vide
                        val parsed = it.toDoubleOrNull()
                        if (parsed != null && prixM2.isBlank()) {
                            prixM2 = "%.2f".format(parsed / 4.5)
                        }
                    },
                    label          = { Text("Prix par table (€) *") },
                    singleLine     = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier       = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value          = prixM2,
                    onValueChange  = { prixM2 = it },
                    label          = { Text("Prix au m² (€) — auto si vide") },
                    singleLine     = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier       = Modifier.fillMaxWidth()
                )
                // Hint prix m²
                Text(
                    "💡 Par défaut : prix table / 4,5",
                    fontSize = 11.sp,
                    color    = TextGray
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    onConfirm(
                        nom.trim(),
                        nbTables.toIntOrNull() ?: 0,
                        prixTable.toDoubleOrNull() ?: 0.0,
                        prixM2.toDoubleOrNull()
                    )
                },
                enabled = canConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = TextDark)
            ) {
                if (isLoading)
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = Color.White
                    )
                else
                    Text(
                        if (initial == null) "Créer" else "Enregistrer",
                        color = Color.White
                    )
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun ZonePlanFormDialog(
    title: String,
    initial: ZonePlanDetailDto?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nom: String, nbTables: Int) -> Unit
) {
    var nom      by remember { mutableStateOf(initial?.nom ?: "") }
    var nbTables by remember { mutableStateOf(initial?.nombre_tables_total?.toString() ?: "") }

    val canConfirm = nom.isNotBlank() &&
            nbTables.toIntOrNull() != null && (nbTables.toIntOrNull() ?: 0) > 0 &&
            !isLoading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value         = nom,
                    onValueChange = { nom = it },
                    label         = { Text("Nom de la zone *") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value           = nbTables,
                    onValueChange   = { nbTables = it },
                    label           = { Text("Nombre de tables *") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier        = Modifier.fillMaxWidth()
                )
                Text(
                    "ℹ️ Les zones du plan servent au placement physique des jeux.",
                    fontSize = 11.sp,
                    color    = TextGray
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(nom.trim(), nbTables.toIntOrNull() ?: 0) },
                enabled  = canConfirm,
                colors   = ButtonDefaults.buttonColors(containerColor = TextDark)
            ) {
                if (isLoading)
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = Color.White
                    )
                else
                    Text(
                        if (initial == null) "Créer" else "Enregistrer",
                        color = Color.White
                    )
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun ConfirmDeleteZoneDialog(
    nom: String,
    hasUsage: Boolean,
    usageText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer cette zone ?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vous allez supprimer « $nom ».")
                if (hasUsage) {
                    Card(
                        shape  = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            "⚠ Impossible : $usageText",
                            fontSize = 12.sp,
                            color    = ErrorRed,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                enabled  = !hasUsage,
                colors   = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Supprimer", color = Color.White)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}