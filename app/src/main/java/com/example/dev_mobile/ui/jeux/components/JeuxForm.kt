package com.example.dev_mobile.ui.jeux.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dev_mobile.network.Editeur
import com.example.dev_mobile.ui.jeux.models.JeuFormState
import com.example.dev_mobile.ui.jeux.viewmodel.JeuxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuxForm(
    formState: JeuFormState,
    editeurs: List<Editeur>,
    isLoading: Boolean = false,
    isEditing: Boolean = false,
    viewModel: JeuxViewModel,
    onCancel: () -> Unit = {}
) {
    var editeurExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Titre
        item {
            Text(
                text = if (isEditing) "Modifier le jeu" else "Ajouter un nouveau jeu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Champ Nom
        item {
            OutlinedTextField(
                value = formState.nom,
                onValueChange = { viewModel.updateFormField("nom", it) },
                label = { Text("Nom du jeu *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                isError = formState.errors.containsKey("nom")
            )
            if (formState.errors.containsKey("nom")) {
                Text(
                    text = formState.errors["nom"] ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }

        // Dropdown Éditeur
        item {
            ExposedDropdownMenuBox(
                expanded = editeurExpanded,
                onExpandedChange = { editeurExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = editeurs.find { it.id == formState.editeurId }?.nom ?: "Sélectionnez un éditeur",
                    onValueChange = {},
                    label = { Text("Éditeur *") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editeurExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = !isLoading,
                    isError = formState.errors.containsKey("editeurId")
                )

                ExposedDropdownMenu(
                    expanded = editeurExpanded,
                    onDismissRequest = { editeurExpanded = false }
                ) {
                    editeurs.forEach { editeur ->
                        DropdownMenuItem(
                            text = { Text(editeur.nom) },
                            onClick = {
                                viewModel.updateFormField("editeurId", editeur.id.toString())
                                editeurExpanded = false
                            }
                        )
                    }
                }
            }
            if (formState.errors.containsKey("editeurId")) {
                Text(
                    text = formState.errors["editeurId"] ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }

        // Type de jeu
        item {
            OutlinedTextField(
                value = formState.typeJeu,
                onValueChange = { viewModel.updateFormField("typeJeu", it) },
                label = { Text("Type de jeu") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )
        }

        // Âge minimum et maximum (row)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formState.ageMini,
                    onValueChange = { viewModel.updateFormField("ageMini", it) },
                    label = { Text("Âge min") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    singleLine = true
                )
                OutlinedTextField(
                    value = formState.ageMaxi,
                    onValueChange = { viewModel.updateFormField("ageMaxi", it) },
                    label = { Text("Âge max") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    singleLine = true
                )
            }
        }

        // Joueurs minimum et maximum (row)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formState.joueursMin,
                    onValueChange = { viewModel.updateFormField("joueursMin", it) },
                    label = { Text("Joueurs min") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    singleLine = true
                )
                OutlinedTextField(
                    value = formState.joueursMax,
                    onValueChange = { viewModel.updateFormField("joueursMax", it) },
                    label = { Text("Joueurs max") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    singleLine = true
                )
            }
        }

        // Taille table
        item {
            OutlinedTextField(
                value = formState.tailleTable,
                onValueChange = { viewModel.updateFormField("tailleTable", it) },
                label = { Text("Taille de table") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )
        }

        // Durée moyenne
        item {
            OutlinedTextField(
                value = formState.dureeMoyenne,
                onValueChange = { viewModel.updateFormField("dureeMoyenne", it) },
                label = { Text("Durée moyenne (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )
        }

        // Section Auteurs
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auteurs *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { viewModel.addAuteur() },
                        enabled = !isLoading,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter auteur",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Ajouter")
                    }
                }

                if (formState.auteurs.isEmpty()) {
                    Text(
                        text = "Au moins un auteur est obligatoire",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                formState.auteurs.forEachIndexed { index, auteur ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = auteur.nom,
                            onValueChange = { viewModel.updateAuteur(index, "nom", it) },
                            label = { Text("Nom") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = auteur.prenom,
                            onValueChange = { viewModel.updateAuteur(index, "prenom", it) },
                            label = { Text("Prénom") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            singleLine = true
                        )
                        IconButton(
                            onClick = { viewModel.removeAuteur(index) },
                            enabled = !isLoading && formState.auteurs.size > 1,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Supprimer auteur",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Boutons d'action
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.submitForm() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Enregistrement..." else if (isEditing) "Modifier le jeu" else "Créer le jeu")
                }
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Annuler")
                }
            }
        }
    }
}
