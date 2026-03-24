package com.example.dev_mobile.ui.administration

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.network.PendingUserDto
import com.example.dev_mobile.network.FullUserDto
import com.example.dev_mobile.session.UserSession

// ── Palette (cohérente avec le reste de l'app) ─────────────────────────────────
private val AccentBlue = Color(0xFF4A7FC1)
private val TextDark   = Color(0xFF1A1A2E)
private val TextGray   = Color(0xFF8A8FA3)
private val PageBg     = Color(0xFFF4F7FC)
private val CardBg     = Color(0xFFFFFFFF)
private val DividerCol = Color(0xFFF0F3F8)
private val BorderGray = Color(0xFFE2E6EF)
private val ErrorRed   = Color(0xFFD32F2F)
private val SuccessGreen = Color(0xFF388E3C)

// Rôles disponibles (sauf 'user' qui est réservé aux comptes en attente)
val ALL_ROLES = listOf("admin", "super organisateur", "organisateur", "benevole", "visiteur")
val ALL_ROLES_INCLUDING_USER = listOf("admin", "super organisateur", "organisateur", "benevole", "visiteur", "user")

fun roleColor(role: String): Color = when (role) {
    "admin"              -> Color(0xFF7B1FA2)
    "super organisateur" -> Color(0xFF1565C0)
    "organisateur"       -> Color(0xFF1976D2)
    "benevole"           -> Color(0xFF388E3C)
    "visiteur"           -> Color(0xFF0288D1)
    "user"               -> Color(0xFFE65100)
    else                 -> Color(0xFF757575)
}

fun roleLabel(role: String): String = when (role) {
    "admin"              -> "Administrateur"
    "super organisateur" -> "Super Organisateur"
    "organisateur"       -> "Organisateur"
    "benevole"           -> "Bénévole"
    "visiteur"           -> "Visiteur"
    "user"               -> "En attente"
    else                 -> role
}

// ── Écran principal ────────────────────────────────────────────────────────────
@Composable
fun AdministrationScreen(vm: AdministrationViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.errorMessage?.let  { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                val isErr = data.visuals.message.startsWith("Erreur") || data.visuals.message.startsWith("Serveur") || data.visuals.message.startsWith("Impossible")
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isErr) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
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
                    Text("Administration", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Gestion des utilisateurs", fontSize = 12.sp, color = TextGray)
                }
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                ) { Text("+ Nouveau", fontSize = 13.sp, color = Color.White) }
            }

            // ── Tabs ─────────────────────────────────────────────────────────
            val tabTitles = listOf(
                "Tous (${state.users.size})",
                "⏳ En attente (${state.pendingUsers.size})"
            )
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = CardBg,
                contentColor = AccentBlue,
                divider = { HorizontalDivider(color = DividerCol) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick  = { vm.setTab(index) },
                        text     = {
                            Text(
                                title,
                                fontSize = 13.sp,
                                fontWeight = if (state.selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (state.selectedTab == index) AccentBlue else TextGray
                            )
                        }
                    )
                }
            }

            // ── Contenu tab ──────────────────────────────────────────────────
            when (state.selectedTab) {
                0 -> UsersTab(
                    users     = state.users,
                    isLoading = state.isLoadingUsers,
                    onRefresh = { vm.loadUsers() },
                    onDelete     = { userId -> vm.deleteUser(userId) },
                    onRoleChange = { userId, role -> vm.changeRole(userId, role) }
                )
                1 -> PendingTab(
                    pending   = state.pendingUsers,
                    isLoading = state.isLoadingPending,
                    onRefresh = { vm.loadPendingUsers() },
                    onValidate = { userId, role -> vm.validateAccount(userId, role) },
                    isSubmitting = state.isSubmitting
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            isLoading = state.isSubmitting,
            onDismiss = { showCreateDialog = false },
            onConfirm = { nom, prenom, email, login, password, role ->
                vm.createUser(nom, prenom, email, login, password, role)
                showCreateDialog = false
            }
        )
    }
}

// ── Tab : liste de tous les utilisateurs ──────────────────────────────────────
@Composable
private fun UsersTab(
    users: List<FullUserDto>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDelete: (Int) -> Unit,
    onRoleChange: (Int, String) -> Unit
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        users.isEmpty() -> EmptyState("Aucun utilisateur", onRefresh)
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users, key = { it.id }) { user ->
                UserCard(
                    user = user,
                    onDelete  = { onDelete(user.id) },
                    onRoleChange = { newRole -> onRoleChange(user.id, newRole) }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Card utilisateur ───────────────────────────────────────────────────────────
@Composable
private fun UserCard(
    user: FullUserDto,
    onDelete: () -> Unit,
    onRoleChange: (String) -> Unit
) {
    var showRoleMenu  by remember { mutableStateOf(false) }
    var showDeleteDlg by remember { mutableStateOf(false) }
    val isSelf = user.login == UserSession.login

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(roleColor(user.role).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (user.nom?.take(1) ?: user.login.take(1)).uppercase(),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = roleColor(user.role)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${user.nom ?: ""} ${user.prenom ?: ""}".trim().ifEmpty { user.login },
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark
                    )
                    Text("@${user.login}", fontSize = 12.sp, color = TextGray)
                    Text(user.email, fontSize = 11.sp, color = AccentBlue)
                }
                // Badge rôle cliquable
                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = roleColor(user.role).copy(alpha = 0.12f),
                        modifier = Modifier.clickable(enabled = !isSelf) { showRoleMenu = true }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(roleLabel(user.role), fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold, color = roleColor(user.role))
                            if (!isSelf) {
                                Spacer(Modifier.width(4.dp))
                                Text("▾", fontSize = 10.sp, color = roleColor(user.role))
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        ALL_ROLES.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier.size(10.dp).clip(CircleShape)
                                                .background(roleColor(role))
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(roleLabel(role), fontSize = 13.sp,
                                            fontWeight = if (role == user.role) FontWeight.Bold else FontWeight.Normal)
                                    }
                                },
                                onClick = {
                                    showRoleMenu = false
                                    if (role != user.role) onRoleChange(role)
                                }
                            )
                        }
                    }
                }
            }

            // Footer : date + bouton supprimer
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                user.created_at?.take(10)?.let {
                    Text("Créé le $it", fontSize = 11.sp, color = TextGray)
                }
                if (!isSelf) {
                    TextButton(
                        onClick = { showDeleteDlg = true },
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("🗑", fontSize = 16.sp, color = ErrorRed) }
                } else {
                    Text("(vous)", fontSize = 11.sp, color = TextGray)
                }
            }
        }
    }

    if (showDeleteDlg) {
        ConfirmDeleteUserDialog(
            login     = user.login,
            onConfirm = { onDelete(); showDeleteDlg = false },
            onDismiss = { showDeleteDlg = false }
        )
    }
}

