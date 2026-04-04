package com.example.dev_mobile.ui.reservants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.network.*
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.ReservantRepository
import com.example.dev_mobile.utils.NetworkConnectivityObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservantUiState(
    val isLoading: Boolean = false,
    val reservants: List<ReservantDto> = emptyList(),
    val selectedReservant: ReservantDetailDto? = null,
    val isLoadingDetail: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isFromCache: Boolean = false
)

class ReservantViewModel(application: Application) : AndroidViewModel(application) {

    private val db           = AppDatabase.getInstance(application)
    private val connectivity = NetworkConnectivityObserver(application)
    private val repository   = ReservantRepository(db, connectivity)

    private val _uiState = MutableStateFlow(ReservantUiState())
    val uiState: StateFlow<ReservantUiState> = _uiState

    init {
        loadReservants()
        startRealtimeSync()
    }

    private fun startRealtimeSync() {
        viewModelScope.launch {
            while (true) {
                delay(15000)
                if (!_uiState.value.isSubmitting && _uiState.value.selectedReservant == null
                    && connectivity.isCurrentlyConnected()) {
                    val r = repository.getAll()
                    if (r is ApiResult.Success) {
                        _uiState.update { it.copy(reservants = r.data, isFromCache = false) }
                    }
                }
            }
        }
    }

    fun loadReservants() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val isOnline = connectivity.isCurrentlyConnected()
            when (val r = repository.getAll()) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isLoading   = false,
                    reservants  = r.data,
                    isFromCache = !isOnline
                )}
                is ApiResult.Error -> _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = r.message
                )}
            }
        }
    }

    fun openDetail(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }
            when (val r = repository.getById(id)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoadingDetail = false, selectedReservant = r.data) }
                is ApiResult.Error   -> _uiState.update { it.copy(isLoadingDetail = false, errorMessage = r.message) }
            }
        }
    }

    fun closeDetail() { _uiState.update { it.copy(selectedReservant = null) } }

    fun createReservant(nom: String, type: String, contacts: List<ContactDto>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val req = CreateReservantRequest(nom = nom, type_reservant = type, contacts = contacts)
            when (val r = repository.create(req)) {
                is ApiResult.Success -> {
                    loadReservants()
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Réservant « ${r.data.nom} » créé ✅") }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun updateReservant(id: Int, nom: String, type: String, contacts: List<ContactDto>) {
        viewModelScope.launch {
            val currentDetail = _uiState.value.selectedReservant
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val req = CreateReservantRequest(nom = nom, type_reservant = type, contacts = contacts)
            when (val r = repository.update(id, req)) {
                is ApiResult.Success -> {
                    val updatedDetail = r.data.copy(
                        historique = currentDetail?.historique ?: emptyList(),
                        contacts   = if (r.data.contacts.isNullOrEmpty()) contacts else r.data.contacts
                    )
                    _uiState.update { it.copy(
                        isSubmitting      = false,
                        successMessage    = "Modifications enregistrées ✏️",
                        selectedReservant = updatedDetail,
                        reservants        = _uiState.value.reservants.map { rv ->
                            if (rv.id == id) rv.copy(nom = updatedDetail.nom, type_reservant = updatedDetail.type_reservant, nb_contacts = updatedDetail.contacts?.size ?: 0) else rv
                        }
                    )}
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun deleteReservant(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = repository.delete(id)) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isSubmitting      = false,
                    reservants        = _uiState.value.reservants.filter { rv -> rv.id != id },
                    selectedReservant = null,
                    successMessage    = "Réservant supprimé 🗑️"
                )}
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = r.message) }
            }
        }
    }

    fun clearMessages() { _uiState.update { it.copy(errorMessage = null, successMessage = null) } }
}