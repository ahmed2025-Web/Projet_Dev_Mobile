package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.*

// ── DTOs ──────────────────────────────────────────────────────────────────────

data class FestivalCourantDto(
    val id: Int,
    val nom: String,
    val est_actif: Boolean = true,
    val est_courant: Boolean = false,
    val date_debut: String? = null,
    val date_fin: String? = null
)

data class FestivalDashboardDto(
    val id: Int,
    val nom: String,
    val est_actif: Boolean = true,
    val est_courant: Boolean = false,
    val espace_tables_total: Int,
    val date_debut: String? = null,
    val date_fin: String? = null,
    val created_at: String? = null,

    // Stocks
    val stock_tables_standard: Int = 0,
    val stock_tables_grandes: Int = 0,
    val stock_tables_mairie: Int = 0,
    val stock_chaises_standard: Int = 0,
    val stock_chaises_mairie: Int = 0,
    val prix_prise_electrique: Double = 0.0,

    // Stats zones
    val nb_zones_tarifaires: Int = 0,
    val nb_zones_plan: Int = 0,
    val tables_totales_tarifaires: Int = 0,

    // Stats réservations
    val nb_reservations_totales: Int = 0,
    val nb_reservations_confirmees: Int = 0,
    val nb_presents: Int = 0,
    val nb_absents: Int = 0,

    // Stats factures
    val nb_factures: Int = 0,
    val montant_total_factures: Double = 0.0,
    val nb_factures_payees: Int = 0,
    val montant_paye: Double = 0.0
)

data class CreateFestivalRequest(
    val nom: String,
    val espace_tables_total: Int,
    val date_debut: String? = null,
    val date_fin: String? = null,
    val description: String? = null,
    val stock_tables_standard: Int = 0,
    val stock_tables_grandes: Int = 0,
    val stock_tables_mairie: Int = 0,
    val stock_chaises_standard: Int = 0,
    val stock_chaises_mairie: Int = 0,
    val prix_prise_electrique: Double = 0.0,
    val est_actif: Boolean = true,
    val est_courant: Boolean = false
)

data class UpdateFestivalRequest(
    val nom: String? = null,
    val espace_tables_total: Int? = null,
    val date_debut: String? = null,
    val date_fin: String? = null,
    val description: String? = null,
    val stock_tables_standard: Int? = null,
    val stock_tables_grandes: Int? = null,
    val stock_tables_mairie: Int? = null,
    val stock_chaises_standard: Int? = null,
    val stock_chaises_mairie: Int? = null,
    val prix_prise_electrique: Double? = null,
    val est_actif: Boolean? = null
)

data class FestivalDeleteResponse(
    val message: String? = null,
    val error: String? = null
)

data class CanDeleteResponse(
    val canDelete: Boolean,
    val reason: String? = null
)

// ── Interface ─────────────────────────────────────────────────────────────────

interface FestivalApiService {

    @GET("api/festivals/courant")
    suspend fun getFestivalCourant(): Response<FestivalDashboardDto>

    @GET("api/festivals")
    suspend fun getAll(): Response<List<FestivalDashboardDto>>

    @GET("api/festivals/{id}")
    suspend fun getById(@Path("id") id: Int): Response<FestivalDashboardDto>

    @POST("api/festivals")
    suspend fun create(@Body body: CreateFestivalRequest): Response<FestivalDashboardDto>

    @PATCH("api/festivals/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body body: UpdateFestivalRequest
    ): Response<FestivalDashboardDto>

    @PATCH("api/festivals/{id}/set-courant")
    suspend fun setCourant(@Path("id") id: Int): Response<FestivalDashboardDto>

    @GET("api/festivals/{id}/can-delete")
    suspend fun canDelete(@Path("id") id: Int): Response<CanDeleteResponse>

    @DELETE("api/festivals/{id}")
    suspend fun delete(@Path("id") id: Int): Response<FestivalDeleteResponse>
}
