
package com.example.dev_mobile.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


private val BackgroundBlue = Color(0xFFDDE8F5)
private val LogoCircle     = Color(0xFFCBDAEF)
private val AccentBlue     = Color(0xFF4A7FC1)
private val TextDark       = Color(0xFF1A1A2E)
private val TextGray       = Color(0xFF8A8FA3)
private val BorderGray     = Color(0xFFE2E6EF)
private val CardWhite      = Color(0xFFFFFFFF)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var login    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlue),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(LogoCircle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎮", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                
                Text(
                    text = "FestiJeux",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                
                Text(
                    text = "Gestion de festivals de jeux",
                    fontSize = 13.sp,
                    color = AccentBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
                )

                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Login",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it },
                        placeholder = { Text("votre login", color = TextGray, fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderGray,
                            focusedBorderColor = AccentBlue,
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Mot de passe",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = TextGray, fontSize = 14.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderGray,
                            focusedBorderColor = AccentBlue,
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                
                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                
                Button(
                    onClick = { viewModel.login(login, password) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Se connecter",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pas encore de compte ? ",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                    TextButton(
                        onClick = {
                            viewModel.resetState()
                            onNavigateToRegister()
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "S'inscrire",
                            fontSize = 13.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}