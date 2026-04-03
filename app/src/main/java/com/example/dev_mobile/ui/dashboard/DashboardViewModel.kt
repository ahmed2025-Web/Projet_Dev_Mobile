
package com.example.dev_mobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dev_mobile.network.FestivalDashboardDto
import com.example.dev_mobile.network.ReservationDto
import com.example.dev_mobile.repository.ApiResult
import com.example.dev_mobile.repository.FestivalRepository
import com.example.dev_mobile.repository.ReservationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val festival: FestivalDashboardDto? = null,
    val reservations: List<ReservationDto> = emptyList(),
    val errorMessage: String? = null,
    val lastRefresh: Long = 0L
)

class DashboardViewModel : ViewModel() {
    private val festivalRepo    = FestivalRepository()
    private val reservationRepo = ReservationRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Charger le festival courant en parallèle
            val festivalDeferred = async { festivalRepo.getFestivalCourant() }
            val festival = festivalDeferred.await()

            if (festival == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Aucun festival courant défini"
                )
                return@launch
            }

            // Charger les réservations du festival courant
            val reservations = when (val r = reservationRepo.getByFestival(festival.id)) {
                is ApiResult.Success -> r.data
                is ApiResult.Error   -> emptyList()
            }

            _uiState.value = _uiState.value.copy(
                isLoading    = false,
                festival     = festival,
                reservations = reservations,
                lastRefresh  = System.currentTimeMillis()
            )
        }
    }

    // Statistiques calculées depuis les réservations
    fun getReservationStats(): Map<String, Int> {
        val list = _uiState.value.reservations
        return mapOf(
            "total"               to list.size,
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

    fun getTotalTablesReservees(): Int =
        _uiState.value.reservations.sumOf { it.nb_tables_reservees }

    fun getMontantTotal(): Double =
        _uiState.value.reservations.sumOf { it.montant_brut }
}