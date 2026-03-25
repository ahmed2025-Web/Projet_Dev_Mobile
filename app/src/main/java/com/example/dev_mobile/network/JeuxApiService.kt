package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.GET

data class AuteurDto(
    val id: Int,
    val nom: String?,
    val prenom: String?
)

data class JeuDto(
    val id: Int,
    val nom: String?,
    val publisherName: String?,
    val gameType: String?,
    val minAge: Int?,
    val maxAge: Int?,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val tableSize: String?,
    val averageDuration: Int?,
    val auteurs: List<AuteurDto>?
)

interface JeuxApiService {
    @GET("api/jeux")
    suspend fun getAll(): Response<List<JeuDto>>
}
