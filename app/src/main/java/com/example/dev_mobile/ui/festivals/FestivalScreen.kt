package com.example.dev_mobile.ui.festivals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.dev_mobile.network.FestivalDashboardDto
import com.example.dev_mobile.session.UserSession

// ── Palette (cohérente avec l'app) ────────────────────────────────────────────
private val AccentBlue   = Color(0xFF4A7FC1)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8FA3)
private val PageBg       = Color(0xFFF4F7FC)
private val CardBg       = Color(0xFFFFFFFF)
private val DividerCol   = Color(0xFFF0F3F8)
private val BorderGray   = Color(0xFFE2E6EF)
private val ErrorRed     = Color(0xFFD32F2F)
private val SuccessGreen = Color(0xFF388E3C)
private val GoldColor    = Color(0xFFF59E0B)

private val canCreate get() = UserSession.canAdmin()
private val canEdit   get() = UserSession.canAdmin()
private val canDelete get() = UserSession.isAdmin()

// ── Écran principal ────────────────────────────────────────────────────────────
@Composable
fun FestivalScreen(
    onFestivalCourantChanged: (() -> Unit)? = null,
    vm: FestivalViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.errorMessage?.let  { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    // Notifier la nav quand le courant change
    val courant = state.festivals.firstOrNull { it.est_courant }
    LaunchedEffect(courant?.id) { onFestivalCourantChanged?.invoke() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                val isErr = data.visuals.message.startsWith("Erreur") ||
                        data.visuals.message.startsWith("Serveur") ||
                        data.visuals.message.startsWith("Impossible")
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isErr) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isErr) "❌" else "✅", fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(data.visuals.message,
                            color = if (isErr) ErrorRed else SuccessGreen,
                            fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        containerColor = PageBg
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // ── En-tête ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Festivals", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("${state.festivals.size} festival(s) au total", fontSize = 12.sp, color = TextGray)
                }
                if (canCreate) {
                    Button(
                        onClick = { vm.openCreateDialog() },
                        shape  = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                    ) { Text("+ Nouveau", fontSize = 13.sp, color = Color.White) }
                }
            }
            HorizontalDivider(color = DividerCol)

            // ── Liste ────────────────────────────────────────────────────────
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
                state.festivals.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Aucun festival", color = TextGray, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { vm.loadFestivals() }) {
                            Text("Rafraîchir", color = AccentBlue)
                        }
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.festivals, key = { it.id }) { festival ->
                        FestivalCard(
                            festival    = festival,
                            isSubmitting = state.isSubmitting,
                            onSetCourant = { vm.setCourant(festival.id) },
                            onEdit       = if (canEdit) ({ vm.openEditDialog(festival) }) else null,
                            onDelete     = if (canDelete) ({ vm.deleteFestival(festival.id) }) else null
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (state.showCreateDialog) {
        FestivalFormDialog(
            title     = "Nouveau festival",
            initial   = null,
            isLoading = state.isSubmitting,
            onDismiss = { vm.closeCreateDialog() },
            onConfirm = { nom, esp, dd, df, sStd, sGde, sMairie, scStd, scMairie, prix, setCourant ->
                vm.createFestival(nom, esp, dd, df, sStd, sGde, sMairie, scStd, scMairie, prix, setCourant)
            }
        )
    }

    if (state.showEditDialog && state.editTarget != null) {
        FestivalFormDialog(
            title     = "Modifier le festival",
            initial   = state.editTarget,
            isLoading = state.isSubmitting,
            onDismiss = { vm.closeEditDialog() },
            onConfirm = { nom, esp, dd, df, sStd, sGde, sMairie, scStd, scMairie, prix, _ ->
                vm.updateFestival(state.editTarget!!.id, nom, esp, dd, df, sStd, sGde, sMairie, scStd, scMairie, prix)
            }
        )
    }
}

// ── Card festival ──────────────────────────────────────────────────────────────
@Composable
private fun FestivalCard(
    festival: FestivalDashboardDto,
    isSubmitting: Boolean,
    onSetCourant: () -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    var showDeleteDlg by remember { mutableStateOf(false) }

    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(if (festival.est_courant) 6.dp else 2.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .then(
                if (festival.est_courant)
                    Modifier.border(2.dp, GoldColor, RoundedCornerShape(16.dp))
                else Modifier
            )
    ) {
        Column(Modifier.padding(16.dp)) {

            // ── Header de la card ────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (festival.est_courant) GoldColor.copy(alpha = 0.15f) else Color(0xFFE8F0FB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (festival.est_courant) "⭐" else "📅", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            festival.nom,
                            fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark
                        )
                        if (festival.est_courant) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GoldColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "COURANT", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    color = GoldColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    // Dates
                    val dateRange = buildString {
                        festival.date_debut?.take(10)?.let { append(it) }
                        if (festival.date_debut != null && festival.date_fin != null) append(" → ")
                        festival.date_fin?.take(10)?.let { append(it) }
                    }
                    if (dateRange.isNotBlank()) {
                        Text(dateRange, fontSize = 11.sp, color = TextGray)
                    }
                }
                // Actions
                if (onEdit != null) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Text("✏️", fontSize = 16.sp)
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = { showDeleteDlg = true }, modifier = Modifier.size(36.dp)) {
                        Text("🗑", fontSize = 16.sp, color = ErrorRed)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(10.dp))

            // ── Statistiques ─────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    icon  = "🪑",
                    label = "Tables",
                    value = "${festival.espace_tables_total}",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon  = "🗺️",
                    label = "Zones tar.",
                    value = "${festival.nb_zones_tarifaires}",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon  = "📋",
                    label = "Réservations",
                    value = "${festival.nb_reservations_totales}",
                    modifier = Modifier.weight(1f)
                )
            }

            // Montant total si factures
            if (festival.montant_total_factures > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip(
                        icon  = "💰",
                        label = "Facturé",
                        value = "%.0f €".format(festival.montant_total_factures),
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        icon  = "✅",
                        label = "Payé",
                        value = "%.0f €".format(festival.montant_paye),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.weight(1f))
                }
            }

            // Stocks tables
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Stocks", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                    Text(
                        "📦 Std:${festival.stock_tables_standard} · Gde:${festival.stock_tables_grandes} · Mairie:${festival.stock_tables_mairie}",
                        fontSize = 11.sp, color = TextDark
                    )
                }
                // Bouton Définir comme courant (si pas déjà courant)
                if (!festival.est_courant) {
                    OutlinedButton(
                        onClick  = onSetCourant,
                        enabled  = !isSubmitting,
                        shape    = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text("Définir courant", fontSize = 11.sp, color = AccentBlue)
                    }
                }
            }
        }
    }

    // Dialog suppression
    if (showDeleteDlg) {
        ConfirmDeleteFestivalDialog(
            nom       = festival.nom,
            onConfirm = { onDelete?.invoke(); showDeleteDlg = false },
            onDismiss = { showDeleteDlg = false }
        )
    }
}

