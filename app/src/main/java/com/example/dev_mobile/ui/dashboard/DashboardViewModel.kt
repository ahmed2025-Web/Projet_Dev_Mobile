package com.example.dev_mobile.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.data.local.AppDatabase
import com.example.dev_mobile.network.FestivalDashboardDto
import com.example.dev_mobile.network.ReservationDto
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.FestivalRepository
import com.example.dev_mobile.repository.ReservationRepository
import com.example.dev_mobile.utils.NetworkConnectivityObserver
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val festival: FestivalDashboardDto? = null,
    val reservations: List<ReservationDto> = emptyList(),
    val errorMessage: String? = null,
    val lastRefresh: Long = 0L,
    val isFromCache: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db              = AppDatabase.getInstance(application)
    private val connectivity    = NetworkConnectivityObserver(application)
    private val festivalRepo    = FestivalRepository(db, connectivity)
    private val reservationRepo = ReservationRepository(db, connectivity)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val isOnline = connectivity.isCurrentlyConnected()
            val festival = festivalRepo.getFestivalCourant()

            if (festival == null) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = if (isOnline) "Aucun festival courant défini"
                    else "Hors ligne — Aucune donnée en cache"
                )}
                return@launch
            }

            val reservations = when (val r = reservationRepo.getByFestival(festival.id)) {
                is ApiResult.Success -> r.data
                is ApiResult.Error   -> emptyList()
            }

            _uiState.update { it.copy(
                isLoading    = false,
                festival     = festival,
                reservations = reservations,
                lastRefresh  = System.currentTimeMillis(),
                isFromCache  = !isOnline
            )}
        }
    }

    fun getReservationStats(): Map<String, Int> {
        val list = _uiState.value.reservations
        return mapOf(
            "total"               to list.size,
            "pas_contacte"        to list.count { it.etat_contact == "pas_contacte" },
            "contacte"            to list.count { it.etat_contact == "contacte" },
            "en_discussion"       to list.count { it.etat_contact == "en_discussion" },
            "reserve"             to list.count { it.etat_contact == "reserve" },
            "liste_jeux_demandee" to list.count { it.etat_contact == "liste_jeux_demandee" },
            "liste_jeux_obtenue"  to list.count { it.etat_contact == "liste_jeux_obtenue" },
            "jeux_recus"          to list.count { it.etat_contact == "jeux_recus" },
            "presents"            to list.count { it.etat_presence == "present" },
            "absents"             to list.count { it.etat_presence == "absent" }
        )
    }

    fun getTotalTablesReservees(): Int = _uiState.value.reservations.sumOf { it.nb_tables_reservees }
    fun getMontantTotal(): Double      = _uiState.value.reservations.sumOf { it.montant_brut }
}