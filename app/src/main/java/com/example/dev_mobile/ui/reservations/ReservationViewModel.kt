package com.example.dev_mobile.ui.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.*
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<ReservationDto> = emptyList(),
    val selectedReservation: ReservationDetailDto? = null,
    val isLoadingDetail: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Pour création
    val showCreateDialog: Boolean = false,
    // Zones tarifaires du festival (pour création)
    val zonesTarifaires: List<ZoneTarifaireDto> = emptyList(),
    // Reservants disponibles (pour sélection)
    val reservants: List<ReservantDto> = emptyList(),
    // Filtre actif
    val activeFilter: String? = null
)

class ReservationViewModel : ViewModel() {
    private val repository = ReservationRepository()
    // Réservants via ReservantRepository
    private val reservantRepo = com.example.dev_mobile.repository.ReservantRepository()

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState

    private var currentFestivalId: Int = -1

    fun init(festivalId: Int) {
        if (festivalId <= 0) return
        currentFestivalId = festivalId
        loadReservations()
        loadZonesTarifaires()
        loadReservants()
    }

    fun loadReservations() {
        if (currentFestivalId <= 0) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val r = repository.getByFestival(currentFestivalId)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, reservations = r.data
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, errorMessage = r.message
                )
            }
        }
    }

    private fun loadZonesTarifaires() {
        if (currentFestivalId <= 0) return
        viewModelScope.launch {
            when (val r = repository.getZonesTarifaires(currentFestivalId)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(zonesTarifaires = r.data)
                is ApiResult.Error -> { /* silent */ }
            }
        }
    }

    private fun loadReservants() {
        viewModelScope.launch {
            when (val r = reservantRepo.getAll()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(reservants = r.data)
                is ApiResult.Error -> { /* silent */ }
            }
        }
    }

    fun openDetail(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDetail = true)
            when (val r = repository.getById(id)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false, selectedReservation = r.data
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false, errorMessage = r.message
                )
            }
        }
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(selectedReservation = null)
    }

    fun createReservation(
        reservantId: Int,
        nbPrises: Int,
        notes: String?,
        viendrAnimer: Boolean,
        zones: List<ZoneReserveeRequest>
    ) {
        if (currentFestivalId <= 0) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            val req = CreateReservationRequest(
                reservant_id = reservantId,
                etat_contact = "contacte",
                nb_prises_electriques = nbPrises,
                notes = notes?.ifBlank { null },
                viendra_animer = viendrAnimer,
                zones_reservees = zones
            )
            when (val r = repository.create(currentFestivalId, req)) {
                is ApiResult.Success -> {
                    loadReservations()
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showCreateDialog = false,
                        successMessage = "Réservation créée ✅"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun updateReservation(
        id: Int,
        nbPrises: Int,
        remiseTables: Int,
        remiseMontant: Double,
        notes: String?,
        viendrAnimer: Boolean,
        zones: List<ZoneReserveeRequest>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            val req = UpdateReservationRequest(
                nb_prises_electriques = nbPrises,
                remise_tables = remiseTables,
                remise_montant = remiseMontant,
                notes = notes?.ifBlank { null },
                viendra_animer = viendrAnimer,
                zones_reservees = zones
            )
            when (val r = repository.update(id, req)) {
                is ApiResult.Success -> {
                    loadReservations()
                    // Rafraîchir le détail si ouvert
                    if (_uiState.value.selectedReservation?.id == id) {
                        openDetail(id)
                    }
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Réservation modifiée ✏️"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun deleteReservation(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            when (val r = repository.delete(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        reservations = _uiState.value.reservations.filter { it.id != id },
                        selectedReservation = null,
                        successMessage = "Réservation supprimée 🗑️"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun updateWorkflowContact(id: Int, etat: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            when (val r = repository.updateWorkflowContact(id, etat)) {
                is ApiResult.Success -> {
                    // Mise à jour locale dans la liste
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        reservations = _uiState.value.reservations.map {
                            if (it.id == id) it.copy(etat_contact = etat) else it
                        },
                        selectedReservation = _uiState.value.selectedReservation?.let {
                            if (it.id == id) it.copy(etat_contact = etat) else it
                        },
                        successMessage = "Workflow mis à jour ✅"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun updateWorkflowPresence(id: Int, etat: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            when (val r = repository.updateWorkflowPresence(id, etat)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        reservations = _uiState.value.reservations.map {
                            if (it.id == id) it.copy(etat_presence = etat) else it
                        },
                        selectedReservation = _uiState.value.selectedReservation?.let {
                            if (it.id == id) it.copy(etat_presence = etat) else it
                        },
                        successMessage = "Présence mise à jour ✅"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun addContactRelance(id: Int, dateContact: String?, typeContact: String?, notes: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            val req = AddContactRelanceRequest(
                date_contact = dateContact,
                type_contact = typeContact?.ifBlank { null },
                notes = notes?.ifBlank { null }
            )
            when (val r = repository.addContactRelance(id, req)) {
                is ApiResult.Success -> {
                    // Rafraîchir le détail
                    openDetail(id)
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Contact/relance ajouté ✅"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun setFilter(filter: String?) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun openCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun closeCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    // Liste filtrée pour l'affichage
    fun getFilteredReservations(): List<ReservationDto> {
        val filter = _uiState.value.activeFilter ?: return _uiState.value.reservations
        return _uiState.value.reservations.filter { it.etat_contact == filter }
    }
}