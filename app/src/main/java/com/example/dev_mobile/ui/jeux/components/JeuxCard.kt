package com.example.dev_mobile.ui.jeux.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dev_mobile.network.Jeu

@Composable
fun JeuxCard(
    jeu: Jeu,
    isSelected: Boolean = false,
    isEditing: Boolean = false,
    canModify: Boolean = false,
    canDelete: Boolean = false,
    onSelect: (Jeu) -> Unit = {},
    onEdit: (Jeu) -> Unit = {},
    onDelete: (Jeu) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onSelect(jeu) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected || isEditing) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Titre du jeu
            Text(
                text = jeu.nom,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected || isEditing) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            // Éditeur
            if (!jeu.editeur_nom.isNullOrBlank()) {
                Text(
                    text = "Éditeur: ${jeu.editeur_nom}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected || isEditing) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Type de jeu
            if (!jeu.type_jeu.isNullOrBlank()) {
                Text(
                    text = "Type: ${jeu.type_jeu}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected || isEditing) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Âge
            if (jeu.age_mini != null || jeu.age_maxi != null) {
                val ageRange = buildString {
                    if (jeu.age_mini != null) append("${jeu.age_mini}")
                    if (jeu.age_maxi != null) append(" - ${jeu.age_maxi}")
                }
                if (ageRange.isNotBlank()) {
                    Text(
                        text = "Âge: $ageRange+",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected || isEditing) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Joueurs
            if (jeu.joueurs_mini != null || jeu.joueurs_maxi != null) {
                val playersRange = buildString {
                    if (jeu.joueurs_mini != null) append("${jeu.joueurs_mini}")
                    if (jeu.joueurs_maxi != null) append(" - ${jeu.joueurs_maxi}")
                }
                if (playersRange.isNotBlank()) {
                    Text(
                        text = "Joueurs: $playersRange",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected || isEditing) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Durée
            if (jeu.duree_moyenne != null) {
                Text(
                    text = "Durée: ${jeu.duree_moyenne} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected || isEditing) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Auteurs
            if (jeu.auteurs.isNotEmpty()) {
                Text(
                    text = "Auteurs: ${jeu.auteurs.joinToString(", ") { "${it.prenom ?: ""} ${it.nom}".trim() }}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected || isEditing) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Actions (si permis)
            if (canModify || canDelete) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canModify) {
                        IconButton(
                            onClick = { onEdit(jeu) },
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (canDelete) {
                        IconButton(
                            onClick = { onDelete(jeu) },
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
