
package com.example.dev_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.ui.auth.AuthViewModel
import com.example.dev_mobile.ui.auth.LoginScreen
import com.example.dev_mobile.ui.auth.PendingScreen
import com.example.dev_mobile.ui.auth.RegisterScreen
import com.example.dev_mobile.ui.common.PlaceholderScreen
import com.example.dev_mobile.ui.dashboard.DashboardScreen
import com.example.dev_mobile.ui.layout.MainScaffold
import com.example.dev_mobile.ui.main.MainViewModel
import com.example.dev_mobile.ui.navigation.AppDestination
import com.example.dev_mobile.ui.navigation.MenuConfig
import com.example.dev_mobile.ui.reservants.ReservantScreen
import com.example.dev_mobile.ui.theme.DevMobileTheme

enum class AuthScreen { LOGIN, REGISTER, PENDING, APP }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DevMobileTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var authScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess, uiState.isPending) {
        when {
            uiState.isSuccess -> authScreen = AuthScreen.APP
            uiState.isPending -> authScreen = AuthScreen.PENDING
        }
    }

    when (authScreen) {
        AuthScreen.LOGIN -> LoginScreen(
            onLoginSuccess       = { authScreen = AuthScreen.APP },
            onNavigateToRegister = { authScreen = AuthScreen.REGISTER },
            viewModel            = authViewModel
        )
        AuthScreen.REGISTER -> RegisterScreen(
            onRegisterSuccess = { authScreen = AuthScreen.LOGIN },
            onNavigateToLogin = { authScreen = AuthScreen.LOGIN },
            viewModel         = authViewModel
        )
        AuthScreen.PENDING -> PendingScreen(
            onLogout  = { authScreen = AuthScreen.LOGIN },
            viewModel = authViewModel
        )
        AuthScreen.APP -> MainApp(
            onLogout = {
                authViewModel.logout()
                authScreen = AuthScreen.LOGIN
            }
        )
    }
}

@Composable
fun MainApp(onLogout: () -> Unit) {
    val mainViewModel: MainViewModel = viewModel()
    val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()

    // Destination initiale = premier item du menu selon le rôle
    val firstDest = remember { MenuConfig.getMenuItems().firstOrNull() ?: AppDestination.Dashboard }
    var currentDestination by remember { mutableStateOf(firstDest) }

    MainScaffold(
        currentDestination  = currentDestination,
        festivalNom         = mainState.festivalCourantNom,
        onDestinationChange = { currentDestination = it },
        onLogout            = onLogout
    ) {
        when (currentDestination) {
            AppDestination.Dashboard     -> DashboardScreen(festivalNom = mainState.festivalCourantNom)
            AppDestination.Reservants    -> ReservantScreen()
            AppDestination.Festivals     -> PlaceholderScreen("Festivals", "📅")
            AppDestination.Reservations  -> PlaceholderScreen("Réservations", "📋")
            AppDestination.JeuxEditeurs  -> PlaceholderScreen("Jeux & Éditeurs", "🎮")
            AppDestination.Zones         -> PlaceholderScreen("Zones", "🗺️")
            AppDestination.Facturation   -> PlaceholderScreen("Facturation", "💰")
            AppDestination.Recapitulatif -> PlaceholderScreen("Récapitulatif", "📊")
            AppDestination.Administration -> PlaceholderScreen("Administration", "⚙️")
            AppDestination.VuesPubliques -> PlaceholderScreen("Vues publiques", "👁️")
        }
    }
}