// ── Chip stat ─────────────────────────────────────────────────────────────────
@Composable
private fun StatChip(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(10.dp),
        color    = Color(0xFFF4F7FC)
    ) {
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

// ── Dialog formulaire (création + modification) ────────────────────────────────
@Composable
private fun FestivalFormDialog(
    title: String,
    initial: FestivalDashboardDto?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        nom: String,
        espaceTables: Int,
        dateDebut: String?,
        dateFin: String?,
        stockStd: Int,
        stockGde: Int,
        stockMairie: Int,
        stockChaisesStd: Int,
        stockChaisesMairie: Int,
        prixPrise: Double,
        setCourant: Boolean
    ) -> Unit
) {
    // Champs pré-remplis si édition
    var nom             by remember { mutableStateOf(initial?.nom ?: "") }
    var espaceTables    by remember { mutableStateOf(initial?.espace_tables_total?.toString() ?: "") }
    var dateDebut       by remember { mutableStateOf(initial?.date_debut?.take(10) ?: "") }
    var dateFin         by remember { mutableStateOf(initial?.date_fin?.take(10) ?: "") }
    var stockStd        by remember { mutableStateOf(initial?.stock_tables_standard?.toString() ?: "0") }
    var stockGde        by remember { mutableStateOf(initial?.stock_tables_grandes?.toString() ?: "0") }
    var stockMairie     by remember { mutableStateOf(initial?.stock_tables_mairie?.toString() ?: "0") }
    var stockChStd      by remember { mutableStateOf(initial?.stock_chaises_standard?.toString() ?: "0") }
    var stockChMairie   by remember { mutableStateOf(initial?.stock_chaises_mairie?.toString() ?: "0") }
    var prixPrise       by remember { mutableStateOf(initial?.prix_prise_electrique?.toString() ?: "0") }
    var setCourant      by remember { mutableStateOf(false) }

    val isCreation = initial == null
    val canConfirm = nom.isNotBlank() && espaceTables.isNotBlank() && !isLoading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // ── Section principale ───────────────────────────────────────
                SectionHeader("Informations générales")

                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom du festival *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = espaceTables, onValueChange = { espaceTables = it },
                    label = { Text("Espace total (tables) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateDebut, onValueChange = { dateDebut = it },
                        label = { Text("Début (YYYY-MM-DD)") },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dateFin, onValueChange = { dateFin = it },
                        label = { Text("Fin (YYYY-MM-DD)") },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }

                // ── Stocks tables ────────────────────────────────────────────
                SectionHeader("Stocks de tables")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockStd, onValueChange = { stockStd = it },
                        label = { Text("Standard") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockGde, onValueChange = { stockGde = it },
                        label = { Text("Grandes") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockMairie, onValueChange = { stockMairie = it },
                        label = { Text("Mairie") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Stocks chaises ───────────────────────────────────────────
                SectionHeader("Stocks de chaises")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockChStd, onValueChange = { stockChStd = it },
                        label = { Text("Standard") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockChMairie, onValueChange = { stockChMairie = it },
                        label = { Text("Mairie") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Prix prise ───────────────────────────────────────────────
                SectionHeader("Tarification")

                OutlinedTextField(
                    value = prixPrise, onValueChange = { prixPrise = it },
                    label = { Text("Prix prise électrique (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        nom.trim(),
                        espaceTables.toIntOrNull() ?: 0,
                        dateDebut.ifBlank { null },
                        dateFin.ifBlank { null },
                        stockStd.toIntOrNull() ?: 0,
                        stockGde.toIntOrNull() ?: 0,
                        stockMairie.toIntOrNull() ?: 0,
                        stockChStd.toIntOrNull() ?: 0,
                        stockChMairie.toIntOrNull() ?: 0,
                        prixPrise.toDoubleOrNull() ?: 0.0,
                        setCourant
                    )
                },
                enabled = canConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = TextDark)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text(if (isCreation) "Créer" else "Enregistrer", color = Color.White)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

// ── Titres de section dans le formulaire ──────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = AccentBlue,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// ── Dialog confirmation suppression ───────────────────────────────────────────
@Composable
private fun ConfirmDeleteFestivalDialog(
    nom: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer ce festival ?", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vous allez supprimer « $nom ».")
                Card(
                    shape  = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        "⚠ Un festival ne peut être supprimé que s'il ne contient aucune zone, réservation ou zone tarifaire.",
                        fontSize = 12.sp, color = ErrorRed,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) { Text("Supprimer", color = Color.White) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

