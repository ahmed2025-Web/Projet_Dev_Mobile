// app/src/main/java/com/example/dev_mobile/ui/zones/ZoneUiState.kt
package com.example.dev_mobile.ui.zones

import com.example.dev_mobile.network.JeuDisponibleDto
import com.example.dev_mobile.network.ZonePlanDetailDto
import com.example.dev_mobile.network.ZoneTarifaireDetailDto

enum class ZoneTab { TARIFAIRES, PLAN }

data class ZoneUiState(
    val isLoading: Boolean = false,
    val activeTab: ZoneTab = ZoneTab.TARIFAIRES,

    // Zones tarifaires
    val zonesTarifaires: List<ZoneTarifaireDetailDto> = emptyList(),
    val isSubmittingTarifaire: Boolean = false,
    val showCreateTarifaireDialog: Boolean = false,
    val showEditTarifaireDialog: Boolean = false,
    val tarifaireToEdit: ZoneTarifaireDetailDto? = null,

    // Zones du plan
    val zonesPlan: List<ZonePlanDetailDto> = emptyList(),
    val isSubmittingPlan: Boolean = false,
    val showCreatePlanDialog: Boolean = false,
    val showEditPlanDialog: Boolean = false,
    val planToEdit: ZonePlanDetailDto? = null,

    // Placement des jeux
    val zonePlanForPlacement: ZonePlanDetailDto? = null, // zone sélectionnée pour placer un jeu
    val showPlacerJeuDialog: Boolean = false,
    val jeuxDisponibles: List<JeuDisponibleDto> = emptyList(),
    val isLoadingJeux: Boolean = false,
    val isSubmittingPlacement: Boolean = false,

    // Festival courant
    val festivalId: Int = -1,
    val festivalNom: String = "",
    val festivalEspaceTables: Int = 0,

    // Messages
    val errorMessage: String? = null,
    val successMessage: String? = null
)