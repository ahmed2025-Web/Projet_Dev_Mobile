
package com.example.dev_mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dev_mobile.session.UserSession

@Composable
fun DashboardScreen(festivalNom: String) {
    val roleColor = Color(UserSession.getRoleColor())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FC))
            .padding(16.dp)
    ) {
        Text("Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Spacer(Modifier.height(4.dp))
        Text("Bienvenue, ${UserSession.login}", fontSize = 13.sp, color = Color(0xFF8A8FA3))

        Spacer(Modifier.height(16.dp))

        // Badge rôle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(roleColor.copy(alpha = 0.12f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(UserSession.getRoleLabel(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = roleColor)
        }

        Spacer(Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Festival courant", fontSize = 13.sp, color = Color(0xFF8A8FA3))
                Text(festivalNom, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
            }
        }
    }
}