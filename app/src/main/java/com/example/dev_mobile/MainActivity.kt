package com.example.dev_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.ui.auth.AuthViewModel
import com.example.dev_mobile.ui.auth.HomeScreen
import com.example.dev_mobile.ui.auth.LoginScreen
import com.example.dev_mobile.ui.auth.RegisterScreen
import com.example.dev_mobile.ui.theme.DevMobileTheme

enum class Screen { LOGIN, REGISTER, HOME }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DevMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    val authViewModel: AuthViewModel = viewModel()


    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        when (currentScreen) {
            Screen.LOGIN -> LoginScreen(
                onLoginSuccess = { currentScreen = Screen.HOME },
                onNavigateToRegister = { currentScreen = Screen.REGISTER },
                viewModel = authViewModel
            )
            Screen.REGISTER -> RegisterScreen(
                onRegisterSuccess = { currentScreen = Screen.LOGIN },
                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                viewModel = authViewModel
            )
            Screen.HOME -> HomeScreen(
                onLogout = { currentScreen = Screen.LOGIN },
                viewModel = authViewModel
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    DevMobileTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppContent()
        }
    }
}