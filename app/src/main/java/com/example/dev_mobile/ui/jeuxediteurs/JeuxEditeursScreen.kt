package com.example.dev_mobile.ui.jeuxediteurs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.network.JeuDto

private val PageBg = Color(0xFFF4F7FC)
private val TextDark = Color(0xFF1A1A2E)
private val TextGray = Color(0xFF8A8FA3)
private val CardBg = Color(0xFFFFFFFF)
private val CardBorder = Color(0xFFE6EAF2)
private val Accent = Color(0xFF8B5CF6)

@Composable
fun JeuxExpeditersScreen(viewModel: JeuxEditeursViewModel = viewModel()) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Jeux&Editeurs",
            fontSize = 30.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Catalogue des jeux de societe",
            fontSize = 14.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        val queryValue = (uiState as? JeuxEditeursUiState.Success)?.query.orEmpty()
        OutlinedTextField(
            value = queryValue,
            onValueChange = viewModel::updateQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Rechercher un jeu, un auteur, un editeur...") },
            leadingIcon = { Text("🔍", fontSize = 16.sp) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        when (uiState) {
            is JeuxEditeursUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is JeuxEditeursUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(onClick = viewModel::loadJeux) {
                            Text("Reessayer")
                        }
                    }
                }
            }

            is JeuxEditeursUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredJeux, key = { it.id }) { jeu ->
                        JeuCard(jeu = jeu)
                    }
                }
            }
        }
    }
}

@Composable
private fun JeuCard(jeu: JeuDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF2EAFE), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎮", fontSize = 14.sp, color = Accent)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val nomJeu = jeu.nom?.takeIf { it.isNotBlank() } ?: "Non renseigne"
                    val nomEditeur = jeu.publisherName?.takeIf { it.isNotBlank() } ?: "Non renseigne"
                    Text(
                        text = nomJeu,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = nomEditeur,
                        fontSize = 12.sp,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val auteursList = jeu.auteurs.orEmpty()
            val auteurs = if (auteursList.isEmpty()) {
                "Non disponible"
            } else {
                auteursList.joinToString(", ") { auteur ->
                    listOfNotNull(auteur.prenom, auteur.nom).joinToString(" ").trim()
                }
            }

            Text(
                text = "Auteur(s): $auteurs",
                fontSize = 12.sp,
                color = Accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val typeJeu = jeu.gameType ?: "Non renseigne"
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type: $typeJeu",
                fontSize = 12.sp,
                color = Accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val ageLabel = when {
                jeu.minAge != null -> "${jeu.minAge}+ ans"
                jeu.maxAge != null -> "${jeu.maxAge} ans"
                else -> "Non renseigne"
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Age: $ageLabel",
                fontSize = 12.sp,
                color = Accent
            )
        }
    }
}
