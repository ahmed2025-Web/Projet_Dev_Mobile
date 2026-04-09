// app/src/main/java/com/example/dev_mobile/ui/zones/ZoneViewModel.kt
package com.example.dev_mobile.ui.zones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.network.*
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.FestivalRepository
import com.example.dev_mobile.repository.ZoneRepository
import com.example.dev_mobile.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ZoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db           = AppDatabase.getInstance(application)
    private val connectivity = NetworkConnectivityObserver(application)
    private val festivalRepo = FestivalRepository(db, connectivity)
    private val repository   = ZoneRepository()

    private val _uiState = MutableStateFlow(ZoneUiState())
    val uiState: StateFlow<ZoneUiState> = _uiState

    init { loadFestivalAndZones() }

    // ── Chargement initial ────────────────────────────────────────────────────

    fun loadFestivalAndZones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val festival = festivalRepo.getFestivalCourant()
            if (festival == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Aucun festival courant défini")
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    festivalId           = festival.id,
                    festivalNom          = festival.nom,
                    festivalEspaceTables = festival.espace_tables_total
                )
            }

            loadBothZones(festival.id)
        }
    }

    private suspend fun loadBothZones(festivalId: Int) {
        val tarifResult = repository.getZonesTarifaires(festivalId)
        val planResult  = repository.getZonesPlan(festivalId)

        _uiState.update {
            it.copy(
                isLoading       = false,
                zonesTarifaires = if (tarifResult is ApiResult.Success) tarifResult.data else it.zonesTarifaires,
                zonesPlan       = if (planResult  is ApiResult.Success) planResult.data  else it.zonesPlan,
                errorMessage    = when {
                    tarifResult is ApiResult.Error -> tarifResult.message
                    planResult  is ApiResult.Error -> planResult.message
                    else -> null
                }
            )
        }
    }

    fun refresh() { loadFestivalAndZones() }

    fun setTab(tab: ZoneTab) { _uiState.update { it.copy(activeTab = tab) } }

    // ── Zones Tarifaires — CRUD ───────────────────────────────────────────────

    fun openCreateTarifaireDialog()  { _uiState.update { it.copy(showCreateTarifaireDialog = true) } }
    fun closeCreateTarifaireDialog() { _uiState.update { it.copy(showCreateTarifaireDialog = false) } }

    fun openEditTarifaireDialog(zone: ZoneTarifaireDetailDto) {
        _uiState.update { it.copy(showEditTarifaireDialog = true, tarifaireToEdit = zone) }
    }
    fun closeEditTarifaireDialog() {
        _uiState.update { it.copy(showEditTarifaireDialog = false, tarifaireToEdit = null) }
    }

    fun createZoneTarifaire(nom: String, nbTables: Int, prixTable: Double, prixM2: Double?) {
        val festivalId = _uiState.value.festivalId
        if (festivalId <= 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingTarifaire = true, errorMessage = null) }
            val req = CreateZoneTarifaireRequest(
                nom                 = nom,
                nombre_tables_total = nbTables,
                prix_table          = prixTable,
                prix_m2             = prixM2 ?: (prixTable / 4.5)
            )
            when (val r = repository.createZoneTarifaire(festivalId, req)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingTarifaire     = false,
                            showCreateTarifaireDialog = false,
                            zonesTarifaires           = it.zonesTarifaires + r.data,
                            successMessage            = "Zone tarifaire « ${r.data.nom} » créée ✅"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingTarifaire = false, errorMessage = r.message)
                }
            }
        }
    }

    fun updateZoneTarifaire(id: Int, nom: String, nbTables: Int, prixTable: Double, prixM2: Double?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingTarifaire = true, errorMessage = null) }
            val req = UpdateZoneTarifaireRequest(
                nom                 = nom,
                nombre_tables_total = nbTables,
                prix_table          = prixTable,
                prix_m2             = prixM2 ?: (prixTable / 4.5)
            )
            when (val r = repository.updateZoneTarifaire(id, req)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmittingTarifaire   = false,
                        showEditTarifaireDialog = false,
                        tarifaireToEdit         = null,
                        zonesTarifaires         = it.zonesTarifaires.map { z -> if (z.id == id) r.data else z },
                        successMessage          = "Zone tarifaire modifiée ✏️"
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingTarifaire = false, errorMessage = r.message)
                }
            }
        }
    }

    fun deleteZoneTarifaire(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingTarifaire = true) }
            when (val r = repository.deleteZoneTarifaire(id)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmittingTarifaire = false,
                        zonesTarifaires       = it.zonesTarifaires.filter { z -> z.id != id },
                        successMessage        = "Zone tarifaire supprimée 🗑️"
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingTarifaire = false, errorMessage = r.message)
                }
            }
        }
    }

    // ── Zones du Plan — CRUD ──────────────────────────────────────────────────

    fun openCreatePlanDialog()  { _uiState.update { it.copy(showCreatePlanDialog = true) } }
    fun closeCreatePlanDialog() { _uiState.update { it.copy(showCreatePlanDialog = false) } }

    fun openEditPlanDialog(zone: ZonePlanDetailDto) {
        _uiState.update { it.copy(showEditPlanDialog = true, planToEdit = zone) }
    }
    fun closeEditPlanDialog() {
        _uiState.update { it.copy(showEditPlanDialog = false, planToEdit = null) }
    }

    fun createZonePlan(nom: String, nbTables: Int) {
        val festivalId = _uiState.value.festivalId
        if (festivalId <= 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingPlan = true, errorMessage = null) }
            val req = CreateZonePlanRequest(nom = nom, nombre_tables_total = nbTables)
            when (val r = repository.createZonePlan(festivalId, req)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmittingPlan     = false,
                        showCreatePlanDialog = false,
                        zonesPlan            = it.zonesPlan + r.data,
                        successMessage       = "Zone du plan « ${r.data.nom} » créée ✅"
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingPlan = false, errorMessage = r.message)
                }
            }
        }
    }

    fun updateZonePlan(id: Int, nom: String, nbTables: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingPlan = true, errorMessage = null) }
            val req = UpdateZonePlanRequest(nom = nom, nombre_tables_total = nbTables)
            when (val r = repository.updateZonePlan(id, req)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmittingPlan  = false,
                        showEditPlanDialog = false,
                        planToEdit         = null,
                        zonesPlan          = it.zonesPlan.map { z -> if (z.id == id) r.data else z },
                        successMessage     = "Zone du plan modifiée ✏️"
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingPlan = false, errorMessage = r.message)
                }
            }
        }
    }

    fun deleteZonePlan(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingPlan = true) }
            when (val r = repository.deleteZonePlan(id)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmittingPlan = false,
                        zonesPlan        = it.zonesPlan.filter { z -> z.id != id },
                        successMessage   = "Zone du plan supprimée 🗑️"
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingPlan = false, errorMessage = r.message)
                }
            }
        }
    }

    // ── Placement des jeux ────────────────────────────────────────────────────

    fun openPlacerJeuDialog(zone: ZonePlanDetailDto) {
        _uiState.update {
            it.copy(
                zonePlanForPlacement = zone,
                showPlacerJeuDialog  = true,
                jeuxDisponibles      = emptyList()
            )
        }
        chargerJeuxDisponibles()
    }

    fun closePlacerJeuDialog() {
        _uiState.update {
            it.copy(
                showPlacerJeuDialog  = false,
                zonePlanForPlacement = null,
                jeuxDisponibles      = emptyList()
            )
        }
    }

    private fun chargerJeuxDisponibles() {
        val festivalId = _uiState.value.festivalId
        if (festivalId <= 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingJeux = true) }
            when (val r = repository.getJeuxFestival(festivalId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoadingJeux = false, jeuxDisponibles = r.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoadingJeux = false, errorMessage = r.message)
                }
            }
        }
    }

    fun placerJeu(jeuId: Int, nbTables: Int, typeTable: String) {
        val zone = _uiState.value.zonePlanForPlacement ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingPlacement = true, errorMessage = null) }
            val req = PlacerJeuRequest(jeu_id = jeuId, nb_tables = nbTables, type_table = typeTable)
            when (val r = repository.placerJeu(zone.id, req)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingPlacement = false,
                            showPlacerJeuDialog   = false,
                            zonePlanForPlacement  = null,
                            zonesPlan             = it.zonesPlan.map { z ->
                                if (z.id == zone.id) r.data else z
                            },
                            successMessage        = "Jeu placé dans « ${zone.nom} » ✅"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingPlacement = false, errorMessage = r.message)
                }
            }
        }
    }

    fun retirerJeu(zoneId: Int, jeuId: Int) {
        viewModelScope.launch {
            when (val r = repository.retirerJeu(zoneId, jeuId)) {
                is ApiResult.Success -> {
                    // Recharger la zone après retrait
                    val festivalId = _uiState.value.festivalId
                    val planResult = repository.getZonesPlan(festivalId)
                    _uiState.update {
                        it.copy(
                            zonesPlan      = if (planResult is ApiResult.Success) planResult.data else it.zonesPlan,
                            successMessage = "Jeu retiré ✅"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(errorMessage = r.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}