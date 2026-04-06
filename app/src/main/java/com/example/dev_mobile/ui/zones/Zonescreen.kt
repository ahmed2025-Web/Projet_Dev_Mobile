package com.example.dev_mobile.ui.zones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val AccentBlue = Color(0xFF4A7FC1)
private val TextDark = Color(0xFF1A1A2E)
private val TextGray = Color(0xFF8A8FA3)
private val PageBg = Color(0xFFF4F7FC)
private val CardBg = Color(0xFFFFFFFF)
private val DividerCol = Color(0xFFF0F3F8)
private val GoldColor = Color(0xFFF59E0B)

@Composable
fun ZonesScreen(vm: ZoneViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Scaffold(containerColor = PageBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // En-tête (Inspiré de FestivalScreen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Zones du Plan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Placement des jeux et gestion tarifaire", fontSize = 12.sp, color = TextGray)
                }
                Button(
                    onClick = { vm.openCreateZoneDialog() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("+ Nouvelle Zone", fontSize = 13.sp, color = Color.White) }
            }
            HorizontalDivider(color = DividerCol)

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // KPIs
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Zones", "${state.totalZones}", Modifier.weight(1f))
                        StatCard("Tables (espace)", "${state.tablesUtilisees}/${state.tablesTotal}", Modifier.weight(1.5f))
                        StatCard("Jeux", "${state.jeuxPlaces}", Modifier.weight(1f))
                    }
                }

                // Stocks
                item {
                    Text("Stocks matériel", fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(8.dp))
                    state.stocks?.let { StocksSection(it) }
                }

                // Zones (Inspiré de FestivalCard)
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Zones du plan", fontWeight = FontWeight.Bold, color = TextDark)
                        IconButton(onClick = { vm.toggleViewMode() }) {
                            Icon(if (state.isGridView) Icons.Default.List else Icons.Default.Menu, null)
                        }
                    }
                }

                if (state.isGridView) {
                    item {
                        state.zones.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                                row.forEach { zone -> ZoneCard(zone, Modifier.weight(1f)) }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                } else {
                    items(state.zones) { zone ->
                        ZoneCard(zone, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = CardBg, tonalElevation = 2.dp) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
            Text(label, fontSize = 11.sp, color = TextGray)
        }
    }
}

@Composable
fun StocksSection(stocks: StocksMaterielUi) {
    Surface(shape = RoundedCornerShape(12.dp), color = CardBg, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            StockItem("Std", "${stocks.tablesStdUsed.toInt()}/${stocks.tablesStdTotal}")
            StockItem("Gde", "${stocks.tablesGdesUsed.toInt()}/${stocks.tablesGdesTotal}")
            StockItem("Mairie", "${stocks.tablesMairieUsed.toInt()}/${stocks.tablesMairieTotal}")
        }
    }
}

@Composable
fun StockItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 10.sp, color = TextGray)
    }
}

@Composable
fun ZoneCard(zone: ZonePlanUi, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(zone.nom, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Tarif: ${zone.zoneTarifaireNom}", fontSize = 11.sp, color = TextGray)
            
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { zone.occupationPercent }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape))
            
            Spacer(Modifier.height(12.dp))
            zone.jeux.forEach { jeu ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(jeu.nomJeu, fontSize = 12.sp)
                    Text("${jeu.nbTables} table(s)", fontSize = 12.sp, color = TextGray)
                }
            }
        }
    }
}
