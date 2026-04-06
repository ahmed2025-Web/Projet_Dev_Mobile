package com.example.dev_mobile.ui.zones

import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
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
private val ErrorRed = Color(0xFFD32F2F)

@Composable
fun ZonesScreen(vm: ZoneViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Scaffold(containerColor = PageBg) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().background(CardBg).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Zones du Plan & Placement", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Gestion matériel & Placement par zone", fontSize = 12.sp, color = TextGray)
                }
                Button(onClick = { vm.openCreateZoneDialog() }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("+ Nouvelle Zone") }
            }
            HorizontalDivider(color = DividerCol)

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Zones", "${state.totalZones}", Modifier.weight(1f))
                        val isOver = state.tauxOccupation > 100
                        StatCard("Occupation", "${state.tauxOccupation}%", Modifier.weight(1.5f), textColor = if(isOver) ErrorRed else AccentBlue)
                        StatCard("Libres", "${state.tablesLibres}", Modifier.weight(1f))
                    }
                }

                if (state.jeuxEnAttente.isNotEmpty()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), modifier = Modifier.fillMaxWidth()) {
                            Text("Jeux en attente de placement : ${state.jeuxEnAttente.size}", Modifier.padding(16.dp), color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Text("Stocks matériel (Physique)", fontWeight = FontWeight.Bold, color = TextDark)
                    state.stocks?.let { StocksSection(it) }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Zones du plan", fontWeight = FontWeight.Bold, color = TextDark)
                        IconButton(onClick = { vm.toggleViewMode() }) { Icon(if (state.isGridView) Icons.Default.List else Icons.Default.Menu, null) }
                    }
                }

                if (state.isGridView) {
                    item {
                        state.zones.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                                row.forEach { zone -> ZoneCard(zone, state.userRole, { vm.openPlaceJeuDialog(zone.id) }, Modifier.weight(1f)) }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(state.zones) { zone -> ZoneCard(zone, state.userRole, { vm.openPlaceJeuDialog(zone.id) }, Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, textColor: Color = AccentBlue) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = CardBg, tonalElevation = 2.dp) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(label, fontSize = 11.sp, color = TextGray)
        }
    }
}

@Composable
fun StocksSection(stocks: StocksMaterielUi) {
    Surface(shape = RoundedCornerShape(12.dp), color = CardBg, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            StockItem("Std", stocks.tablesStdUsed.toInt(), stocks.tablesStdTotal)
            StockItem("Gde", stocks.tablesGdesUsed.toInt(), stocks.tablesGdesTotal)
            StockItem("Mairie", stocks.tablesMairieUsed.toInt(), stocks.tablesMairieTotal)
        }
    }
}

@Composable
fun StockItem(label: String, used: Int, total: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$used/$total", fontWeight = FontWeight.Bold, color = if (used > total) ErrorRed else TextDark)
        Text(label, fontSize = 10.sp, color = TextGray)
    }
}

@Composable
fun ZoneCard(zone: ZonePlanUi, role: String, onPlacerJeu: () -> Unit, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(zone.nom, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Tarif: ${zone.zoneTarifaireNom}", fontSize = 11.sp, color = AccentBlue)
                }
                if (role != "visiteur") {
                    Button(onClick = onPlacerJeu, contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.height(30.dp)) {
                        Text("Placer", fontSize = 11.sp)
                    }
                }
            }
            LinearProgressIndicator(progress = { zone.occupationPercent }, modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 8.dp).clip(CircleShape))
            zone.jeux.forEach { jeu ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(jeu.nomJeu, fontSize = 12.sp)
                    Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(4.dp)) {
                        Text(jeu.typeTable, fontSize = 9.sp, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}
