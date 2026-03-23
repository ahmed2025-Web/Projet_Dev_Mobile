package com.example.dev_mobile.ui.jeux.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dev_mobile.ui.jeux.components.JeuxCard
import com.example.dev_mobile.ui.jeux.components.JeuxForm
import com.example.dev_mobile.ui.jeux.components.JeuDetails
import com.example.dev_mobile.ui.jeux.viewmodel.JeuxViewModel
import com.example.dev_mobile.ui.jeux.viewmodel.SortBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuxListScreen(viewModel: JeuxViewModel) {
    val state by viewModel.state.collectAsState()
    val jeuxTries by viewModel.jeuxTries.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var sortExpanded by remember { mutableStateOf(false) }

    // Afficher les messages de succès
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestion des Jeux") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.jeuToEdit != null) {
                // Mode édition/création
                JeuxForm(
                    formState = state.formState,
                    editeurs = state.editeurs,
                    isLoading = state.loadingForm,
                    isEditing = state.jeuToEdit?.id != null && state.jeuToEdit!!.id > 0,
                    viewModel = viewModel,
                    onCancel = { viewModel.cancelEdit() }
                )
            } else if (state.selectedJeu != null) {
                // Mode détail
                JeuDetails(
                    jeu = state.selectedJeu!!,
                    canModify = true,
                    canDelete = true,
                    onEdit = { viewModel.startEditJeu(it) },
                    onDelete = { viewModel.deleteJeu(it) }
                )
            } else {
                // Mode liste
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // En-tête avec bouton ajouter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gestion des Jeux",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { viewModel.startCreateJeu() },
                            enabled = !state.loadingList
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Ajouter")
                        }
                    }

                    // Message d'erreur
                    if (state.errorList != null) {
                        Text(
                            text = state.errorList!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Chargement initial
                    if (state.loadingList && state.jeux.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (!state.loadingList && state.jeux.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucun jeu trouvé")
                        }
                    } else {
                        // Contrôles de tri
                        var sortExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sortExpanded,
                            onExpandedChange = { sortExpanded = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            OutlinedTextField(
                                value = when (state.sortBy) {
                                    SortBy.NOM -> "Nom (A-Z)"
                                    SortBy.NOM_DESC -> "Nom (Z-A)"
                                    SortBy.RECENT -> "Plus récents"
                                },
                                onValueChange = {},
                                label = { Text("Trier par") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nom (A-Z)") },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.NOM)
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Nom (Z-A)") },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.NOM_DESC)
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Plus récents") },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.RECENT)
                                        sortExpanded = false
                                    }
                                )
                            }
                        }

                        // Liste des jeux
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(jeuxTries, key = { it.id }) { jeu ->
                                JeuxCard(
                                    jeu = jeu,
                                    isSelected = state.selectedJeu?.id == jeu.id,
                                    isEditing = state.jeuToEdit?.id == jeu.id,
                                    canModify = true,
                                    canDelete = true,
                                    onSelect = { viewModel.selectJeu(it) },
                                    onEdit = { viewModel.startEditJeu(it) },
                                    onDelete = { viewModel.deleteJeu(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
