package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.*

// ── DTOs Jeux Publics ─────────────────────────────────────────────────────────

data class JeuPublicDto(
    val jeu_id: Int,
    val jeu_nom: String,
    val type_jeu: String? = null,
    val age_mini: Int? = null,
    val age_maxi: Int? = null,
    val joueurs_mini: Int? = null,
    val joueurs_maxi: Int? = null,
    val duree_moyenne: Int? = null,
    val taille_table: String? = null,
    val editeur_id: Int,
    val editeur_nom: String,
    val auteurs: String? = null,
    val festival_id: Int,
    val festival_nom: String,
    val zone_plan_id: Int? = null,
    val zone_plan_nom: String? = null,
    val presente_par: String? = null,
    val nombre_exemplaires: Int = 1
)

// ── DTOs Éditeurs Publics ─────────────────────────────────────────────────────

data class EditeurPublicDto(
    val festival_id: Int,
    val festival_nom: String,
    val editeur_id: Int,
    val editeur_nom: String,
    val nb_jeux_presentes: Int = 0,
    val nb_reservants_pour_editeur: Int = 0
)

// ── DTOs Zones du Plan (vue publique) ─────────────────────────────────────────

data class ZonePlanPublicDto(
    val id: Int,
    val festival_id: Int,
    val nom: String,
    val nombre_tables_total: Int = 0,
    val tables_disponibles: Int? = null,
    val tables_utilisees: Int? = null,
    val nb_jeux_places: Int = 0
)

// ── DTO Festival Courant Public ───────────────────────────────────────────────

data class FestivalPublicDto(
    val id: Int,
    val nom: String,
    val est_courant: Boolean = true,
    val date_debut: String? = null,
    val date_fin: String? = null
)

// ── Interfaces Retrofit ───────────────────────────────────────────────────────

interface PublicApiService {

    @GET("api/view-public/jeux/festival-courant")
    suspend fun getJeuxFestivalCourant(): Response<List<JeuPublicDto>>

    @GET("api/view-public/editeurs/festival-courant")
    suspend fun getEditeursFestivalCourant(): Response<List<EditeurPublicDto>>

    @GET("api/view-public/festival-courant")
    suspend fun getFestivalCourant(): Response<FestivalPublicDto>
}

interface ZonePlanPublicApiService {

    @GET("api/zones-plan/festival/{festivalId}")
    suspend fun getZonesByFestival(
        @Path("festivalId") festivalId: Int
    ): Response<List<ZonePlanPublicDto>>
}