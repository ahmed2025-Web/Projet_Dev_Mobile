package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.*

// ── DTOs Zones Tarifaires ─────────────────────────────────────────────────────

data class ZoneTarifaireDto(
    val id: Int,
    val festival_id: Int,
    val nom: String,
    val nombre_tables_total: Int = 0,
    val tables_disponibles: Int = 0,
    val tables_reservees: Int = 0,
    val prix_table: Double = 0.0,
    val prix_m2: Double = 0.0
)

// ── DTOs Réservations ─────────────────────────────────────────────────────────

data class ReservationDto(
    val id: Int,
    val festival_id: Int,
    val festival_nom: String,
    val reservant_id: Int,
    val reservant_nom: String,
    val type_reservant: String,
    val editeur_id: Int? = null,
    val editeur_nom: String? = null,
    val etat_contact: String = "pas_contacte",
    val etat_presence: String = "non_defini",
    val date_dernier_contact: String? = null,
    val nb_prises_electriques: Int = 0,
    val viendra_animer: Boolean = true,
    val remise_tables: Int = 0,
    val remise_montant: Double = 0.0,
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val nb_contacts: Int = 0,
    val nb_tables_reservees: Int = 0,
    val montant_tables: Double = 0.0,
    val montant_prises: Double = 0.0,
    val montant_brut: Double = 0.0,
    val nb_jeux: Int = 0,
    val nb_jeux_places: Int = 0,
    val nb_jeux_recus: Int = 0
)

data class ZoneReserveeDto(
    val id: Int? = null,
    val reservation_id: Int? = null,
    val zone_tarifaire_id: Int,
    val zone_tarifaire_nom: String? = null,
    val nombre_tables: Int,
    val prix_unitaire: Double = 0.0
)

data class ContactRelanceDto(
    val id: Int? = null,
    val reservation_id: Int? = null,
    val date_contact: String? = null,
    val type_contact: String? = null,
    val notes: String? = null,
    val created_at: String? = null
)

// ── DTOs Jeux Festival ────────────────────────────────────────────────────────

