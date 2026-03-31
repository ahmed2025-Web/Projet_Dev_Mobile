package com.example.dev_mobile.ui.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.dev_mobile.network.*
import com.example.dev_mobile.session.UserSession

// ── Palette cohérente ─────────────────────────────────────────────────────────
private val AccentBlue   = Color(0xFF4A7FC1)
private val TextDark     = Color(0xFF1A1A2E)
private val TextGray     = Color(0xFF8A8FA3)
private val PageBg       = Color(0xFFF4F7FC)
private val CardBg       = Color(0xFFFFFFFF)
private val DividerCol   = Color(0xFFF0F3F8)
private val BorderGray   = Color(0xFFE2E6EF)
private val ErrorRed     = Color(0xFFD32F2F)
private val SuccessGreen = Color(0xFF388E3C)

private val canCreate get() = UserSession.canManage()
private val canEdit   get() = UserSession.canManage()
private val canDelete get() = UserSession.canAdmin()

// ── Helpers workflow ──────────────────────────────────────────────────────────

val ETATS_CONTACT = listOf(
    "pas_contacte", "contacte", "en_discussion", "reserve",
    "liste_jeux_demandee", "liste_jeux_obtenue", "jeux_recus"
)

val ETATS_PRESENCE = listOf("non_defini", "present", "considere_absent", "absent")

fun etatContactLabel(etat: String) = when (etat) {
    "pas_contacte"        -> "Pas contacté"
    "contacte"            -> "Contacté"
    "en_discussion"       -> "En discussion"
    "reserve"             -> "Réservé"
    "liste_jeux_demandee" -> "Liste demandée"
    "liste_jeux_obtenue"  -> "Liste obtenue"
    "jeux_recus"          -> "Jeux reçus"
    else                  -> etat
}

fun etatContactColor(etat: String): Pair<Color, Color> = when (etat) {
    "pas_contacte"        -> Color(0xFFF5F5F5) to Color(0xFF9E9E9E)
    "contacte"            -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
    "en_discussion"       -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
    "reserve"             -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
    "liste_jeux_demandee" -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
    "liste_jeux_obtenue"  -> Color(0xFFE8EAF6) to Color(0xFF3949AB)
    "jeux_recus"          -> Color(0xFFE0F7FA) to Color(0xFF00838F)
    else                  -> Color(0xFFF5F5F5) to Color(0xFF9E9E9E)
}

fun etatPresenceLabel(etat: String) = when (etat) {
    "non_defini"       -> "Non défini"
    "present"          -> "Présent"
    "considere_absent" -> "Considéré absent"
    "absent"           -> "Absent"
    else               -> etat
}

fun etatPresenceColor(etat: String): Pair<Color, Color> = when (etat) {
    "present"          -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
    "absent"           -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
    "considere_absent" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
    else               -> Color(0xFFF5F5F5) to Color(0xFF9E9E9E)
}

