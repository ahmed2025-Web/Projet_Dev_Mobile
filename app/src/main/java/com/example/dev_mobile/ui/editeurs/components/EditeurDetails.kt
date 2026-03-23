package com.example.dev_mobile.ui.editeurs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dev_mobile.network.Contact
import com.example.dev_mobile.network.Editeur
import com.example.dev_mobile.network.Jeu

@Composable
fun EditeurDetails(
    editeur: Editeur,
    jeux: List<Jeu> = emptyList(),
    contacts: List<Contact> = emptyList(),
    canModify: Boolean = false,
    canDelete: Boolean = false,
    onEdit: (Editeur) -> Unit = {},
    onDelete: (Editeur) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // En-tête avec actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = editeur.nom,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canModify) {
                        Button(onClick = { onEdit(editeur) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Modifier")
                        }
                    }
                    if (canDelete) {
                        Button(onClick = { onDelete(editeur) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Supprimer")
                        }
                    }
                }
            }
        }

        // Statistiques
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Jeux",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${editeur.nb_jeux}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        text = "Contacts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${editeur.nb_contacts}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Jeux de l'éditeur
        if (jeux.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Jeux publiés",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(jeux) { jeu ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = jeu.nom,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!jeu.type_jeu.isNullOrBlank()) {
                        Text(
                            text = "Type: ${jeu.type_jeu}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Contacts de l'éditeur
        if (contacts.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Contacts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(contacts) { contact ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = contact.nom,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!contact.email.isNullOrBlank()) {
                        Text(
                            text = "Email: ${contact.email}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (!contact.telephone.isNullOrBlank()) {
                        Text(
                            text = "Tél: ${contact.telephone}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (!contact.role_profession.isNullOrBlank()) {
                        Text(
                            text = "Rôle: ${contact.role_profession}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
