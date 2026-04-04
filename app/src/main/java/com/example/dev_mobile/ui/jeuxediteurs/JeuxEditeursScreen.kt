package com.example.dev_mobile.ui.jeuxediteurs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.network.AuteurDto
import com.example.dev_mobile.network.ContactEditeurDto
import com.example.dev_mobile.network.CreateEditeurRequest
import com.example.dev_mobile.network.CreateJeuRequest
import com.example.dev_mobile.network.EditeurSummaryDto
import com.example.dev_mobile.network.JeuSummaryDto
import com.example.dev_mobile.session.UserSession

private val AccentBlue = Color(0xFF4A7FC1)
private val TextDark = Color(0xFF1A1A2E)
private val TextGray = Color(0xFF8A8FA3)
private val PageBg = Color(0xFFF4F7FC)
private val CardBg = Color(0xFFFFFFFF)
private val DividerCol = Color(0xFFF0F3F8)
private val ErrorRed = Color(0xFFD32F2F)
private val SuccessGreen = Color(0xFF388E3C)

private val canCreate get() = UserSession.canManage()
private val canEdit get() = UserSession.canManage()
private val canDelete get() = UserSession.canAdmin()

@Composable
fun JeuxEditeursScreen(vm: JeuxEditeursViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.errorMessage?.let { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                val isErr = data.visuals.message.startsWith("Erreur") ||
                    data.visuals.message.startsWith("Serveur") ||
                    data.visuals.message.startsWith("Impossible")
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isErr) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isErr) "❌" else "✅", fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            data.visuals.message,
                            color = if (isErr) ErrorRed else SuccessGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        containerColor = PageBg
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Jeux & Éditeurs", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(
                        "${state.editeurs.size} éditeur(s) • ${state.jeux.size} jeu(x)",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
                if (canCreate) {
                    Button(
                        onClick = {
                            if (state.activeTab == JeuxEditeursTab.EDITEURS) vm.openCreateEditeurDialog()
                            else vm.openCreateJeuDialog()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                    ) {
                        Text(
                            if (state.activeTab == JeuxEditeursTab.EDITEURS) "+ Éditeur" else "+ Jeu",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    label = "Éditeurs",
                    selected = state.activeTab == JeuxEditeursTab.EDITEURS,
                    onClick = { vm.setActiveTab(JeuxEditeursTab.EDITEURS) }
                )
                TabButton(
                    label = "Jeux",
                    selected = state.activeTab == JeuxEditeursTab.JEUX,
                    onClick = { vm.setActiveTab(JeuxEditeursTab.JEUX) }
                )
            }
            HorizontalDivider(color = DividerCol)

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }

                state.activeTab == JeuxEditeursTab.EDITEURS -> EditeursTabContent(
                    state = state,
                    onOpenDetails = vm::openEditeurDetails,
                    onCloseDetails = vm::closeEditeurDetails,
                    onEdit = vm::openEditEditeurDialog,
                    onDelete = vm::deleteEditeur
                )

                else -> JeuxTabContent(
                    state = state,
                    onEdit = vm::openEditJeuDialog,
                    onDelete = vm::deleteJeu
                )
            }
        }
    }

    if (state.showCreateEditeurDialog) {
        EditeurFormDialog(
            title = "Nouvel éditeur",
            initialName = "",
            initialContacts = emptyList(),
            isLoading = state.isSubmitting,
            onDismiss = { vm.closeCreateEditeurDialog() },
            onConfirm = vm::createEditeur
        )
    }

    if (state.showEditEditeurDialog && state.editeurToEdit != null) {
        EditeurFormDialog(
            title = "Modifier l'éditeur",
            initialName = state.editeurToEdit!!.nom,
            initialContacts = state.selectedEditeurContacts,
            isLoading = state.isSubmitting,
            onDismiss = { vm.closeEditEditeurDialog() },
            onConfirm = { req -> vm.updateEditeur(state.editeurToEdit!!.id, req) }
        )
    }

    if (state.showCreateJeuDialog) {
        JeuFormDialog(
            title = "Nouveau jeu",
            initial = null,
            editeurs = state.editeurs,
            isLoading = state.isSubmitting,
            onDismiss = { vm.closeCreateJeuDialog() },
            onConfirm = vm::createJeu
        )
    }

    if (state.showEditJeuDialog && state.jeuToEdit != null) {
        JeuFormDialog(
            title = "Modifier le jeu",
            initial = state.jeuToEdit,
            editeurs = state.editeurs,
            isLoading = state.isSubmitting,
            onDismiss = { vm.closeEditJeuDialog() },
            onConfirm = { req -> vm.updateJeu(state.jeuToEdit!!.id, req) }
        )
    }
}