// ── Écran principal ────────────────────────────────────────────────────────────
@Composable
fun ReservationScreen(
    festivalId: Int,
    vm: ReservationViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // Init avec l'id du festival
    LaunchedEffect(festivalId) { vm.init(festivalId) }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.errorMessage?.let  { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    // Écran détail
    state.selectedReservation?.let { detail ->
        ReservationDetailScreen(
            detail       = detail,
            zonesTarifaires = state.zonesTarifaires,
            isSubmitting = state.isSubmitting,
            onBack       = { vm.closeDetail() },
            onUpdateWorkflowContact = { etat -> vm.updateWorkflowContact(detail.id, etat) },
            onUpdateWorkflowPresence = { etat -> vm.updateWorkflowPresence(detail.id, etat) },
            onAddContactRelance = { date, type, notes -> vm.addContactRelance(detail.id, date, type, notes) },
            onUpdate = { nbPrises, remiseT, remiseM, notes, animer, zones ->
                vm.updateReservation(detail.id, nbPrises, remiseT, remiseM, notes, animer, zones)
            },
            onDelete = { vm.deleteReservation(detail.id) }
        )
        return
    }

    if (state.isLoadingDetail) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    if (festivalId <= 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚠️", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Aucun festival sélectionné", color = TextGray, fontSize = 14.sp)
            }
        }
        return
    }

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
                    Text("Réservations", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("${vm.getFilteredReservations().size} résultat(s)", fontSize = 12.sp, color = TextGray)
                }
                if (canCreate) {
                    Button(
                        onClick = { vm.openCreateDialog() },
                        shape  = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                    ) { Text("+ Nouvelle", fontSize = 13.sp, color = Color.White) }
                }
            }

            // ── Filtres workflow ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Filtre "Tous"
                FilterChipItem(
                    label = "Tous (${state.reservations.size})",
                    selected = state.activeFilter == null,
                    bgColor = Color(0xFFE8F0FB),
                    textColor = AccentBlue,
                    onClick = { vm.setFilter(null) }
                )
                ETATS_CONTACT.forEach { etat ->
                    val count = state.reservations.count { it.etat_contact == etat }
                    if (count > 0) {
                        val (bg, fg) = etatContactColor(etat)
                        FilterChipItem(
                            label = "${etatContactLabel(etat)} ($count)",
                            selected = state.activeFilter == etat,
                            bgColor = bg,
                            textColor = fg,
                            onClick = { vm.setFilter(etat) }
                        )
                    }
                }
            }
            HorizontalDivider(color = DividerCol)

            // ── Contenu ──────────────────────────────────────────────────────
            val displayed = vm.getFilteredReservations()
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
                displayed.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Aucune réservation", color = TextGray, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { vm.loadReservations() }) {
                            Text("Rafraîchir", color = AccentBlue)
                        }
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayed, key = { it.id }) { res ->
                        ReservationCard(
                            reservation = res,
                            onContactClick  = { vm.updateWorkflowContact(res.id, nextEtatContact(res.etat_contact)) },
                            onClick     = { vm.openDetail(res.id) }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Dialog création
    if (state.showCreateDialog) {
        CreateReservationDialog(
            reservants = state.reservants,
            zonesTarifaires = state.zonesTarifaires,
            isLoading  = state.isSubmitting,
            onDismiss  = { vm.closeCreateDialog() },
            onConfirm  = { reservantId, nbPrises, notes, animer, zones ->
                vm.createReservation(reservantId, nbPrises, notes, animer, zones)
            }
        )
    }
}

// ── Avancer le workflow contact ───────────────────────────────────────────────
private fun nextEtatContact(current: String): String = when (current) {
    "pas_contacte"        -> "contacte"
    "contacte"            -> "en_discussion"
    "en_discussion"       -> "reserve"
    "reserve"             -> "liste_jeux_demandee"
    "liste_jeux_demandee" -> "liste_jeux_obtenue"
    "liste_jeux_obtenue"  -> "jeux_recus"
    else                  -> current
}

// ── Chip de filtre ────────────────────────────────────────────────────────────
@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = if (selected) bgColor else Color(0xFFF5F5F5),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) textColor else TextGray,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

