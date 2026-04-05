package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class EditeurSummaryDto(
    val id: Int,
    val nom: String,
    val nb_jeux: Int = 0,
    val nb_contacts: Int = 0
)

data class JeuEditeurDto(
    val id: Int,
    val nom: String,
    val type_jeu: String? = null,
    val age_mini: Int? = null,
    val age_maxi: Int? = null,
    val joueurs_mini: Int? = null,
    val joueurs_maxi: Int? = null,
    val taille_table: String? = null,
    val duree_moyenne: Int? = null
)

data class ContactEditeurDto(
    val id: Int? = null,
    val nom: String,
    val email: String? = null,
    val telephone: String? = null,
    val role_profession: String? = null,
    val reservant_id: Int? = null,
    val reservant_nom: String? = null,
    val type_reservant: String? = null
)

data class CreateEditeurRequest(
    val nom: String,
    val contacts: List<ContactEditeurDto>? = null
)

data class EditeurCreateResponse(
    val editeur: EditeurSummaryDto,
    val reservant_id: Int
)

data class DeleteEntityResponse(
    val message: String? = null,
    val error: String? = null
)

interface EditeurApiService {
    @GET("api/editeurs")
    suspend fun getEditeurs(): Response<List<EditeurSummaryDto>>

    @GET("api/editeurs/{id}/jeux")
    suspend fun getJeuxEditeur(@Path("id") id: Int): Response<List<JeuEditeurDto>>

    @GET("api/editeurs/{id}/contacts")
    suspend fun getContactsEditeur(@Path("id") id: Int): Response<List<ContactEditeurDto>>

    @POST("api/editeurs")
    suspend fun createEditeur(@Body body: CreateEditeurRequest): Response<EditeurCreateResponse>

    @PUT("api/editeurs/{id}")
    suspend fun updateEditeur(
        @Path("id") id: Int,
        @Body body: CreateEditeurRequest
    ): Response<EditeurSummaryDto>

    @DELETE("api/editeurs/{id}")
    suspend fun deleteEditeur(@Path("id") id: Int): Response<DeleteEntityResponse>
}
