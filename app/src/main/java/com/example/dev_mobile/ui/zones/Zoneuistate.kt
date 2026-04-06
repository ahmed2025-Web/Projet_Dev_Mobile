package com.example.dev_mobile.ui.zones

data class JeuPlaceUi(
    val id: Int,
    val nomJeu: String,
    val nomEditeur: String,
    val nbExemplaires: Int,
    val nbTables: Int,
    val typeTable: String // "Std", "Gde", "Mairie"
)

data class ZonePlanUi(
    val id: Int,
    val nom: String,
    val zoneTarifaireNom: String,
    val nbTablesTotal: Int,
    val nbTablesOccupees: Int,
    val jeux: List<JeuPlaceUi> = emptyList()
) {
    val occupationPercent: Float
        get() = if (nbTablesTotal > 0) nbTablesOccupees.toFloat() / nbTablesTotal else 0f
}

data class StocksMaterielUi(
    val tablesStdUsed: Float,
    val tablesStdTotal: Int,
    val tablesGdesUsed: Float,
    val tablesGdesTotal: Int,
    val tablesMairieUsed: Float,
    val tablesMairieTotal: Int,
    val chaisesStdUsed: Int,
    val chaisesStdTotal: Int,
    val chaisesMairieUsed: Int,
    val chaisesMairieTotal: Int
)

data class ZoneUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val zones: List<ZonePlanUi> = emptyList(),
    val stocks: StocksMaterielUi? = null,
    val totalZones: Int = 0,
    val tablesUtilisees: Int = 0,
    val tablesTotal: Int = 0,
    val jeuxPlaces: Int = 0,
    val tablesLibres: Int = 0,
    // Dialog states
    val showCreateZoneDialog: Boolean = false,
    val showPlaceJeuDialog: Boolean = false,
    val selectedZoneId: Int? = null,
    val isGridView: Boolean = true
) {
    val tauxOccupation: Int
        get() = if (tablesTotal > 0) (tablesUtilisees * 100 / tablesTotal) else 0
}
