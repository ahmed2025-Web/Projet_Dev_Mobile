package com.example.dev_mobile.ui.editeurs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dev_mobile.ui.editeurs.models.EditeurFormState
import com.example.dev_mobile.ui.editeurs.viewmodel.EditeurViewModel

@Composable
fun EditeurForm(
    formState: EditeurFormState,
    isLoading: Boolean = false,
    isEditing: Boolean = false,
    viewModel: EditeurViewModel,
    onCancel: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Titre
        item {
            Text(
                text = if (isEditing) "Modifier l'éditeur" else "Ajouter un nouvel éditeur",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Champ Nom
        item {
            OutlinedTextField(
                value = formState.nom,
                onValueChange = { viewModel.updateFormField("nom", it) },
                label = { Text("Nom de l'éditeur *") },
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

        // Section Contacts
        item {
            androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contacts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { viewModel.addContact() },
                        enabled = !isLoading,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter contact",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Ajouter")
                    }
                }

                formState.contacts.forEachIndexed { index, contact ->
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Nom du contact
                        OutlinedTextField(
                            value = contact.nom,
                            onValueChange = { viewModel.updateContact(index, "nom", it) },
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            singleLine = true
                        )

                        // Email
                        OutlinedTextField(
                            value = contact.email,
                            onValueChange = { viewModel.updateContact(index, "email", it) },
                            label = { Text("Email") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            enabled = !isLoading,
                            singleLine = true
                        )

                        // Téléphone
                        OutlinedTextField(
                            value = contact.telephone,
                            onValueChange = { viewModel.updateContact(index, "telephone", it) },
                            label = { Text("Téléphone") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            enabled = !isLoading,
                            singleLine = true
                        )

                        // Rôle professionnel
                        OutlinedTextField(
                            value = contact.roleProfession,
                            onValueChange = { viewModel.updateContact(index, "roleProfession", it) },
                            label = { Text("Rôle professionnel") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            enabled = !isLoading,
                            singleLine = true
                        )

                        // Bouton supprimer
                        if (formState.contacts.size > 1) {
                            IconButton(
                                onClick = { viewModel.removeContact(index) },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Supprimer contact",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
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
                    Text(if (isLoading) "Enregistrement..." else if (isEditing) "Modifier l'éditeur" else "Créer l'éditeur")
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
