// app/src/main/java/com/example/dev_mobile/network/ReservantApiService.kt
package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.*



data class ContactDto(
    val id: Int? = null,
    val nom: String,
    val email: String? = null,
    val telephone: String? = null,
    val role_profession: String? = null,
    val reservant_id: Int? = null
)


data class ReservantDto(
    val id: Int,
    val nom: String,
    val type_reservant: String,
    val editeur_id: Int? = null,
    val editeur_nom: String? = null,
    val nb_contacts: Int = 0,
    val nb_reservations: Int = 0,
    val created_at: String? = null,
    val updated_at: String? = null
)

//détail avec contacts + historique réservations
data class HistoriqueDto(
    val id: Int,
    val festival_id: Int,
    val festival_nom: String,
    val date_debut: String? = null,
    val date_fin: String? = null,
    val etat_contact: String,
    val etat_presence: String,
    val nb_tables: Int = 0,
    val montant_total: Double = 0.0,
    val created_at: String? = null
)

data class ReservantDetailDto(
    val id: Int,
    val nom: String,
    val type_reservant: String,
    val editeur_id: Int? = null,
    val editeur_nom: String? = null,
    val contacts: List<ContactDto>? = emptyList(),
    val historique: List<HistoriqueDto>? = emptyList()
)


data class CreateReservantRequest(
    val nom: String,
    val type_reservant: String,
    val editeur_id: Int? = null,
    val contacts: List<ContactDto>? = emptyList()
)

// ── Interface Retrofit ────────────────────────────────────────────────────────

interface ReservantApiService {

    @GET("api/reservants")
    suspend fun getAll(): Response<List<ReservantDto>>

    @GET("api/reservants/{id}")
    suspend fun getById(@Path("id") id: Int): Response<ReservantDetailDto>

    @GET("api/reservants/{id}/contacts")
    suspend fun getContacts(@Path("id") id: Int): Response<List<ContactDto>>

    @POST("api/reservants")
    suspend fun create(@Body body: CreateReservantRequest): Response<ReservantDetailDto>

    @PUT("api/reservants/{id}")
    suspend fun update(@Path("id") id: Int, @Body body: CreateReservantRequest): Response<ReservantDetailDto>

    @DELETE("api/reservants/{id}")
    suspend fun delete(@Path("id") id: Int): Response<AuthResponse>
}