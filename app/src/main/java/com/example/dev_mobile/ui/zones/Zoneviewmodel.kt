package com.example.dev_mobile.ui.zones

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ZoneViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ZoneUiState())
    val uiState: StateFlow<ZoneUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val mockZones = listOf(
            ZonePlanUi(
                id = 1,
                nom = "Zone Premium - Allée A",
                zoneTarifaireNom = "Zone Premium",
                nbTablesTotal = 20,
                nbTablesOccupees = 15.0,
                jeux = listOf(
                    JeuPlaceUi(1, "Dobble", "Asmodee", 3, 1.0, "Std"),
                    JeuPlaceUi(2, "Splendor", "Asmodee", 2, 1.0, "Std"),
                    JeuPlaceUi(3, "Azul", "Plan B", 2, 1.0, "Std")
                )
            ),
            ZonePlanUi(
                id = 2,
                nom = "Zone Premium - Allée B",
                zoneTarifaireNom = "Zone Premium",
                nbTablesTotal = 20,
                nbTablesOccupees = 18.0,
                jeux = listOf(
                    JeuPlaceUi(4, "Wingspan", "Matagot", 3, 2.0, "Gde"),
                    JeuPlaceUi(5, "Terraforming Mars", "Intrafin", 2, 2.0, "Gde")
                )
            ),
            ZonePlanUi(
                id = 3,
                nom = "Zone Standard - Allée C",
                zoneTarifaireNom = "Zone Standard",
                nbTablesTotal = 30,
                nbTablesOccupees = 22.0,
                jeux = emptyList()
            ),
            ZonePlanUi(
                id = 4,
                nom = "Zone Standard - Allée D",
                zoneTarifaireNom = "Zone Standard",
                nbTablesTotal = 30,
                nbTablesOccupees = 20.0,
                jeux = emptyList()
            )
        )

        val mockStocks = StocksMaterielUi(
            tablesStdUsed = 10.5f, tablesStdTotal = 150,
            tablesGdesUsed = 4f, tablesGdesTotal = 40,
            tablesMairieUsed = 0f, tablesMairieTotal = 30,
            chaisesStdUsed = 48, chaisesStdTotal = 600,
            chaisesMairieUsed = 0, chaisesMairieTotal = 180
        )

        _uiState.update {
            it.copy(
                zones = mockZones,
                stocks = mockStocks,
                totalZones = 5,
                tablesUtiliseesEspace = 87,
                tablesTotalEspace = 115,
                jeuxPlaces = 11
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun openCreateZoneDialog() {
        _uiState.update { it.copy(showCreateZoneDialog = true) }
    }

    fun closeCreateZoneDialog() {
        _uiState.update { it.copy(showCreateZoneDialog = false) }
    }

    fun openPlaceJeuDialog(zoneId: Int) {
        _uiState.update { it.copy(showPlaceJeuDialog = true, selectedZoneId = zoneId) }
    }

    fun closePlaceJeuDialog() {
        _uiState.update { it.copy(showPlaceJeuDialog = false, selectedZoneId = null) }
    }
}
