
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

data class ZonePlanDetailDto(
    val id: Int,
    val festival_id: Int,
    val nom: String,
    val nombre_tables_total: Int = 0,
    val tables_disponibles: Int? = null,
    val tables_utilisees: Int? = null,
    val nb_jeux_places: Int = 0
)

data class CreateZonePlanRequest(
    val nom: String,
    val nombre_tables_total: Int
)

data class UpdateZonePlanRequest(
    val nom: String? = null,
    val nombre_tables_total: Int? = null
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
}