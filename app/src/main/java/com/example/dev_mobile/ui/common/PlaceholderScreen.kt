
package com.example.dev_mobile.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlaceholderScreen(title: String, icon: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FC)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            Spacer(Modifier.height(8.dp))
            Text("Écran en cours de développement", fontSize = 13.sp, color = Color(0xFF8A8FA3))
        }
    }
}