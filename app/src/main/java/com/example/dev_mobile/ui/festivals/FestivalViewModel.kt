package com.example.dev_mobile.ui.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.CreateFestivalRequest
import com.example.dev_mobile.network.FestivalDashboardDto
import com.example.dev_mobile.network.UpdateFestivalRequest
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FestivalUiState(
    val isLoading: Boolean = false,
    val festivals: List<FestivalDashboardDto> = emptyList(),
    val selectedFestival: FestivalDashboardDto? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Pour le dialogue de création/édition
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editTarget: FestivalDashboardDto? = null
)

class FestivalViewModel : ViewModel() {
    private val repository = FestivalRepository()
    private val _uiState = MutableStateFlow(FestivalUiState())
    val uiState: StateFlow<FestivalUiState> = _uiState

    init { loadFestivals() }

    fun loadFestivals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val r = repository.getAll()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, festivals = r.data
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, errorMessage = r.message
                )
            }
        }
    }

    fun setCourant(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            when (val r = repository.setCourant(id)) {
                is ApiResult.Success -> {
                    // Met à jour la liste localement
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Festival courant mis à jour ✅",
                        festivals = _uiState.value.festivals.map {
                            it.copy(est_courant = it.id == id)
                        }
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun createFestival(
        nom: String,
        espaceTables: Int,
        dateDebut: String?,
        dateFin: String?,
        stockStd: Int,
        stockGde: Int,
        stockMairie: Int,
        stockChaisesStd: Int,
        stockChaisesMairie: Int,
        prixPrise: Double,
        setCourant: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            val req = CreateFestivalRequest(
                nom = nom,
                espace_tables_total = espaceTables,
                date_debut = dateDebut?.ifBlank { null },
                date_fin = dateFin?.ifBlank { null },
                stock_tables_standard = stockStd,
                stock_tables_grandes = stockGde,
                stock_tables_mairie = stockMairie,
                stock_chaises_standard = stockChaisesStd,
                stock_chaises_mairie = stockChaisesMairie,
                prix_prise_electrique = prixPrise,
                est_courant = setCourant
            )
            when (val r = repository.create(req)) {
                is ApiResult.Success -> {
                    loadFestivals()
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showCreateDialog = false,
                        successMessage = "Festival « ${r.data.nom} » créé ✅"
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun updateFestival(
        id: Int,
        nom: String,
        espaceTables: Int,
        dateDebut: String?,
        dateFin: String?,
        stockStd: Int,
        stockGde: Int,
        stockMairie: Int,
        stockChaisesStd: Int,
        stockChaisesMairie: Int,
        prixPrise: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            val req = UpdateFestivalRequest(
                nom = nom,
                espace_tables_total = espaceTables,
                date_debut = dateDebut?.ifBlank { null },
                date_fin = dateFin?.ifBlank { null },
                stock_tables_standard = stockStd,
                stock_tables_grandes = stockGde,
                stock_tables_mairie = stockMairie,
                stock_chaises_standard = stockChaisesStd,
                stock_chaises_mairie = stockChaisesMairie,
                prix_prise_electrique = prixPrise
            )
            when (val r = repository.update(id, req)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showEditDialog = false,
                        editTarget = null,
                        successMessage = "Festival modifié ✏️",
                        festivals = _uiState.value.festivals.map { if (it.id == id) r.data else it }
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = r.message
                )
            }
        }
    }

    fun deleteFestival(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            // D'abord vérifier si on peut supprimer
            when (val check = repository.canDelete(id)) {
                is ApiResult.Success -> {
                    if (!check.data.canDelete) {
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            errorMessage = check.data.reason ?: "Impossible de supprimer ce festival"
                        )
                        return@launch
                    }
                    // Procéder à la suppression
                    when (val r = repository.delete(id)) {
                        is ApiResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isSubmitting = false,
                                festivals = _uiState.value.festivals.filter { it.id != id },
                                successMessage = "Festival supprimé 🗑️"
                            )
                        }
                        is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                            isSubmitting = false, errorMessage = r.message
                        )
                    }
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false, errorMessage = check.message
                )
            }
        }
    }

    fun openCreateDialog()  { _uiState.value = _uiState.value.copy(showCreateDialog = true) }
    fun closeCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = false) }

    fun openEditDialog(f: FestivalDashboardDto) {
        _uiState.value = _uiState.value.copy(showEditDialog = true, editTarget = f)
    }
    fun closeEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, editTarget = null)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}