package com.example.dev_mobile.ui.festivals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.network.*
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.FestivalRepository
import com.example.dev_mobile.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FestivalUiState(
    val isLoading: Boolean = false,
    val festivals: List<FestivalDashboardDto> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editTarget: FestivalDashboardDto? = null,
    val isFromCache: Boolean = false
)

class FestivalViewModel(application: Application) : AndroidViewModel(application) {

    private val db           = AppDatabase.getInstance(application)
    private val connectivity = NetworkConnectivityObserver(application)
    private val repository   = FestivalRepository(db, connectivity)

    private val _uiState = MutableStateFlow(FestivalUiState())
    val uiState: StateFlow<FestivalUiState> = _uiState

    init { loadFestivals() }

    fun loadFestivals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val isOnline = connectivity.isCurrentlyConnected()
            when (val r = repository.getAll()) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isLoading   = false,
                    festivals   = r.data,
                    isFromCache = !isOnline
                )}
                is ApiResult.Error -> _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = r.message
                )}
            }
        }
    }

    fun setCourant(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val r = repository.setCourant(id)) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isSubmitting   = false,
                    successMessage = "Festival courant mis à jour ✅",
                    festivals      = _uiState.value.festivals.map { f -> f.copy(est_courant = f.id == id) }
                )}
                is ApiResult.Error -> _uiState.update { it.copy(
                    isSubmitting = false, errorMessage = r.message
                )}
            }
        }
    }

    fun createFestival(
        nom: String, espaceTables: Int, dateDebut: String?, dateFin: String?,
        stockStd: Int, stockGde: Int, stockMairie: Int,
        stockChaisesStd: Int, stockChaisesMairie: Int, prixPrise: Double, setCourant: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val req = CreateFestivalRequest(
                nom = nom, espace_tables_total = espaceTables,
                date_debut = dateDebut?.ifBlank { null }, date_fin = dateFin?.ifBlank { null },
                stock_tables_standard = stockStd, stock_tables_grandes = stockGde,
                stock_tables_mairie = stockMairie, stock_chaises_standard = stockChaisesStd,
                stock_chaises_mairie = stockChaisesMairie, prix_prise_electrique = prixPrise,
                est_courant = setCourant
            )
            when (val r = repository.create(req)) {
                is ApiResult.Success -> {
                    loadFestivals()
                    _uiState.update { it.copy(
                        isSubmitting     = false,
                        showCreateDialog = false,
                        successMessage   = "Festival « ${r.data.nom} » créé ✅"
                    )}
                }
                is ApiResult.Error -> _uiState.update { it.copy(
                    isSubmitting = false, errorMessage = r.message
                )}
            }
        }
    }

    fun updateFestival(
        id: Int, nom: String, espaceTables: Int, dateDebut: String?, dateFin: String?,
        stockStd: Int, stockGde: Int, stockMairie: Int,
        stockChaisesStd: Int, stockChaisesMairie: Int, prixPrise: Double
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val req = UpdateFestivalRequest(
                nom = nom, espace_tables_total = espaceTables,
                date_debut = dateDebut?.ifBlank { null }, date_fin = dateFin?.ifBlank { null },
                stock_tables_standard = stockStd, stock_tables_grandes = stockGde,
                stock_tables_mairie = stockMairie, stock_chaises_standard = stockChaisesStd,
                stock_chaises_mairie = stockChaisesMairie, prix_prise_electrique = prixPrise
            )
            when (val r = repository.update(id, req)) {
                is ApiResult.Success -> _uiState.update { it.copy(
                    isSubmitting   = false,
                    showEditDialog = false,
                    editTarget     = null,
                    successMessage = "Festival modifié ✏️",
                    festivals      = _uiState.value.festivals.map { if (it.id == id) r.data else it }
                )}
                is ApiResult.Error -> _uiState.update { it.copy(
                    isSubmitting = false, errorMessage = r.message
                )}
            }
        }
    }

    fun deleteFestival(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val check = repository.canDelete(id)) {
                is ApiResult.Success -> {
                    if (!check.data.canDelete) {
                        _uiState.update { it.copy(
                            isSubmitting = false,
                            errorMessage = check.data.reason ?: "Impossible de supprimer ce festival"
                        )}
                        return@launch
                    }
                    when (val r = repository.delete(id)) {
                        is ApiResult.Success -> _uiState.update { it.copy(
                            isSubmitting = false,
                            festivals    = _uiState.value.festivals.filter { f -> f.id != id },
                            successMessage = "Festival supprimé 🗑️"
                        )}
                        is ApiResult.Error -> _uiState.update { it.copy(
                            isSubmitting = false, errorMessage = r.message
                        )}
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(
                    isSubmitting = false, errorMessage = check.message
                )}
            }
        }
    }

    fun openCreateDialog()  { _uiState.update { it.copy(showCreateDialog = true) } }
    fun closeCreateDialog() { _uiState.update { it.copy(showCreateDialog = false) } }
    fun openEditDialog(f: FestivalDashboardDto) { _uiState.update { it.copy(showEditDialog = true, editTarget = f) } }
    fun closeEditDialog()   { _uiState.update { it.copy(showEditDialog = false, editTarget = null) } }
    fun clearMessages()     { _uiState.update { it.copy(errorMessage = null, successMessage = null) } }
}