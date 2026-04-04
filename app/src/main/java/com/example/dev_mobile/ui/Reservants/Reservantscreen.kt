package com.example.dev_mobile.ui.reservants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.network.ContactDto
import com.example.dev_mobile.network.HistoriqueDto
import com.example.dev_mobile.network.ReservantDetailDto
import com.example.dev_mobile.network.ReservantDto
import com.example.dev_mobile.session.UserSession
import com.example.dev_mobile.ui.common.OfflineBadge
import com.example.dev_mobile.ui.common.OfflineEmptyState

private val AccentBlue = Color(0xFF4A7FC1)
private val TextDark   = Color(0xFF1A1A2E)
private val TextGray   = Color(0xFF8A8FA3)
private val PageBg     = Color(0xFFF4F7FC)
private val CardBg     = Color(0xFFFFFFFF)
private val DividerCol = Color(0xFFF0F3F8)
private val BorderGray = Color(0xFFE2E6EF)
private val ErrorRed   = Color(0xFFD32F2F)

private val canView   get() = UserSession.canManage()
private val canCreate get() = UserSession.canManage()
private val canUpdate get() = UserSession.canManage()
private val canDelete get() = UserSession.canAdmin()

@Composable
fun ReservantScreen(
    isOnline: Boolean = true,
    viewModel: ReservantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var toDelete         by remember { mutableStateOf<ReservantDto?>(null) }
    val snackbar = remember { SnackbarHostState() }

    // Recharger dès que connexion revient
    LaunchedEffect(isOnline) {
        if (isOnline) viewModel.loadReservants()
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        uiState.errorMessage?.let   { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    // Écran détail
    uiState.selectedReservant?.let { detail ->
        ReservantDetailScreen(
            detail       = detail,
            isOnline     = isOnline,
            onBack       = { viewModel.closeDetail() },
            onUpdate     = { nom, type, contacts -> viewModel.updateReservant(detail.id, nom, type, contacts) },
            onDelete     = { viewModel.deleteReservant(detail.id) },
            isSubmitting = uiState.isSubmitting
        )
        return
    }

    if (uiState.isLoadingDetail) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                val isError = data.visuals.message.startsWith("Erreur") ||
                        data.visuals.message.startsWith("Serveur") ||
                        data.visuals.message.startsWith("Impossible") ||
                        data.visuals.message.startsWith("Création") ||
                        data.visuals.message.startsWith("Modification") ||
                        data.visuals.message.startsWith("Suppression")
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isError) "❌" else "✅", fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(data.visuals.message,
                            color = if (isError) Color(0xFFD32F2F) else Color(0xFF388E3C),
                            fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        containerColor = PageBg
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // En-tête
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Réservants", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        if (!isOnline) {
                            Spacer(Modifier.width(8.dp))
                            OfflineBadge()
                        }
                    }
                    Text(
                        if (!isOnline) "Mode lecture — données en cache"
                        else "Gérez les réservants du festival",
                        fontSize = 13.sp, color = TextGray
                    )
                }
                if (canCreate) {
                    Button(
                        onClick  = { showCreateDialog = true },
                        enabled  = isOnline,
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = TextDark,
                            disabledContainerColor = Color(0xFFCCCCCC)
                        )
                    ) {
                        Text(
                            if (isOnline) "+ Nouveau réservant" else "🔒 Hors ligne",
                            fontSize = 13.sp, color = Color.White
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
                uiState.reservants.isEmpty() && !isOnline -> OfflineEmptyState(
                    message  = "Aucun réservant en cache",
                    subtitle = "Reconnectez-vous pour charger les données"
                )
                uiState.reservants.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Aucun réservant", color = TextGray, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadReservants() }) { Text("Rafraîchir") }
                    }
                }
                else -> {
                    // Banner cache
                    if (uiState.isFromCache) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📡", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Text("Données depuis le cache — lecture seule", fontSize = 12.sp,
                                    color = Color(0xFFE65100), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.reservants, key = { it.id }) { r ->
                            ReservantCard(
                                reservant     = r,
                                onClick       = { viewModel.openDetail(r.id) },
                                onDeleteClick = if (canDelete && isOnline) ({ toDelete = r }) else null
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    toDelete?.let { r ->
        ConfirmDeleteDialog(
            nom             = r.nom,
            hasReservations = r.nb_reservations > 0,
            onConfirm       = { viewModel.deleteReservant(r.id); toDelete = null },
            onDismiss       = { toDelete = null }
        )
    }

    if (showCreateDialog && isOnline) {
        ReservantFormDialog(
            title     = "Nouveau réservant",
            initial   = null,
            isLoading = uiState.isSubmitting,
            onDismiss = { showCreateDialog = false },
            onConfirm = { nom, type, contacts ->
                viewModel.createReservant(nom, type, contacts)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ReservantCard(
    reservant: ReservantDto,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)?
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8F0FB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(reservant.nom.take(1).uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(reservant.nom, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text(buildString {
                        append(reservant.type_reservant.replaceFirstChar { it.uppercase() })
                        reservant.editeur_nom?.let { append(" · $it") }
                    }, fontSize = 12.sp, color = TextGray)
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("👤 ${reservant.nb_contacts} contact(s)", fontSize = 12.sp, color = TextGray)
                    Text("📋 ${reservant.nb_reservations} réservation(s)", fontSize = 12.sp, color = TextGray)
                }
                onDeleteClick?.let {
                    TextButton(onClick = it, contentPadding = PaddingValues(0.dp)) {
                        Text("🗑", fontSize = 16.sp, color = ErrorRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservantDetailScreen(
    detail: ReservantDetailDto,
    isOnline: Boolean,
    onBack: () -> Unit,
    onUpdate: (nom: String, type: String, contacts: List<ContactDto>) -> Unit,
    onDelete: () -> Unit,
    isSubmitting: Boolean
) {
    var showEditDialog   by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(PageBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(CardBg).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Retour", color = AccentBlue, fontWeight = FontWeight.SemiBold) }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(detail.nom, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                if (!isOnline) { Spacer(Modifier.width(6.dp)); OfflineBadge() }
            }
            if (canUpdate && isOnline) {
                TextButton(onClick = { showEditDialog = true }) { Text("✏️", color = AccentBlue, fontSize = 18.sp) }
            }
            if (canDelete && isOnline) {
                TextButton(onClick = { showDeleteDialog = true }) { Text("🗑", color = ErrorRed, fontSize = 18.sp) }
            }
        }
        HorizontalDivider(color = BorderGray)

        // Message info si offline
        if (!isOnline) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFF3E0)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📡", fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Détails limités en mode hors ligne. Contacts et historique non disponibles.",
                        fontSize = 12.sp, color = Color(0xFFE65100))
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailSection("Informations") {
                InfoRow("Type", detail.type_reservant.replaceFirstChar { it.uppercase() })
                detail.editeur_nom?.let { InfoRow("Éditeur", it) }
            }

            DetailSection("Contacts (${detail.contacts?.size ?: 0})") {
                if (detail.contacts.isNullOrEmpty()) {
                    Text(
                        if (!isOnline) "Contacts non disponibles hors ligne"
                        else "Aucun contact enregistré",
                        color = TextGray, fontSize = 13.sp
                    )
                } else {
                    detail.contacts.forEachIndexed { i, c ->
                        if (i > 0) HorizontalDivider(color = DividerCol, modifier = Modifier.padding(vertical = 8.dp))
                        ContactItem(c)
                    }
                }
            }

            DetailSection("Historique réservations (${detail.historique?.size ?: 0})") {
                if (detail.historique.isNullOrEmpty()) {
                    Text(
                        if (!isOnline) "Historique non disponible hors ligne"
                        else "Aucune réservation dans l'historique",
                        color = TextGray, fontSize = 13.sp
                    )
                } else {
                    detail.historique.forEachIndexed { i, h ->
                        if (i > 0) HorizontalDivider(color = DividerCol, modifier = Modifier.padding(vertical = 8.dp))
                        HistoriqueItem(h)
                    }
                }
            }
        }
    }

    if (showEditDialog && isOnline) {
        ReservantFormDialog(
            title     = "Modifier le réservant",
            initial   = detail,
            isLoading = isSubmitting,
            onDismiss = { showEditDialog = false },
            onConfirm = { nom, type, contacts ->
                onUpdate(nom, type, contacts)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog && isOnline) {
        ConfirmDeleteDialog(
            nom             = detail.nom,
            hasReservations = (detail.historique?.size ?: 0) > 0,
            onConfirm       = { onDelete(); showDeleteDialog = false },
            onDismiss       = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label : ", fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = TextDark)
    }
}

@Composable
private fun ContactItem(contact: ContactDto) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE8F0FB)), contentAlignment = Alignment.Center) {
            Text("👤", fontSize = 16.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(contact.nom, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            contact.role_profession?.let { Text(it, fontSize = 12.sp, color = TextGray) }
            contact.email?.let { Spacer(Modifier.height(3.dp)); Text("✉ $it", fontSize = 12.sp, color = AccentBlue) }
            contact.telephone?.let { Text("📞 $it", fontSize = 12.sp, color = TextGray) }
        }
    }
}

@Composable
private fun HistoriqueItem(histo: HistoriqueDto) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(histo.festival_nom, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                histo.date_debut?.let { Text(it.take(10), fontSize = 11.sp, color = TextGray) }
            }
            if (histo.montant_total > 0)
                Text("%.0f €".format(histo.montant_total), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            EtatBadge(histo.etat_contact)
            PresenceBadge(histo.etat_presence)
            if (histo.nb_tables > 0) Text("${histo.nb_tables} table(s)", fontSize = 11.sp, color = TextGray)
        }
    }
}

@Composable
private fun EtatBadge(etat: String) {
    val (bg, fg, label) = when (etat) {
        "pas_contacte"        -> Triple(Color(0xFFF5F5F5), Color(0xFF9E9E9E), "Pas contacté")
        "contacte"            -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "Contacté")
        "en_discussion"       -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "En discussion")
        "reserve"             -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "Réservé")
        "liste_jeux_demandee" -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), "Liste demandée")
        "liste_jeux_obtenue"  -> Triple(Color(0xFFE8EAF6), Color(0xFF3949AB), "Liste obtenue")
        "jeux_recus"          -> Triple(Color(0xFFE0F7FA), Color(0xFF00838F), "Jeux reçus")
        else                  -> Triple(Color(0xFFF5F5F5), Color(0xFF9E9E9E), etat)
    }
    Surface(shape = RoundedCornerShape(10.dp), color = bg) {
        Text(label, fontSize = 11.sp, color = fg, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun PresenceBadge(etat: String) {
    if (etat == "non_defini") return
    val (bg, fg, label) = when (etat) {
        "present"          -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "Présent")
        "absent"           -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "Absent")
        "considere_absent" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Considéré absent")
        else               -> Triple(Color(0xFFF5F5F5), Color(0xFF9E9E9E), etat)
    }
    Surface(shape = RoundedCornerShape(10.dp), color = bg) {
        Text(label, fontSize = 11.sp, color = fg, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun ReservantFormDialog(
    title: String, initial: ReservantDetailDto?, isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nom: String, type: String, contacts: List<ContactDto>) -> Unit
) {
    var nom  by remember { mutableStateOf(initial?.nom ?: "") }
    var type by remember { mutableStateOf(initial?.type_reservant ?: "editeur") }
    val contacts = remember { mutableStateListOf<ContactDto>().also { list -> initial?.contacts?.let { list.addAll(it) } } }
    var showContactForm by remember { mutableStateOf(false) }
    var contactNom  by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactTel  by remember { mutableStateOf("") }
    var contactRole by remember { mutableStateOf("") }
    val types = listOf("editeur", "prestataire", "association", "animation", "boutique", "autre")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom du réservant *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Type :", fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                types.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t, fontSize = 11.sp) }) }
                    }
                }
                HorizontalDivider(color = BorderGray)
                Text("Contacts :", fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                contacts.forEachIndexed { i, c ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.nom, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            c.role_profession?.let { Text(it, fontSize = 11.sp, color = TextGray) }
                            c.email?.let { Text(it, fontSize = 11.sp, color = AccentBlue) }
                        }
                        IconButton(onClick = { contacts.removeAt(i) }) { Text("✕", color = ErrorRed, fontSize = 14.sp) }
                    }
                }
                if (showContactForm) {
                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD))) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = contactNom, onValueChange = { contactNom = it }, label = { Text("Nom *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = contactRole, onValueChange = { contactRole = it }, label = { Text("Profession") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = contactTel, onValueChange = { contactTel = it }, label = { Text("Téléphone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { showContactForm = false }, Modifier.weight(1f)) { Text("Annuler", fontSize = 12.sp) }
                                Button(
                                    onClick = {
                                        if (contactNom.isNotBlank()) {
                                            contacts.add(ContactDto(nom = contactNom, email = contactEmail.ifBlank { null }, telephone = contactTel.ifBlank { null }, role_profession = contactRole.ifBlank { null }))
                                            contactNom = ""; contactEmail = ""; contactTel = ""; contactRole = ""; showContactForm = false
                                        }
                                    },
                                    enabled = contactNom.isNotBlank(), modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                ) { Text("Ajouter", fontSize = 12.sp, color = Color.White) }
                            }
                        }
                    }
                } else {
                    TextButton(onClick = { showContactForm = true }, contentPadding = PaddingValues(0.dp)) {
                        Text("+ Ajouter un contact", color = AccentBlue, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nom, type, contacts.toList()) },
                enabled = nom.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = TextDark)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text(if (initial == null) "Créer" else "Enregistrer", color = Color.White)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun ConfirmDeleteDialog(nom: String, hasReservations: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer ce réservant ?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vous allez supprimer « $nom ».")
                if (hasReservations) {
                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                        Text("⚠ Impossible de supprimer ce réservant car il a des réservations en cours.",
                            fontSize = 12.sp, color = ErrorRed, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), enabled = !hasReservations) {
                Text("Supprimer", color = Color.White)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}