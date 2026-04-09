// app/src/main/java/com/example/dev_mobile/network/ZoneApiService.kt
package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.*

// ── DTOs Zones Tarifaires ─────────────────────────────────────────────────────

data class ZoneTarifaireDetailDto(
    val id: Int,
    val festival_id: Int,
    val nom: String,
    val nombre_tables_total: Int = 0,
    val tables_disponibles: Int = 0,
    val tables_reservees: Int = 0,
    val prix_table: Double = 0.0,
    val prix_m2: Double = 0.0
)

data class CreateZoneTarifaireRequest(
    val nom: String,
    val nombre_tables_total: Int,
    val prix_table: Double,
    val prix_m2: Double? = null
)

data class UpdateZoneTarifaireRequest(
    val nom: String? = null,
    val nombre_tables_total: Int? = null,
    val prix_table: Double? = null,
    val prix_m2: Double? = null
)

// ── DTOs Zones du Plan ────────────────────────────────────────────────────────

data class JeuPlaceDto(
    val id: Int,
    val jeu_id: Int,
    val zone_plan_id: Int,
    val nb_tables: Int = 1,
    val type_table: String = "standard", // standard, grande, mairie
    val nom_jeu: String = "",
    val nom_editeur: String = "",
    val nb_exemplaires: Int = 0
)

data class ZonePlanDetailDto(
    val id: Int,
    val festival_id: Int,
    val nom: String,
    val nombre_tables_total: Int = 0,
    val tables_disponibles: Int? = null,
    val tables_utilisees: Int? = null,
    val nb_jeux_places: Int = 0,
    val jeux_places: List<JeuPlaceDto> = emptyList()
)

data class CreateZonePlanRequest(
    val nom: String,
    val nombre_tables_total: Int
)

data class UpdateZonePlanRequest(
    val nom: String? = null,
    val nombre_tables_total: Int? = null
)

// ── DTOs Placement de jeux ────────────────────────────────────────────────────

data class JeuDisponibleDto(
    val id: Int,
    val nom: String,
    val editeur: String = "",
    val nb_exemplaires: Int = 0
)

data class PlacerJeuRequest(
    val jeu_id: Int,
    val nb_tables: Int = 1,
    val type_table: String = "standard"
)

data class ZoneActionResponse(
    val message: String? = null,
    val error: String? = null
)

// ── Interfaces Retrofit ───────────────────────────────────────────────────────

interface ZoneTarifaireFullApiService {

    @GET("api/zones-tarifaires/festival/{festivalId}")
    suspend fun getByFestival(
        @Path("festivalId") festivalId: Int
    ): Response<List<ZoneTarifaireDetailDto>>

    @GET("api/zones-tarifaires/{id}")
    suspend fun getById(@Path("id") id: Int): Response<ZoneTarifaireDetailDto>

    @POST("api/zones-tarifaires/festival/{festivalId}")
    suspend fun create(
        @Path("festivalId") festivalId: Int,
        @Body body: CreateZoneTarifaireRequest
    ): Response<ZoneTarifaireDetailDto>

    @PATCH("api/zones-tarifaires/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body body: UpdateZoneTarifaireRequest
    ): Response<ZoneTarifaireDetailDto>

    @DELETE("api/zones-tarifaires/{id}")
    suspend fun delete(@Path("id") id: Int): Response<ZoneActionResponse>
}

interface ZonePlanFullApiService {

    @GET("api/zones-plan/festival/{festivalId}")
    suspend fun getByFestival(
        @Path("festivalId") festivalId: Int
    ): Response<List<ZonePlanDetailDto>>

    @GET("api/zones-plan/{id}")
    suspend fun getById(@Path("id") id: Int): Response<ZonePlanDetailDto>

    @POST("api/zones-plan/festival/{festivalId}")
    suspend fun create(
        @Path("festivalId") festivalId: Int,
        @Body body: CreateZonePlanRequest
    ): Response<ZonePlanDetailDto>

    @PATCH("api/zones-plan/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body body: UpdateZonePlanRequest
    ): Response<ZonePlanDetailDto>

    @DELETE("api/zones-plan/{id}")
    suspend fun delete(@Path("id") id: Int): Response<ZoneActionResponse>

    // Placement des jeux dans une zone
    @POST("api/zones-plan/{zoneId}/jeux")
    suspend fun placerJeu(
        @Path("zoneId") zoneId: Int,
        @Body body: PlacerJeuRequest
    ): Response<ZonePlanDetailDto>

    @DELETE("api/zones-plan/{zoneId}/jeux/{jeuId}")
    suspend fun retirerJeu(
        @Path("zoneId") zoneId: Int,
        @Path("jeuId") jeuId: Int
    ): Response<ZoneActionResponse>
}

// Interface pour récupérer les jeux disponibles d'un festival (réutilise JeuApiService)
interface JeuxFestivalApiService {
    @GET("api/jeux/festival/{festivalId}")
    suspend fun getJeuxFestival(
        @Path("festivalId") festivalId: Int
    ): Response<List<JeuDisponibleDto>>
}