data class JeuFestivalDto(
    val id: Int,
    val reservation_id: Int,
    val jeu_id: Int,
    val jeu_nom: String? = null,
    val editeur_nom: String? = null,
    val zone_plan_id: Int? = null,
    val zone_plan_nom: String? = null,
    val nombre_exemplaires: Int = 1,
    val tables_allouees: Double = 1.0,
    val nb_tables_std: Int = 0,
    val nb_tables_gde: Int = 0,
    val nb_tables_mairie: Int = 0,
    val jeu_recu: Boolean = false,
    val est_place: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class AddJeuFestivalRequest(
    val jeu_id: Int,
    val nombre_exemplaires: Int = 1,
    val tables_allouees: Double = 1.0
)

data class UpdateJeuRecuRequest(
    val jeu_recu: Boolean
)

data class ReservationDetailDto(
    val id: Int,
    val festival_id: Int,
    val festival_nom: String,
    val reservant_id: Int,
    val reservant_nom: String,
    val type_reservant: String,
    val editeur_id: Int? = null,
    val editeur_nom: String? = null,
    val etat_contact: String = "pas_contacte",
    val etat_presence: String = "non_defini",
    val date_dernier_contact: String? = null,
    val nb_prises_electriques: Int = 0,
    val viendra_animer: Boolean = true,
    val remise_tables: Int = 0,
    val remise_montant: Double = 0.0,
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val nb_contacts: Int = 0,
    val nb_tables_reservees: Int = 0,
    val montant_tables: Double = 0.0,
    val montant_prises: Double = 0.0,
    val montant_brut: Double = 0.0,
    val nb_jeux: Int = 0,
    val nb_jeux_places: Int = 0,
    val nb_jeux_recus: Int = 0,
    val contacts: List<ContactRelanceDto> = emptyList(),
    val zones_reservees: List<ZoneReserveeDto> = emptyList(),
    val jeux: List<JeuFestivalDto> = emptyList()
)

// ── Requests ──────────────────────────────────────────────────────────────────

data class CreateReservationRequest(
    val reservant_id: Int,
    val etat_contact: String = "contacte",
    val etat_presence: String = "non_defini",
    val nb_prises_electriques: Int = 0,
    val notes: String? = null,
    val viendra_animer: Boolean = true,
    val zones_reservees: List<ZoneReserveeRequest> = emptyList()
)

data class ZoneReserveeRequest(
    val zone_tarifaire_id: Int,
    val nombre_tables: Int,
    val prix_unitaire: Double? = null
)

data class UpdateReservationRequest(
    val etat_contact: String? = null,
    val etat_presence: String? = null,
    val nb_prises_electriques: Int? = null,
    val remise_tables: Int? = null,
    val remise_montant: Double? = null,
    val notes: String? = null,
    val viendra_animer: Boolean? = null,
    val zones_reservees: List<ZoneReserveeRequest>? = null
)

data class UpdateWorkflowContactRequest(
    val etat_contact: String
)

data class UpdateWorkflowPresenceRequest(
    val etat_presence: String
)

data class AddContactRelanceRequest(
    val date_contact: String? = null,
    val type_contact: String? = null,
    val notes: String? = null
)

data class ReservationActionResponse(
    val message: String? = null,
    val error: String? = null
)

// ── Interfaces Retrofit ───────────────────────────────────────────────────────

interface ZoneTarifaireApiService {
    @GET("api/zones-tarifaires/festival/{festivalId}")
    suspend fun getByFestival(@Path("festivalId") festivalId: Int): Response<List<ZoneTarifaireDto>>
}

interface ReservationApiService {

    @GET("api/reservations/festival/{festivalId}")
    suspend fun getByFestival(
        @Path("festivalId") festivalId: Int
    ): Response<List<ReservationDto>>

    @GET("api/reservations/{id}")
    suspend fun getById(
        @Path("id") id: Int
    ): Response<ReservationDetailDto>

    @POST("api/reservations/festival/{festivalId}")
    suspend fun create(
        @Path("festivalId") festivalId: Int,
        @Body body: CreateReservationRequest
    ): Response<ReservationDto>

    @PATCH("api/reservations/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body body: UpdateReservationRequest
    ): Response<ReservationDto>

    @DELETE("api/reservations/{id}")
    suspend fun delete(
        @Path("id") id: Int
    ): Response<ReservationActionResponse>

    @PATCH("api/reservations/{id}/workflow/contact")
    suspend fun updateWorkflowContact(
        @Path("id") id: Int,
        @Body body: UpdateWorkflowContactRequest
    ): Response<ReservationDto>

    @PATCH("api/reservations/{id}/workflow/presence")
    suspend fun updateWorkflowPresence(
        @Path("id") id: Int,
        @Body body: UpdateWorkflowPresenceRequest
    ): Response<ReservationDto>

    @POST("api/reservations/{id}/contacts")
    suspend fun addContactRelance(
        @Path("id") id: Int,
        @Body body: AddContactRelanceRequest
    ): Response<ContactRelanceDto>

    // ── Jeux de la réservation ─────────────────────────────────────────────────

    @POST("api/reservations/{id}/jeux")
    suspend fun addJeu(
        @Path("id") id: Int,
        @Body body: AddJeuFestivalRequest
    ): Response<JeuFestivalDto>

    @DELETE("api/reservations/{id}/jeux/{jeuFestivalId}")
    suspend fun removeJeu(
        @Path("id") id: Int,
        @Path("jeuFestivalId") jeuFestivalId: Int
    ): Response<ReservationActionResponse>

    @PATCH("api/reservations/{id}/jeux/{jeuFestivalId}/recu")
    suspend fun updateJeuRecu(
        @Path("id") id: Int,
        @Path("jeuFestivalId") jeuFestivalId: Int,
        @Body body: UpdateJeuRecuRequest
    ): Response<JeuFestivalDto>
}