// ── Card réservation ──────────────────────────────────────────────────────────
@Composable
private fun ReservationCard(
    reservation: ReservationDto,
    onContactClick: () -> Unit,
    onClick: () -> Unit
) {
    val (contactBg, contactFg) = etatContactColor(reservation.etat_contact)
    val (presenceBg, presenceFg) = etatPresenceColor(reservation.etat_presence)

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // En-tête
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8F0FB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(reservation.reservant_nom.take(1).uppercase(),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(reservation.reservant_nom, fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text(reservation.type_reservant.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp, color = TextGray)
                }
                // Montant brut
                if (reservation.montant_brut > 0) {
                    Text("%.0f €".format(reservation.montant_brut),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Badges workflow
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge contact (cliquable pour avancer le workflow)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = contactBg,
                    modifier = Modifier.clickable(
                        enabled = canEdit && reservation.etat_contact != "jeux_recus"
                    ) { onContactClick() }
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(etatContactLabel(reservation.etat_contact),
                            fontSize = 11.sp, color = contactFg, fontWeight = FontWeight.SemiBold)
                        if (canEdit && reservation.etat_contact != "jeux_recus") {
                            Spacer(Modifier.width(3.dp))
                            Text("›", fontSize = 11.sp, color = contactFg)
                        }
                    }
                }

                // Badge présence (si défini)
                if (reservation.etat_presence != "non_defini") {
                    Surface(shape = RoundedCornerShape(10.dp), color = presenceBg) {
                        Text(etatPresenceLabel(reservation.etat_presence),
                            fontSize = 11.sp, color = presenceFg, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            // Stats
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (reservation.nb_tables_reservees > 0)
                    Text("🪑 ${reservation.nb_tables_reservees} table(s)", fontSize = 11.sp, color = TextGray)
                if (reservation.nb_jeux > 0)
                    Text("🎮 ${reservation.nb_jeux} jeu(x)", fontSize = 11.sp, color = TextGray)
                reservation.date_dernier_contact?.take(10)?.let {
                    Text("📅 $it", fontSize = 11.sp, color = TextGray)
                }
            }
        }
    }
}

// ── Écran détail ──────────────────────────────────────────────────────────────
@Composable
private fun ReservationDetailScreen(
    detail: ReservationDetailDto,
    zonesTarifaires: List<ZoneTarifaireDto>,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onUpdateWorkflowContact: (String) -> Unit,
    onUpdateWorkflowPresence: (String) -> Unit,
    onAddContactRelance: (String?, String?, String?) -> Unit,
    onUpdate: (Int, Int, Double, String?, Boolean, List<ZoneReserveeRequest>) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog         by remember { mutableStateOf(false) }
    var showDeleteDialog       by remember { mutableStateOf(false) }
    var showAddRelanceDialog   by remember { mutableStateOf(false) }
    var showContactDropdown    by remember { mutableStateOf(false) }
    var showPresenceDropdown   by remember { mutableStateOf(false) }

    val (contactBg, contactFg) = etatContactColor(detail.etat_contact)
    val (presenceBg, presenceFg) = etatPresenceColor(detail.etat_presence)

    Column(Modifier.fillMaxSize().background(PageBg)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Retour", color = AccentBlue, fontWeight = FontWeight.SemiBold)
            }
            Column(Modifier.weight(1f)) {
                Text(detail.reservant_nom, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(detail.festival_nom, fontSize = 11.sp, color = TextGray)
            }
            if (canEdit) {
                TextButton(onClick = { showEditDialog = true }) {
                    Text("✏️", fontSize = 18.sp)
                }
            }
            if (canDelete) {
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text("🗑", fontSize = 18.sp, color = ErrorRed)
                }
            }
        }
        HorizontalDivider(color = BorderGray)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Workflow contact ──────────────────────────────────────────────
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Workflow de contact", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(10.dp))

                    // Stepper visuel
                    ContactWorkflowStepper(currentEtat = detail.etat_contact)

                    Spacer(Modifier.height(12.dp))

                    // Boutons de changement de workflow
                    if (canEdit) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box {
                                OutlinedButton(
                                    onClick = { showContactDropdown = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = contactBg) {
                                        Text(etatContactLabel(detail.etat_contact), fontSize = 11.sp,
                                            color = contactFg, fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    Text("▾", fontSize = 12.sp, color = TextGray)
                                }
                                DropdownMenu(expanded = showContactDropdown,
                                    onDismissRequest = { showContactDropdown = false }) {
                                    ETATS_CONTACT.forEach { etat ->
                                        DropdownMenuItem(
                                            text = {
                                                val (bg, fg) = etatContactColor(etat)
                                                Surface(shape = RoundedCornerShape(8.dp), color = bg) {
                                                    Text(etatContactLabel(etat), fontSize = 13.sp, color = fg,
                                                        fontWeight = if (etat == detail.etat_contact) FontWeight.Bold else FontWeight.Normal,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                }
                                            },
                                            onClick = { showContactDropdown = false; onUpdateWorkflowContact(etat) }
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = { showAddRelanceDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Relance", fontSize = 12.sp, color = AccentBlue)
                            }
                        }
                    }

                    // Historique des relances
                    if (detail.contacts.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Historique des contacts (${detail.contacts.size})", fontSize = 12.sp,
                            color = TextGray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        detail.contacts.forEach { c ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📞", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(c.date_contact?.take(10) ?: "—", fontSize = 12.sp, color = TextDark)
                                    c.type_contact?.let { Text(it, fontSize = 11.sp, color = TextGray) }
                                    c.notes?.let { Text(it, fontSize = 11.sp, color = TextGray) }
                                }
                            }
                        }
                    }
                }
            }

            // ── Présence ──────────────────────────────────────────────────────
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Présence", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(10.dp))
                    if (canEdit) {
                        Box {
                            OutlinedButton(onClick = { showPresenceDropdown = true },
                                shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = presenceBg) {
                                        Text(etatPresenceLabel(detail.etat_presence), fontSize = 11.sp,
                                            color = presenceFg, fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Text("▾", fontSize = 12.sp, color = TextGray)
                                }
                            }
                            DropdownMenu(expanded = showPresenceDropdown,
                                onDismissRequest = { showPresenceDropdown = false }) {
                                ETATS_PRESENCE.forEach { etat ->
                                    DropdownMenuItem(
                                        text = {
                                            val (bg, fg) = etatPresenceColor(etat)
                                            Surface(shape = RoundedCornerShape(8.dp), color = bg) {
                                                Text(etatPresenceLabel(etat), fontSize = 13.sp, color = fg,
                                                    fontWeight = if (etat == detail.etat_presence) FontWeight.Bold else FontWeight.Normal,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                            }
                                        },
                                        onClick = { showPresenceDropdown = false; onUpdateWorkflowPresence(etat) }
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(10.dp), color = presenceBg) {
                            Text(etatPresenceLabel(detail.etat_presence), fontSize = 12.sp,
                                color = presenceFg, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Viendra animer : ", fontSize = 12.sp, color = TextGray)
                        Text(if (detail.viendra_animer) "Oui" else "Non", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Zones réservées ───────────────────────────────────────────────
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Zones tarifaires réservées", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("${detail.nb_tables_reservees} table(s)", fontSize = 12.sp, color = AccentBlue, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    if (detail.zones_reservees.isEmpty()) {
                        Text("Aucune zone réservée", fontSize = 13.sp, color = TextGray)
                    } else {
                        detail.zones_reservees.forEachIndexed { i, z ->
                            if (i > 0) HorizontalDivider(color = DividerCol, modifier = Modifier.padding(vertical = 6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(z.zone_tarifaire_nom ?: "Zone ${z.zone_tarifaire_id}", fontSize = 13.sp, color = TextDark)
                                Text("${z.nombre_tables} tables × %.2f €".format(z.prix_unitaire), fontSize = 12.sp, color = TextGray)
                            }
                        }
                    }
                }
            }

            // ── Facturation ───────────────────────────────────────────────────
            if (detail.montant_brut > 0 || detail.remise_tables > 0 || detail.remise_montant > 0) {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Facturation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(Modifier.height(10.dp))
                        FactureRow("Tables", "%.2f €".format(detail.montant_tables))
                        if (detail.nb_prises_electriques > 0)
                            FactureRow("Prises (${detail.nb_prises_electriques})", "%.2f €".format(detail.montant_prises))
                        FactureRow("Montant brut", "%.2f €".format(detail.montant_brut), bold = true)
                        if (detail.remise_tables > 0)
                            FactureRow("Remise tables", "-${detail.remise_tables} table(s)", color = SuccessGreen)
                        if (detail.remise_montant > 0)
                            FactureRow("Remise montant", "-%.2f €".format(detail.remise_montant), color = SuccessGreen)
                    }
                }
            }

            // ── Notes ─────────────────────────────────────────────────────────
            detail.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Notes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Spacer(Modifier.height(8.dp))
                            Text(notes, fontSize = 13.sp, color = TextGray)
                        }
                    }
                }
            }

            // ── Jeux ─────────────────────────────────────────────────────────
            if (detail.nb_jeux > 0) {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Jeux (${detail.nb_jeux})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("✅ ${detail.nb_jeux_places} placé(s)", fontSize = 12.sp, color = SuccessGreen)
                            Text("📦 ${detail.nb_jeux_recus} reçu(s)", fontSize = 12.sp, color = AccentBlue)
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showEditDialog) {
        EditReservationDialog(
            detail = detail,
            zonesTarifaires = zonesTarifaires,
            isLoading = isSubmitting,
            onDismiss = { showEditDialog = false },
            onConfirm = { nbPrises, remiseT, remiseM, notes, animer, zones ->
                onUpdate(nbPrises, remiseT, remiseM, notes, animer, zones)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer cette réservation ?", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Vous allez supprimer la réservation de « ${detail.reservant_nom} ».")
                    Card(shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                        Text("⚠ Impossible si des jeux sont placés ou si une facture est payée.",
                            fontSize = 12.sp, color = ErrorRed, modifier = Modifier.padding(10.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                    Text("Supprimer", color = Color.White)
                }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Annuler") } }
        )
    }

    if (showAddRelanceDialog) {
        AddRelanceDialog(
            isLoading = isSubmitting,
            onDismiss = { showAddRelanceDialog = false },
            onConfirm = { date, type, notes ->
                onAddContactRelance(date, type, notes)
                showAddRelanceDialog = false
            }
        )
    }
}

// ── Stepper visuel workflow contact ───────────────────────────────────────────
@Composable
private fun ContactWorkflowStepper(currentEtat: String) {
    val steps = ETATS_CONTACT
    val currentIdx = steps.indexOf(currentEtat).coerceAtLeast(0)

    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { i, etat ->
            val (bg, fg) = etatContactColor(etat)
            val isActive = i <= currentIdx
            val isCurrent = i == currentIdx

            if (i > 0) {
                Box(Modifier.width(8.dp).height(2.dp)
                    .background(if (isActive) AccentBlue.copy(alpha = 0.4f) else DividerCol))
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isCurrent) bg else if (isActive) bg.copy(alpha = 0.6f) else Color(0xFFF5F5F5)
            ) {
                Text(
                    text = when(etat) {
                        "pas_contacte" -> "❌"
                        "contacte" -> "📞"
                        "en_discussion" -> "💬"
                        "reserve" -> "✅"
                        "liste_jeux_demandee" -> "📋"
                        "liste_jeux_obtenue" -> "📄"
                        "jeux_recus" -> "🎮"
                        else -> "•"
                    },
                    fontSize = if (isCurrent) 16.sp else 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(etatContactLabel(currentEtat), fontSize = 12.sp, color = AccentBlue, fontWeight = FontWeight.SemiBold)
}

// ── Ligne facture ─────────────────────────────────────────────────────────────
@Composable
private fun FactureRow(label: String, value: String, bold: Boolean = false, color: Color = TextDark) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextGray)
        Text(value, fontSize = 13.sp, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

// ── Dialog création réservation ───────────────────────────────────────────────
@Composable
private fun CreateReservationDialog(
    reservants: List<ReservantDto>,
    zonesTarifaires: List<ZoneTarifaireDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String?, Boolean, List<ZoneReserveeRequest>) -> Unit
) {
    var selectedReservantId by remember { mutableStateOf<Int?>(null) }
    var showReservantMenu   by remember { mutableStateOf(false) }
    var nbPrises            by remember { mutableStateOf("0") }
    var notes               by remember { mutableStateOf("") }
    var viendrAnimer        by remember { mutableStateOf(true) }

    // Zones : nb tables par zone
    val zonesMap = remember { mutableStateMapOf<Int, String>() }

    val canConfirm = selectedReservantId != null && !isLoading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle réservation", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sélection réservant
                Text("Réservant *", fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                Box {
                    OutlinedButton(
                        onClick = { showReservantMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                reservants.find { it.id == selectedReservantId }?.nom ?: "Choisir un réservant",
                                fontSize = 13.sp, color = if (selectedReservantId != null) TextDark else TextGray
                            )
                            Text("▾", fontSize = 12.sp, color = TextGray)
                        }
                    }
                    DropdownMenu(expanded = showReservantMenu,
                        onDismissRequest = { showReservantMenu = false }) {
                        reservants.forEach { r ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(r.nom, fontSize = 13.sp,
                                            fontWeight = if (r.id == selectedReservantId) FontWeight.Bold else FontWeight.Normal)
                                        Text(r.type_reservant, fontSize = 11.sp, color = TextGray)
                                    }
                                },
                                onClick = { selectedReservantId = r.id; showReservantMenu = false }
                            )
                        }
                    }
                }

                HorizontalDivider(color = BorderGray)

                // Zones tarifaires
                if (zonesTarifaires.isNotEmpty()) {
                    Text("Tables par zone tarifaire", fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                    zonesTarifaires.forEach { zone ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(zone.nom, fontSize = 13.sp, color = TextDark)
                                Text("${zone.tables_disponibles} dispo · %.2f €/table".format(zone.prix_table),
                                    fontSize = 11.sp, color = TextGray)
                            }
                            OutlinedTextField(
                                value = zonesMap[zone.id] ?: "",
                                onValueChange = { zonesMap[zone.id] = it },
                                label = { Text("Nb") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(70.dp)
                            )
                        }
                    }
                } else {
                    Card(shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                        Text("⚠ Aucune zone tarifaire pour ce festival. Créez-en d'abord dans l'écran Zones.",
                            fontSize = 12.sp, color = Color(0xFFE65100), modifier = Modifier.padding(10.dp))
                    }
                }

                HorizontalDivider(color = BorderGray)

                // Prises électriques
                OutlinedTextField(
                    value = nbPrises, onValueChange = { nbPrises = it },
                    label = { Text("Nb prises électriques") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 3
                )

                // Viendra animer
                Row(
                    Modifier.fillMaxWidth().clickable { viendrAnimer = !viendrAnimer },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = viendrAnimer, onCheckedChange = { viendrAnimer = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Le réservant viendra animer", fontSize = 13.sp, color = TextDark)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val zones = zonesMap.entries
                        .filter { (_, v) -> v.toIntOrNull() != null && v.toInt() > 0 }
                        .map { (id, v) -> ZoneReserveeRequest(id, v.toInt()) }
                    onConfirm(selectedReservantId!!, nbPrises.toIntOrNull() ?: 0,
                        notes.ifBlank { null }, viendrAnimer, zones)
                },
                enabled = canConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = TextDark)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Créer", color = Color.White)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ── Dialog modification réservation ──────────────────────────────────────────
@Composable
private fun EditReservationDialog(
    detail: ReservationDetailDto,
    zonesTarifaires: List<ZoneTarifaireDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Double, String?, Boolean, List<ZoneReserveeRequest>) -> Unit
) {
    var nbPrises      by remember { mutableStateOf(detail.nb_prises_electriques.toString()) }
    var remiseTables  by remember { mutableStateOf(detail.remise_tables.toString()) }
    var remiseMontant by remember { mutableStateOf(detail.remise_montant.toString()) }
    var notes         by remember { mutableStateOf(detail.notes ?: "") }
    var viendrAnimer  by remember { mutableStateOf(detail.viendra_animer) }

    val zonesMap = remember {
        mutableStateMapOf<Int, String>().also { map ->
            detail.zones_reservees.forEach { z -> map[z.zone_tarifaire_id] = z.nombre_tables.toString() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la réservation", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("${detail.reservant_nom}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AccentBlue)

                // Zones tarifaires
                if (zonesTarifaires.isNotEmpty()) {
                    Text("Tables par zone tarifaire", fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                    zonesTarifaires.forEach { zone ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(zone.nom, fontSize = 13.sp, color = TextDark)
                                Text("%.2f €/table".format(zone.prix_table), fontSize = 11.sp, color = TextGray)
                            }
                            OutlinedTextField(
                                value = zonesMap[zone.id] ?: "",
                                onValueChange = { zonesMap[zone.id] = it },
                                label = { Text("Nb") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(70.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = BorderGray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nbPrises, onValueChange = { nbPrises = it },
                        label = { Text("Prises élec.") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = remiseTables, onValueChange = { remiseTables = it },
                        label = { Text("Remise tables") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = remiseMontant, onValueChange = { remiseMontant = it },
                    label = { Text("Remise montant (€)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 3
                )
                Row(
                    Modifier.fillMaxWidth().clickable { viendrAnimer = !viendrAnimer },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = viendrAnimer, onCheckedChange = { viendrAnimer = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Viendra animer", fontSize = 13.sp, color = TextDark)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val zones = zonesMap.entries
                        .filter { (_, v) -> v.toIntOrNull() != null && v.toInt() > 0 }
                        .map { (id, v) -> ZoneReserveeRequest(id, v.toInt()) }
                    onConfirm(nbPrises.toIntOrNull() ?: 0, remiseTables.toIntOrNull() ?: 0,
                        remiseMontant.toDoubleOrNull() ?: 0.0, notes.ifBlank { null }, viendrAnimer, zones)
                },
                enabled = !isLoading,
                colors  = ButtonDefaults.buttonColors(containerColor = TextDark)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Enregistrer", color = Color.White)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ── Dialog ajout relance ──────────────────────────────────────────────────────
@Composable
private fun AddRelanceDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?, String?) -> Unit
) {
    var date  by remember { mutableStateOf("") }
    var type  by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un contact / relance", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = date, onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type, onValueChange = { type = it },
                    label = { Text("Type (mail, téléphone…)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(date.ifBlank { null }, type.ifBlank { null }, notes.ifBlank { null }) },
                enabled  = !isLoading,
                colors   = ButtonDefaults.buttonColors(containerColor = TextDark)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Ajouter", color = Color.White)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}