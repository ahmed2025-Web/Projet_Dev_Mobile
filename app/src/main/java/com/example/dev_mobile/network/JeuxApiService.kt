package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// API Service pour les jeux (tous les modèles sont dans ApiModels.kt)
interface JeuxApiService {

    @GET("api/jeux")
    suspend fun getJeux(): Response<List<Jeu>>

    @GET("api/jeux/{id}")
    suspend fun getJeu(@Path("id") id: Int): Response<Jeu>

    @GET("api/editeurs")
    suspend fun getEditeurs(): Response<List<Editeur>>

    @POST("api/jeux")
    suspend fun createJeu(@Body payload: CreateJeuPayload): Response<Jeu>

    @PUT("api/jeux/{id}")
    suspend fun updateJeu(@Path("id") id: Int, @Body payload: CreateJeuPayload): Response<Jeu>

    @DELETE("api/jeux/{id}")
    suspend fun deleteJeu(@Path("id") id: Int): Response<Map<String, String>>
}
