package com.example.dev_mobile.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.repository.FestivalRepository
import com.example.dev_mobile.utils.NetworkConnectivityObserver
import com.example.dev_mobile.utils.NetworkStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * État de l'interface pour l'écran principal.
 * @param festivalCourantNom Nom du festival à afficher dans la barre de titre.
 * @param festivalCourantId ID du festival sélectionné.
 * @param isOnline Indicateur de connexion internet en temps réel.
 */
data class MainUiState(
    val festivalCourantNom: String = "Chargement...",
    val festivalCourantId: Int? = null,
    val isOnline: Boolean = true
)

/**
 * ViewModel principal de l'application (Global).
 * Utilise AndroidViewModel pour avoir accès au 'Context' (nécessaire pour Room et le réseau).
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Initialisation des composants techniques
    private val db            = AppDatabase.getInstance(application) // Accès à la DB locale Room
    private val connectivity  = NetworkConnectivityObserver(application) // Surveillance du réseau (Wi-Fi/Data)
    
    // Le Repository est "injecté" avec la DB et le surveillant réseau pour gérer le mode Offline
    private val festivalRepository = FestivalRepository(db, connectivity)

    // État interne (Mutable) et état exposé à l'UI (Lecture seule)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        // Lancement d'une coroutine pour surveiller la connexion internet en continu
        viewModelScope.launch {
            connectivity.networkStatus.collect { status ->
                val isOnline = status is NetworkStatus.Available
                
                // Mise à jour de l'état global de connexion
                _uiState.update { it.copy(isOnline = isOnline) }

                // Si la connexion revient, on rafraîchit automatiquement
                // les données pour synchroniser le cache local avec le serveur .
                if (isOnline) {
                    loadFestivalCourant()
                }
            }
        }
        
        // Premier chargement au démarrage de l'application
        loadFestivalCourant()
    }

    /**
     * Récupère les informations du festival actif.
     * Grâce au Repository "Offline-first", cette fonction renverra :
     * 1. Les données du serveur si Online.
     * 2. Les données du cache Room si Offline.
     */
    fun loadFestivalCourant() {
        viewModelScope.launch {
            val festival = festivalRepository.getFestivalCourant()
            if (festival != null) {
                _uiState.update { it.copy(
                    festivalCourantNom = festival.nom,
                    festivalCourantId  = festival.id
                )}
            } else {
                // Cas où aucun festival n'est défini dans la base (serveur ET local vides)
                _uiState.update { it.copy(festivalCourantNom = "Aucun festival") }
            }
        }
    }
}