@Composable
private fun RowScope.TabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Color(0xFFE8F0FB) else Color(0xFFF5F7FC),
        modifier = Modifier.weight(1f)
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = label,
                color = if (selected) AccentBlue else TextGray,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun EditeursTabContent(
    state: JeuxEditeursUiState,
    onOpenDetails: (EditeurSummaryDto) -> Unit,
    onCloseDetails: () -> Unit,
    onEdit: (EditeurSummaryDto) -> Unit,
    onDelete: (Int) -> Unit
) {
    if (state.editeurs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucun éditeur", color = TextGray)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.editeurs, key = { it.id }) { editeur ->
            var showDeleteDialog by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(editeur.nom, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text(
                                "${editeur.nb_jeux} jeu(x) • ${editeur.nb_contacts} contact(s)",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        if (canEdit) {
                            IconButton(onClick = { onEdit(editeur) }) { Text("✏️") }
                        }
                        if (canDelete) {
                            IconButton(onClick = { showDeleteDialog = true }) { Text("🗑", color = ErrorRed) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            if (state.selectedEditeur?.id == editeur.id) onCloseDetails() else onOpenDetails(editeur)
                        }) {
                            Text(if (state.selectedEditeur?.id == editeur.id) "Masquer détails" else "Voir détails")
                        }
                    }

                    if (state.selectedEditeur?.id == editeur.id) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = DividerCol)
                        Spacer(Modifier.height(10.dp))

                        if (state.isLoadingDetails) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Contacts", fontWeight = FontWeight.SemiBold, color = TextDark)
                            if (state.selectedEditeurContacts.isEmpty()) {
                                Text("Aucun contact", fontSize = 12.sp, color = TextGray)
                            } else {
                                state.selectedEditeurContacts.forEach { c ->
                                    Text(
                                        "• ${c.nom}${c.email?.let { " (${it})" } ?: ""}",
                                        fontSize = 12.sp,
                                        color = TextGray
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text("Jeux de l'éditeur", fontWeight = FontWeight.SemiBold, color = TextDark)
                            if (state.selectedEditeurJeux.isEmpty()) {
                                Text("Aucun jeu", fontSize = 12.sp, color = TextGray)
                            } else {
                                state.selectedEditeurJeux.forEach { j ->
                                    Text("• ${j.nom}", fontSize = 12.sp, color = TextGray)
                                }
                            }
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                ConfirmDeleteDialog(
                    title = "Supprimer l'éditeur ?",
                    text = "Vous allez supprimer ${editeur.nom}.",
                    onConfirm = { showDeleteDialog = false; onDelete(editeur.id) },
                    onDismiss = { showDeleteDialog = false }
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun JeuxTabContent(
    state: JeuxEditeursUiState,
    onEdit: (JeuSummaryDto) -> Unit,
    onDelete: (Int) -> Unit
) {
    if (state.jeux.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucun jeu", color = TextGray)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.jeux, key = { it.id }) { jeu ->
            var showDeleteDialog by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(jeu.nom, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text(
                                jeu.editeur_nom ?: "Éditeur inconnu",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        if (canEdit) {
                            IconButton(onClick = { onEdit(jeu) }) { Text("✏️") }
                        }
                        if (canDelete) {
                            IconButton(onClick = { showDeleteDialog = true }) { Text("🗑", color = ErrorRed) }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        listOfNotNull(
                            jeu.type_jeu?.takeIf { it.isNotBlank() },
                            jeu.taille_table?.takeIf { it.isNotBlank() },
                            jeu.duree_moyenne?.let { "${it} min" }
                        ).joinToString(" • ").ifBlank { "Aucune info complémentaire" },
                        fontSize = 12.sp,
                        color = TextGray
                    )

                    val auteursList = jeu.getAuteursOrEmpty()
                    if (auteursList.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Auteurs: ${auteursList.joinToString { a -> "${a.prenom.orEmpty()} ${a.nom}".trim() }}",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }
            }

            if (showDeleteDialog) {
                ConfirmDeleteDialog(
                    title = "Supprimer le jeu ?",
                    text = "Vous allez supprimer ${jeu.nom}.",
                    onConfirm = { showDeleteDialog = false; onDelete(jeu.id) },
                    onDismiss = { showDeleteDialog = false }
                )
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

private class ContactFormState(
    nomInit: String = "",
    emailInit: String = "",
    telInit: String = "",
    roleInit: String = ""
) {
    var nom by mutableStateOf(nomInit)
    var email by mutableStateOf(emailInit)
    var telephone by mutableStateOf(telInit)
    var roleProfession by mutableStateOf(roleInit)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditeurFormDialog(
    title: String,
    initialName: String,
    initialContacts: List<ContactEditeurDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (CreateEditeurRequest) -> Unit
) {
    var nom by remember(initialName) { mutableStateOf(initialName) }
    val contacts = remember(initialContacts) {
        mutableStateListOf<ContactFormState>().apply {
            if (initialContacts.isEmpty()) add(ContactFormState())
            else initialContacts.forEach {
                add(
                    ContactFormState(
                        nomInit = it.nom,
                        emailInit = it.email.orEmpty(),
                        telInit = it.telephone.orEmpty(),
                        roleInit = it.role_profession.orEmpty()
                    )
                )
            }
        }
    }

    val canConfirm = nom.isNotBlank() && !isLoading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom éditeur *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Contacts", color = TextDark, fontWeight = FontWeight.SemiBold)
                contacts.forEachIndexed { index, c ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF))) {
                        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = c.nom,
                                onValueChange = { c.nom = it },
                                label = { Text("Nom contact") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = c.email,
                                    onValueChange = { c.email = it },
                                    label = { Text("Email") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = c.telephone,
                                    onValueChange = { c.telephone = it },
                                    label = { Text("Téléphone") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            OutlinedTextField(
                                value = c.roleProfession,
                                onValueChange = { c.roleProfession = it },
                                label = { Text("Rôle") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = {
                                        if (contacts.size > 1) contacts.removeAt(index)
                                        else {
                                            c.nom = ""
                                            c.email = ""
                                            c.telephone = ""
                                            c.roleProfession = ""
                                        }
                                    }
                                ) {
                                    Text("Supprimer", color = ErrorRed)
                                }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = { contacts.add(ContactFormState()) }) { Text("+ Ajouter contact") }
            }
        },
        confirmButton = {
            Button(
                enabled = canConfirm,
                onClick = {
                    val payloadContacts = contacts
                        .mapNotNull { c ->
                            val trimmed = c.nom.trim()
                            if (trimmed.isBlank()) null
                            else ContactEditeurDto(
                                nom = trimmed,
                                email = c.email.trim().ifBlank { null },
                                telephone = c.telephone.trim().ifBlank { null },
                                role_profession = c.roleProfession.trim().ifBlank { null }
                            )
                        }
                    onConfirm(
                        CreateEditeurRequest(
                            nom = nom.trim(),
                            contacts = payloadContacts.ifEmpty { null }
                        )
                    )
                }
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Enregistrer")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

private class AuteurFormState(
    nomInit: String = "",
    prenomInit: String = ""
) {
    var nom by mutableStateOf(nomInit)
    var prenom by mutableStateOf(prenomInit)
}

@Composable
private fun JeuFormDialog(
    title: String,
    initial: JeuSummaryDto?,
    editeurs: List<EditeurSummaryDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (CreateJeuRequest) -> Unit
) {
    var nom by remember { mutableStateOf(initial?.nom ?: "") }
    var selectedEditeurId by remember {
        mutableStateOf(initial?.editeur_id ?: editeurs.firstOrNull()?.id)
    }
    var editeurMenuExpanded by remember { mutableStateOf(false) }
    var typeJeu by remember { mutableStateOf(initial?.type_jeu ?: "") }
    var ageMini by remember { mutableStateOf(initial?.age_mini?.toString() ?: "") }
    var ageMaxi by remember { mutableStateOf(initial?.age_maxi?.toString() ?: "") }
    var joueursMini by remember { mutableStateOf(initial?.joueurs_mini?.toString() ?: "") }
    var joueursMaxi by remember { mutableStateOf(initial?.joueurs_maxi?.toString() ?: "") }
    var tailleTable by remember { mutableStateOf(initial?.taille_table ?: "") }
    var duree by remember { mutableStateOf(initial?.duree_moyenne?.toString() ?: "") }

    val auteurs = remember(initial) {
        mutableStateListOf<AuteurFormState>().apply {
            val source = initial?.auteurs ?: emptyList()
            if (source.isEmpty()) add(AuteurFormState())
            else source.forEach { add(AuteurFormState(nomInit = it.nom, prenomInit = it.prenom.orEmpty())) }
        }
    }

    val hasAtLeastOneAuteur = auteurs.any { it.nom.trim().isNotBlank() }
    val canConfirm = nom.isNotBlank() && selectedEditeurId != null && hasAtLeastOneAuteur && !isLoading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom du jeu *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = editeurs.firstOrNull { it.id == selectedEditeurId }?.nom ?: "Sélectionner un éditeur",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Éditeur *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { editeurMenuExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp)
                    ) {
                        Text("Choisir un éditeur")
                    }
                    DropdownMenu(
                        expanded = editeurMenuExpanded,
                        onDismissRequest = { editeurMenuExpanded = false }
                    ) {
                        editeurs.forEach { editeur ->
                            DropdownMenuItem(
                                text = { Text(editeur.nom) },
                                onClick = {
                                    selectedEditeurId = editeur.id
                                    editeurMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                if (editeurs.isEmpty()) {
                    Text("Aucun éditeur disponible", color = ErrorRed, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = typeJeu,
                    onValueChange = { typeJeu = it },
                    label = { Text("Type de jeu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageMini,
                        onValueChange = { ageMini = it },
                        label = { Text("Âge min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ageMaxi,
                        onValueChange = { ageMaxi = it },
                        label = { Text("Âge max") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = joueursMini,
                        onValueChange = { joueursMini = it },
                        label = { Text("Joueurs min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = joueursMaxi,
                        onValueChange = { joueursMaxi = it },
                        label = { Text("Joueurs max") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = tailleTable,
                    onValueChange = { tailleTable = it },
                    label = { Text("Taille table") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duree,
                    onValueChange = { duree = it },
                    label = { Text("Durée moyenne (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Auteurs", color = TextDark, fontWeight = FontWeight.SemiBold)
                auteurs.forEachIndexed { index, a ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF))) {
                        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = a.nom,
                                onValueChange = { a.nom = it },
                                label = { Text("Nom auteur") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = a.prenom,
                                onValueChange = { a.prenom = it },
                                label = { Text("Prénom auteur") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    if (auteurs.size > 1) auteurs.removeAt(index)
                                    else {
                                        a.nom = ""
                                        a.prenom = ""
                                    }
                                }) {
                                    Text("Supprimer", color = ErrorRed)
                                }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = { auteurs.add(AuteurFormState()) }) { Text("+ Ajouter auteur") }
            }
        },
        confirmButton = {
            Button(
                enabled = canConfirm,
                onClick = {
                    val editeurId = selectedEditeurId ?: return@Button
                    val payloadAuteurs = auteurs
                        .mapNotNull { a ->
                            val nomTrim = a.nom.trim()
                            if (nomTrim.isBlank()) null
                            else AuteurDto(nom = nomTrim, prenom = a.prenom.trim().ifBlank { null })
                        }
                    onConfirm(
                        CreateJeuRequest(
                            nom = nom.trim(),
                            editeur_id = editeurId,
                            type_jeu = typeJeu.trim().ifBlank { null },
                            age_mini = ageMini.toIntOrNull(),
                            age_maxi = ageMaxi.toIntOrNull(),
                            joueurs_mini = joueursMini.toIntOrNull(),
                            joueurs_maxi = joueursMaxi.toIntOrNull(),
                            taille_table = tailleTable.trim().ifBlank { null },
                            duree_moyenne = duree.toIntOrNull(),
                            auteurs = payloadAuteurs
                        )
                    )
                }
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Enregistrer")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) { Text("Supprimer", color = Color.White) }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
