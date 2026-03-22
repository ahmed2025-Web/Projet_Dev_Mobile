
package com.example.dev_mobile.ui.reservants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.ContactDto
import com.example.dev_mobile.network.CreateReservantRequest
import com.example.dev_mobile.network.ReservantDetailDto
import com.example.dev_mobile.network.ReservantDto
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.ReservantRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReservantUiState(
    val isLoading: Boolean = false,
    val reservants: List<ReservantDto> = emptyList(),
    val selectedReservant: ReservantDetailDto? = null,
    val isLoadingDetail: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ReservantViewModel : ViewModel() {
    private val repository = ReservantRepository()
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
                if (!_uiState.value.isSubmitting && _uiState.value.selectedReservant == null) {
                    val r = repository.getAll()
                    if (r is ApiResult.Success) {
                        _uiState.value = _uiState.value.copy(reservants = r.data)
                    }
                }
            }
        }
    }

    fun loadReservants() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val r = repository.getAll()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, reservants = r.data)
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = r.message)
            }
        }
    }

    fun openDetail(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDetail = true)
            when (val r = repository.getById(id)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoadingDetail = false, selectedReservant = r.data)
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(isLoadingDetail = false, errorMessage = r.message)
            }
        }
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(selectedReservant = null)
    }

    fun createReservant(nom: String, type: String, contacts: List<ContactDto>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            val req = CreateReservantRequest(nom = nom, type_reservant = type, contacts = contacts)
            when (val r = repository.create(req)) {
                is ApiResult.Success -> {
                    loadReservants()
                    _uiState.value = _uiState.value.copy(isSubmitting = false, successMessage = "Réservant « ${r.data.nom} » créé avec succès ! ✅")
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = r.message)
            }
        }
    }

    fun updateReservant(id: Int, nom: String, type: String, contacts: List<ContactDto>) {
        viewModelScope.launch {
            val currentDetail = _uiState.value.selectedReservant
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            
            val req = CreateReservantRequest(nom = nom, type_reservant = type, contacts = contacts)
            when (val r = repository.update(id, req)) {
                is ApiResult.Success -> {
                    val updatedDetail = r.data.copy(
                        historique = currentDetail?.historique ?: emptyList(),
                        contacts = if (r.data.contacts.isNullOrEmpty()) contacts else r.data.contacts
                    )
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Les modifications ont été enregistrées ✏️",
                        selectedReservant = updatedDetail,
                        reservants = _uiState.value.reservants.map {
                            if (it.id == id) it.copy(
                                nom = updatedDetail.nom,
                                type_reservant = updatedDetail.type_reservant,
                                nb_contacts = updatedDetail.contacts?.size ?: 0
                            ) else it
                        }
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = r.message)
            }
        }
    }

    fun deleteReservant(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            when (val r = repository.delete(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        reservants = _uiState.value.reservants.filter { it.id != id },
                        selectedReservant = null,
                        successMessage = "Le réservant a été supprimé 🗑️"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = r.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}