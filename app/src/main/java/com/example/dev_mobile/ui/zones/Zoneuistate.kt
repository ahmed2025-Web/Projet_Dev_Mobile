package com.example.dev_mobile.ui.zones

data class JeuPlaceUi(
    val id: Int,
    val nomJeu: String,
    val nomEditeur: String,
    val nbExemplaires: Int,
    val nbTables: Double, // Support du 0.5 table
    val typeTable: String // "Std", "Gde", "Mairie"
)

data class ZonePlanUi(
    val id: Int,
    val nom: String,
    val zoneTarifaireNom: String,
    val nbTablesTotal: Int, // Capacité physique max
    val nbTablesOccupees: Double,
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
    val chaisesStdUsed: Int, // Stock physique réel
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
    val tablesUtiliseesEspace: Int = 0, // En unité "espace"
    val tablesTotalEspace: Int = 0,
    val jeuxPlaces: Int = 0,
    val jeuxEnAttente: List<JeuPlaceUi> = emptyList(),
    val isGridView: Boolean = true,
    val userRole: String = "organisateur",
    val showCreateZoneDialog: Boolean = false,
    val showPlaceJeuDialog: Boolean = false,
    val selectedZoneId: Int? = null
) {
    val tauxOccupation: Int
        get() = if (tablesTotalEspace > 0) (tablesUtiliseesEspace * 100 / tablesTotalEspace) else 0
    
    val tablesLibres: Int
        get() = (tablesTotalEspace - tablesUtiliseesEspace).coerceAtLeast(0)
}
