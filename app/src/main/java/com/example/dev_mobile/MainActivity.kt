package com.example.dev_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dev_mobile.network.RetrofitClient
import com.example.dev_mobile.ui.administration.AdministrationScreen
import com.example.dev_mobile.ui.auth.AuthViewModel
import com.example.dev_mobile.ui.auth.LoginScreen
import com.example.dev_mobile.ui.auth.PendingScreen
import com.example.dev_mobile.ui.auth.RegisterScreen
import com.example.dev_mobile.ui.common.PlaceholderScreen
import com.example.dev_mobile.ui.dashboard.DashboardScreen
import com.example.dev_mobile.ui.festivals.FestivalScreen
import com.example.dev_mobile.ui.jeuxediteurs.JeuxEditeursScreen
import com.example.dev_mobile.ui.layout.MainScaffold
import com.example.dev_mobile.ui.main.MainViewModel
import com.example.dev_mobile.ui.navigation.AppDestination
import com.example.dev_mobile.ui.navigation.MenuConfig
import com.example.dev_mobile.ui.public.VuesPubliquesScreen
import com.example.dev_mobile.ui.reservants.ReservantScreen
import com.example.dev_mobile.ui.reservations.ReservationScreen
import com.example.dev_mobile.ui.theme.DevMobileTheme
import com.example.dev_mobile.ui.zones.ZonesScreen

enum class AuthScreen { LOGIN, REGISTER, PENDING, APP }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // INITIALISATION RÉSEAU (OFFLINE FIRST)
        RetrofitClient.init(this)

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
    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var authScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

    //verification unique de session pour que ca verifie pas a chaque petit mouvement de l'ecran
    LaunchedEffect(Unit) {
        authViewModel.checkSession()
    }

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

    val firstDest = remember { MenuConfig.getMenuItems().firstOrNull() ?: AppDestination.Dashboard }
    var currentDestination by remember { mutableStateOf(firstDest) }

    MainScaffold(
        currentDestination  = currentDestination,
        festivalNom         = mainState.festivalCourantNom,
        isOnline            = mainState.isOnline,
        onDestinationChange = { currentDestination = it },
        onLogout            = onLogout
    ) {
        when (currentDestination) {
            AppDestination.Dashboard -> DashboardScreen(
                festivalNom              = mainState.festivalCourantNom,
                isOnline                 = mainState.isOnline,
                onNavigateToReservations = { currentDestination = AppDestination.Reservations },
                onNavigateToFestivals    = { currentDestination = AppDestination.Festivals },
                onNavigateToReservants   = { currentDestination = AppDestination.Reservants }
            )
            AppDestination.Festivals -> FestivalScreen(
                isOnline                 = mainState.isOnline,
                onFestivalCourantChanged = { mainViewModel.loadFestivalCourant() }
            )
            AppDestination.Reservants -> ReservantScreen(
                isOnline                 = mainState.isOnline
            )
            AppDestination.Reservations -> ReservationScreen(
                festivalId               = mainState.festivalCourantId ?: -1,
                isOnline                 = mainState.isOnline
            )

            AppDestination.Administration -> AdministrationScreen()

            AppDestination.JeuxEditeurs   -> JeuxEditeursScreen()
            AppDestination.Zones          -> ZonesScreen()
            AppDestination.VuesPubliques -> VuesPubliquesScreen(
                festivalId  = mainState.festivalCourantId ?: -1,
                festivalNom = mainState.festivalCourantNom,
                isOnline    = mainState.isOnline
            )
        }
    }
}