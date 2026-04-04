package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class AuteurDto(
    val id: Int? = null,
    val nom: String,
    val prenom: String? = null
)

data class JeuSummaryDto(
    val id: Int,
    val nom: String,
    val editeur_id: Int,
    val editeur_nom: String? = null,
    val type_jeu: String? = null,
    val age_mini: Int? = null,
    val age_maxi: Int? = null,
    val joueurs_mini: Int? = null,
    val joueurs_maxi: Int? = null,
    val taille_table: String? = null,
    val duree_moyenne: Int? = null,
    val auteurs: List<AuteurDto>? = null
) {
    fun getAuteursOrEmpty() = auteurs ?: emptyList()
}

data class CreateJeuRequest(
    val nom: String,
    val editeur_id: Int,
    val type_jeu: String? = null,
    val age_mini: Int? = null,
    val age_maxi: Int? = null,
    val joueurs_mini: Int? = null,
    val joueurs_maxi: Int? = null,
    val taille_table: String? = null,
    val duree_moyenne: Int? = null,
    val auteurs: List<AuteurDto>
)

data class JeuMutationResponse(
    val jeu: JeuSummaryDto,
    val auteur_ids: List<Int> = emptyList()
)

interface JeuApiService {
    @GET("api/jeux")
    suspend fun getJeux(): Response<List<JeuSummaryDto>>

    @GET("api/jeux/{id}")
    suspend fun getJeu(@Path("id") id: Int): Response<JeuSummaryDto>

    @POST("api/jeux")
    suspend fun createJeu(@Body body: CreateJeuRequest): Response<JeuMutationResponse>

    @PUT("api/jeux/{id}")
    suspend fun updateJeu(
        @Path("id") id: Int,
        @Body body: CreateJeuRequest
    ): Response<JeuMutationResponse>

    @DELETE("api/jeux/{id}")
    suspend fun deleteJeu(@Path("id") id: Int): Response<DeleteEntityResponse>
}
