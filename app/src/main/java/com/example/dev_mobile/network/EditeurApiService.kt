package com.example.dev_mobile.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// API Service pour les éditeurs (tous les modèles sont dans ApiModels.kt)
interface EditeurApiService {

    @GET("api/editeurs")
    suspend fun getEditeurs(): Response<List<Editeur>>

    @GET("api/editeurs/{id}")
    suspend fun getEditeur(@Path("id") id: Int): Response<Editeur>

    @GET("api/editeurs/{id}/jeux")
    suspend fun getJeuxEditeur(@Path("id") id: Int): Response<List<Jeu>>

    @GET("api/editeurs/{id}/contacts")
    suspend fun getContactsEditeur(@Path("id") id: Int): Response<List<Contact>>

    @POST("api/editeurs")
    suspend fun createEditeur(@Body payload: CreateEditeurPayload): Response<Map<String, Any>>

    @PUT("api/editeurs/{id}")
    suspend fun updateEditeur(@Path("id") id: Int, @Body payload: CreateEditeurPayload): Response<Editeur>

    @DELETE("api/editeurs/{id}")
    suspend fun deleteEditeur(@Path("id") id: Int): Response<Map<String, String>>
}
}
