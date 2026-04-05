package com.example.dev_mobile.ui.public

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.EditeurPublicDto
import com.example.dev_mobile.network.JeuPublicDto
import com.example.dev_mobile.network.ZonePlanPublicDto
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.PublicRepository
import com.example.dev_mobile.utils.NetworkConnectivityObserver
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Éditeur enrichi avec la liste de ses jeux
data class EditeurAvecJeux(
    val editeurId: Int,
    val editeurNom: String,
    val nbJeuxPresentes: Int,
    val jeux: List<JeuPublicDto>
)

data class VuesPubliquesUiState(
    val isLoading: Boolean = false,
    val festivalNom: String = "",
    val festivalDateDebut: String? = null,
    val festivalDateFin: String? = null,

    // Tab Jeux
    val jeux: List<JeuPublicDto> = emptyList(),
    val searchQuery: String = "",

    // Tab Éditeurs (enrichis avec leurs jeux)
    val editeurs: List<EditeurAvecJeux> = emptyList(),

    // Tab Plan
    val zonesPlan: List<ZonePlanPublicDto> = emptyList(),

    val errorMessage: String? = null,
    val selectedTab: Int = 0
)

class VuesPubliquesViewModel(application: Application) : AndroidViewModel(application) {

    private val connectivity = NetworkConnectivityObserver(application)
    private val repository   = PublicRepository(connectivity)

    private val _uiState = MutableStateFlow(VuesPubliquesUiState())
    val uiState: StateFlow<VuesPubliquesUiState> = _uiState

    private var currentFestivalId: Int = -1

    fun init(festivalId: Int, festivalNom: String) {
        currentFestivalId = festivalId
        _uiState.update { it.copy(festivalNom = festivalNom) }
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Chargements parallèles
            val jeuxDeferred       = async { repository.getJeuxFestivalCourant() }
            val editeursDeferred   = async { repository.getEditeursFestivalCourant() }
            val zonesDeferred      = async { repository.getZonesPlan(currentFestivalId) }
            val festivalDeferred   = async { repository.getFestivalCourant() }

            val jeuxResult      = jeuxDeferred.await()
            val editeursResult  = editeursDeferred.await()
            val zonesResult     = zonesDeferred.await()
            val festivalResult  = festivalDeferred.await()

            val jeux = if (jeuxResult is ApiResult.Success) jeuxResult.data else emptyList()

            // Construire la liste d'éditeurs enrichis avec leurs jeux
            val editeurs = if (editeursResult is ApiResult.Success) {
                editeursResult.data.map { editeur ->
                    EditeurAvecJeux(
                        editeurId       = editeur.editeur_id,
                        editeurNom      = editeur.editeur_nom,
                        nbJeuxPresentes = editeur.nb_jeux_presentes,
                        jeux            = jeux.filter { it.editeur_id == editeur.editeur_id }
                    )
                }
            } else emptyList()

            val zones = if (zonesResult is ApiResult.Success) zonesResult.data else emptyList()

            // Info festival
            val festival = if (festivalResult is ApiResult.Success) festivalResult.data else null

            // Détecter l'erreur principale
            val error = when {
                jeuxResult is ApiResult.Error && editeursResult is ApiResult.Error ->
                    jeuxResult.message
                else -> null
            }

            _uiState.update {
                it.copy(
                    isLoading         = false,
                    jeux              = jeux,
                    editeurs          = editeurs,
                    zonesPlan         = zones,
                    festivalNom       = festival?.nom ?: it.festivalNom,
                    festivalDateDebut = festival?.date_debut,
                    festivalDateFin   = festival?.date_fin,
                    errorMessage      = error
                )
            }
        }
    }

    fun setTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Jeux filtrés selon la recherche
    fun getFilteredJeux(): List<JeuPublicDto> {
        val query = _uiState.value.searchQuery.trim().lowercase()
        if (query.isEmpty()) return _uiState.value.jeux
        return _uiState.value.jeux.filter { jeu ->
            jeu.jeu_nom.lowercase().contains(query) ||
                    jeu.editeur_nom.lowercase().contains(query) ||
                    (jeu.type_jeu?.lowercase()?.contains(query) == true) ||
                    (jeu.auteurs?.lowercase()?.contains(query) == true)
        }
    }

    // Éditeurs filtrés selon la recherche
    fun getFilteredEditeurs(): List<EditeurAvecJeux> {
        val query = _uiState.value.searchQuery.trim().lowercase()
        if (query.isEmpty()) return _uiState.value.editeurs
        return _uiState.value.editeurs.filter { editeur ->
            editeur.editeurNom.lowercase().contains(query) ||
                    editeur.jeux.any { it.jeu_nom.lowercase().contains(query) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}