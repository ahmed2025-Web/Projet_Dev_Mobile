package com.example.dev_mobile.ui.editeurs.screens

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
import com.example.dev_mobile.ui.editeurs.components.EditeurCard
import com.example.dev_mobile.ui.editeurs.components.EditeurDetails
import com.example.dev_mobile.ui.editeurs.components.EditeurForm
import com.example.dev_mobile.ui.editeurs.viewmodel.EditeurViewModel
import com.example.dev_mobile.ui.editeurs.viewmodel.SortBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditeurListScreen(viewModel: EditeurViewModel) {
    val state by viewModel.state.collectAsState()
    val editeursTries by viewModel.editeursTries.collectAsState()
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
            TopAppBar(title = { Text("Gestion des Éditeurs") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.editeurToEdit != null) {
                // Mode édition/création
                EditeurForm(
                    formState = state.formState,
                    isLoading = state.loadingForm,
                    isEditing = state.editeurToEdit?.id != null && state.editeurToEdit!!.id > 0,
                    viewModel = viewModel,
                    onCancel = { viewModel.cancelEdit() }
                )
            } else if (state.selectedEditeur != null) {
                // Mode détail
                EditeurDetails(
                    editeur = state.selectedEditeur!!,
                    jeux = state.jeuxSelected,
                    contacts = state.contactsSelected,
                    canModify = true,
                    canDelete = true,
                    onEdit = { viewModel.startEditEditeur(it) },
                    onDelete = { viewModel.deleteEditeur(it) }
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
                            text = "Gestion des Éditeurs",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { viewModel.startCreateEditeur() },
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
                    if (state.loadingList && state.editeurs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (!state.loadingList && state.editeurs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucun éditeur trouvé")
                        }
                    } else {
                        // Contrôles de tri
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

                        // Liste des éditeurs
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(editeursTries, key = { it.id }) { editeur ->
                                EditeurCard(
                                    editeur = editeur,
                                    isSelected = state.selectedEditeur?.id == editeur.id,
                                    isEditing = state.editeurToEdit?.id == editeur.id,
                                    canModify = true,
                                    canDelete = true,
                                    onSelect = { viewModel.selectEditeur(it) },
                                    onEdit = { viewModel.startEditEditeur(it) },
                                    onDelete = { viewModel.deleteEditeur(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