// ── Tab : comptes en attente ───────────────────────────────────────────────────
@Composable
private fun PendingTab(
    pending: List<PendingUserDto>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onValidate: (Int, String) -> Unit,
    isSubmitting: Boolean
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        pending.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✅", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Aucun compte en attente", color = TextGray, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRefresh) { Text("Rafraîchir", color = AccentBlue) }
            }
        }
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(pending, key = { it.id }) { user ->
                PendingUserCard(
                    user        = user,
                    isSubmitting = isSubmitting,
                    onValidate  = { role -> onValidate(user.id, role) }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Card compte en attente ─────────────────────────────────────────────────────
@Composable
private fun PendingUserCard(
    user: PendingUserDto,
    isSubmitting: Boolean,
    onValidate: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("organisateur") }
    var showRoleDropdown by remember { mutableStateOf(false) }

    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) { Text("⏳", fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${user.nom ?: ""} ${user.prenom ?: ""}".trim().ifEmpty { user.login },
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark
                    )
                    Text("@${user.login}", fontSize = 12.sp, color = TextGray)
                    Text(user.email, fontSize = 11.sp, color = AccentBlue)
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Text("En attente", fontSize = 11.sp, color = Color(0xFFE65100),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(10.dp))

            // Sélection du rôle à attribuer
            Text("Attribuer le rôle :", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))

            Box {
                OutlinedButton(
                    onClick = { showRoleDropdown = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(10.dp).clip(CircleShape)
                                    .background(roleColor(selectedRole))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(roleLabel(selectedRole), fontSize = 13.sp, color = TextDark)
                        }
                        Text("▾", fontSize = 12.sp, color = TextGray)
                    }
                }
                DropdownMenu(
                    expanded = showRoleDropdown,
                    onDismissRequest = { showRoleDropdown = false }
                ) {
                    ALL_ROLES.forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(10.dp).clip(CircleShape).background(roleColor(role)))
                                    Spacer(Modifier.width(8.dp))
                                    Text(roleLabel(role), fontSize = 13.sp,
                                        fontWeight = if (role == selectedRole) FontWeight.Bold else FontWeight.Normal)
                                }
                            },
                            onClick = { selectedRole = role; showRoleDropdown = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick  = { onValidate(selectedRole) },
                enabled  = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("✓ Valider le compte", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Dialog création utilisateur ────────────────────────────────────────────────
@Composable
private fun CreateUserDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nom: String, prenom: String, email: String, login: String, password: String, role: String) -> Unit
) {
    var nom       by remember { mutableStateOf("") }
    var prenom    by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var login     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var role      by remember { mutableStateOf("organisateur") }
    var showRoleMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel utilisateur", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nom, onValueChange = { nom = it },
                        label = { Text("Nom") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prenom, onValueChange = { prenom = it },
                        label = { Text("Prénom") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = login, onValueChange = { login = it },
                    label = { Text("Login *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Mot de passe *") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Sélecteur de rôle
                Text("Rôle :", fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.Medium)
                Box {
                    OutlinedButton(
                        onClick = { showRoleMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(roleColor(role)))
                                Spacer(Modifier.width(8.dp))
                                Text(roleLabel(role), fontSize = 13.sp, color = TextDark)
                            }
                            Text("▾", fontSize = 12.sp, color = TextGray)
                        }
                    }
                    DropdownMenu(expanded = showRoleMenu, onDismissRequest = { showRoleMenu = false }) {
                        ALL_ROLES_INCLUDING_USER.forEach { r ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(10.dp).clip(CircleShape).background(roleColor(r)))
                                        Spacer(Modifier.width(8.dp))
                                        Text(roleLabel(r), fontSize = 13.sp)
                                    }
                                },
                                onClick = { role = r; showRoleMenu = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onConfirm(nom, prenom, email, login, password, role) },
                enabled  = email.isNotBlank() && login.isNotBlank() && password.isNotBlank() && !isLoading,
                colors   = ButtonDefaults.buttonColors(containerColor = TextDark)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Créer", color = Color.White)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ── Dialog confirmation suppression ───────────────────────────────────────────
@Composable
private fun ConfirmDeleteUserDialog(
    login: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer cet utilisateur ?", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vous allez supprimer le compte @$login.")
                Card(shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Text("⚠ Cette action est irréversible.",
                        fontSize = 12.sp, color = ErrorRed,
                        modifier = Modifier.padding(10.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                Text("Supprimer", color = Color.White)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ── État vide générique ────────────────────────────────────────────────────────
@Composable
private fun EmptyState(message: String, onRefresh: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👥", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(message, color = TextGray, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRefresh) { Text("Rafraîchir", color = AccentBlue) }
        }
    }
}