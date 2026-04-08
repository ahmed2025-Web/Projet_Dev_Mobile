package com.example.dev_mobile.ui.reservations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.network.*
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.JeuRepository
import com.example.dev_mobile.repository.ReservantRepository
import com.example.dev_mobile.repository.ReservationRepository
import com.example.dev_mobile.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<ReservationDto> = emptyList(),
    val selectedReservation: ReservationDetailDto? = null,
    val isLoadingDetail: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val zonesTarifaires: List<ZoneTarifaireDto> = emptyList(),
    val reservants: List<ReservantDto> = emptyList(),
    val activeFilter: String? = null,
    val isFromCache: Boolean = false,
    // Pour la gestion des jeux
    val allJeux: List<JeuSummaryDto> = emptyList(),
    val isLoadingJeux: Boolean = false,
    val jeuSearchQuery: String = ""
)

class ReservationViewModel(application: Application) : AndroidViewModel(application) {

    private val db            = AppDatabase.getInstance(application)
    private val connectivity  = NetworkConnectivityObserver(application)
    private val repository    = ReservationRepository(db, connectivity)
    private val reservantRepo = ReservantRepository(db, connectivity)
    private val jeuRepo       = JeuRepository()

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState

    private var currentFestivalId: Int = -1

    fun init(festivalId: Int) {
        if (festivalId <= 0) return
        currentFestivalId = festivalId
        loadReservations()
        loadZonesTarifaires()
        loadReservants()
        loadAllJeux()
    }

