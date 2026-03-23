package com.example.dev_mobile.ui.jeux.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.dev_mobile.network.Jeu

@Composable
fun JeuDetails(
    jeu: Jeu,
    canModify: Boolean = false,
    canDelete: Boolean = false,
    onEdit: (Jeu) -> Unit = {},
    onDelete: (Jeu) -> Unit = {}
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
                    text = jeu.nom,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canModify) {
                        Button(
                            onClick = { onEdit(jeu) },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Modifier")
                        }
                    }
                    if (canDelete) {
                        Button(
                            onClick = { onDelete(jeu) },
                            modifier = Modifier.padding(4.dp)
                        ) {
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

        // Éditeur
        if (!jeu.editeurNom.isNullOrBlank()) {
            item {
                Column {
                    Text(
                        text = "Éditeur",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = jeu.editeurNom,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Type de jeu
        if (!jeu.type_jeu.isNullOrBlank()) {
            item {
                Column {
                    Text(
                        text = "Type de jeu",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = jeu.type_jeu,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Âge
        if (jeu.age_mini != null || jeu.age_maxi != null) {
            item {
                Column {
                    Text(
                        text = "Âge recommandé",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildString {
                            if (jeu.age_mini != null) append("À partir de ${jeu.age_mini} ans")
                            if (jeu.age_maxi != null) {
                                if (jeu.age_mini != null) append(" jusqu'à ${jeu.age_maxi} ans")
                                else append("Jusqu'à ${jeu.age_maxi} ans")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Joueurs
        if (jeu.joueurs_mini != null || jeu.joueurs_maxi != null) {
            item {
                Column {
                    Text(
                        text = "Nombre de joueurs",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildString {
                            if (jeu.joueurs_mini != null) append("Minimum ${jeu.joueurs_mini}")
                            if (jeu.joueurs_maxi != null) {
                                if (jeu.joueurs_mini != null) append(" à ${jeu.joueurs_maxi} joueurs")
                                else append("Maximum ${jeu.joueurs_maxi} joueurs")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Taille de table
        if (!jeu.taille_table.isNullOrBlank()) {
            item {
                Column {
                    Text(
                        text = "Taille de table",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = jeu.taille_table,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Durée moyenne
        if (jeu.duree_moyenne != null) {
            item {
                Column {
                    Text(
                        text = "Durée moyenne",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${jeu.duree_moyenne} minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Auteurs
        if (jeu.auteurs.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Auteurs",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        jeu.auteurs.forEach { auteur ->
                            Text(
                                text = buildString {
                                    if (!auteur.prenom.isNullOrBlank()) append("${auteur.prenom} ")
                                    append(auteur.nom)
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