    fun loadReservations() {
        if (currentFestivalId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val isOnline = connectivity.isCurrentlyConnected()
            when (val r = repository.getByFestival(currentFestivalId)) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isLoading    = false,
                    reservations = r.data,
                    isFromCache  = !isOnline
                )}
                is ApiResult.Error -> _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = r.message
                )}
            }
        }
    }

    private fun loadZonesTarifaires() {
        if (currentFestivalId <= 0) return
        viewModelScope.launch {
            when (val r = repository.getZonesTarifaires(currentFestivalId)) {
                is ApiResult.Success -> _uiState.update { it.copy(zonesTarifaires = r.data) }
                is ApiResult.Error   -> { /* silent */ }
            }
        }
    }

    private fun loadReservants() {
        viewModelScope.launch {
            when (val r = reservantRepo.getAll()) {
                is ApiResult.Success -> _uiState.update { it.copy(reservants = r.data) }
                is ApiResult.Error   -> { /* silent */ }
            }
        }
    }

    private fun loadAllJeux() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingJeux = true) }
            when (val r = jeuRepo.getAll()) {
                is ApiResult.Success -> _uiState.update { it.copy(allJeux = r.data, isLoadingJeux = false) }
                is ApiResult.Error   -> _uiState.update { it.copy(isLoadingJeux = false) }
            }
        }
    }

    fun setJeuSearchQuery(q: String) {
        _uiState.update { it.copy(jeuSearchQuery = q) }
    }

    fun getFilteredJeuxForSearch(): List<JeuSummaryDto> {
        val q = _uiState.value.jeuSearchQuery.trim().lowercase()
        val current = _uiState.value.selectedReservation
        val alreadyAdded = current?.jeux?.map { it.jeu_id }?.toSet() ?: emptySet()
        val all = _uiState.value.allJeux.filter { it.id !in alreadyAdded }
        if (q.isEmpty()) return all
        return all.filter {
            it.nom.lowercase().contains(q) ||
                    (it.editeur_nom?.lowercase()?.contains(q) == true) ||
                    (it.type_jeu?.lowercase()?.contains(q) == true)
        }
    }

    fun openDetail(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }
            when (val r = repository.getById(id)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoadingDetail = false, selectedReservation = r.data) }
                is ApiResult.Error   -> _uiState.update { it.copy(isLoadingDetail = false, errorMessage = r.message) }
            }
        }
    }

    fun closeDetail() { _uiState.update { it.copy(selectedReservation = null, jeuSearchQuery = "") } }

    fun createReservation(reservantId: Int, nbPrises: Int, notes: String?, viendrAnimer: Boolean, zones: List<ZoneReserveeRequest>) {
        if (currentFestivalId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val req = CreateReservationRequest(
                reservant_id = reservantId, etat_contact = "contacte",
                nb_prises_electriques = nbPrises, notes = notes?.ifBlank { null },
                viendra_animer = viendrAnimer, zones_reservees = zones
            )
            when (val r = repository.create(currentFestivalId, req)) {
                is ApiResult.Success -> {
                    loadReservations()
                    _uiState.update { it.copy(isSubmitting = false, showCreateDialog = false, successMessage = "Réservation créée ✅") }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun updateReservation(id: Int, nbPrises: Int, remiseTables: Int, remiseMontant: Double, notes: String?, viendrAnimer: Boolean, zones: List<ZoneReserveeRequest>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val req = UpdateReservationRequest(
                nb_prises_electriques = nbPrises, remise_tables = remiseTables,
                remise_montant = remiseMontant, notes = notes?.ifBlank { null },
                viendra_animer = viendrAnimer, zones_reservees = zones
            )
            when (val r = repository.update(id, req)) {
                is ApiResult.Success -> {
                    loadReservations()
                    if (_uiState.value.selectedReservation?.id == id) openDetail(id)
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Réservation modifiée ✏️") }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun deleteReservation(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = repository.delete(id)) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isSubmitting        = false,
                    reservations        = _uiState.value.reservations.filter { it.id != id },
                    selectedReservation = null,
                    successMessage      = "Réservation supprimée 🗑️"
                )}
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun updateWorkflowContact(id: Int, etat: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = repository.updateWorkflowContact(id, etat)) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isSubmitting        = false,
                    reservations        = _uiState.value.reservations.map { if (it.id == id) it.copy(etat_contact = etat) else it },
                    selectedReservation = _uiState.value.selectedReservation?.let { if (it.id == id) it.copy(etat_contact = etat) else it },
                    successMessage      = "Workflow mis à jour ✅"
                )}
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun updateWorkflowPresence(id: Int, etat: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = repository.updateWorkflowPresence(id, etat)) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isSubmitting        = false,
                    reservations        = _uiState.value.reservations.map { if (it.id == id) it.copy(etat_presence = etat) else it },
                    selectedReservation = _uiState.value.selectedReservation?.let { if (it.id == id) it.copy(etat_presence = etat) else it },
                    successMessage      = "Présence mise à jour ✅"
                )}
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun addContactRelance(id: Int, dateContact: String?, typeContact: String?, notes: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val req = AddContactRelanceRequest(
                date_contact = dateContact,
                type_contact = typeContact?.ifBlank { null },
                notes = notes?.ifBlank { null }
            )
            when (val r = repository.addContactRelance(id, req)) {
                is ApiResult.Success -> {
                    openDetail(id)
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Contact ajouté ✅") }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    // ── Gestion des jeux ──────────────────────────────────────────────────────

    fun addJeuToReservation(reservationId: Int, jeuId: Int, nbExemplaires: Int, tablesAllouees: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val r = repository.addJeu(reservationId, jeuId, nbExemplaires, tablesAllouees)) {
                is ApiResult.Success -> {
                    // Recharger le détail pour avoir la liste à jour
                    openDetail(reservationId)
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Jeu ajouté ✅", jeuSearchQuery = "") }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun removeJeuFromReservation(reservationId: Int, jeuFestivalId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = repository.removeJeu(reservationId, jeuFestivalId)) {
                is ApiResult.Success -> {
                    // Mettre à jour localement sans rechargement complet
                    val updated = _uiState.value.selectedReservation?.let { detail ->
                        detail.copy(jeux = detail.jeux.filter { it.id != jeuFestivalId })
                    }
                    _uiState.update { it.copy(
                        isSubmitting        = false,
                        selectedReservation = updated,
                        successMessage      = "Jeu retiré 🗑️"
                    )}
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun toggleJeuRecu(reservationId: Int, jeuFestivalId: Int, currentRecu: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = repository.updateJeuRecu(reservationId, jeuFestivalId, !currentRecu)) {
                is ApiResult.Success -> {
                    val updatedJeu = r.data
                    val updated = _uiState.value.selectedReservation?.let { detail ->
                        detail.copy(jeux = detail.jeux.map { j ->
                            if (j.id == jeuFestivalId) updatedJeu else j
                        })
                    }
                    _uiState.update { it.copy(
                        isSubmitting        = false,
                        selectedReservation = updated,
                        successMessage      = if (!currentRecu) "Jeu marqué reçu ✅" else "Jeu marqué non reçu"
                    )}
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun setFilter(filter: String?)  { _uiState.update { it.copy(activeFilter = filter) } }
    fun openCreateDialog()          { _uiState.update { it.copy(showCreateDialog = true) } }
    fun closeCreateDialog()         { _uiState.update { it.copy(showCreateDialog = false) } }
    fun clearMessages()             { _uiState.update { it.copy(errorMessage = null, successMessage = null) } }

    fun getFilteredReservations(): List<ReservationDto> {
        val filter = _uiState.value.activeFilter ?: return _uiState.value.reservations
        return _uiState.value.reservations.filter { it.etat_contact == filter }
    }
}